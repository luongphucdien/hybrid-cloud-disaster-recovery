import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.security.MessageDigest;
import java.util.*;

public class DR2 {
    private static boolean failoverFlag = false;
    
    public static void main(String[] args) {
        final Map<Long, String> vmIntegrityMap = new HashMap<>();
        final SecurityAuthority securityAuthority = new SecurityAuthority();
        final List<VmSimple> newVms = new ArrayList<>();
        
        CloudSimPlus simulation = new CloudSimPlus();
        
        // Datacenters
        DatacenterSimple privateDC = createDatacenter(
                simulation,
                "private-dc"
        );
        DatacenterSimple publicDC = createDatacenter(
                simulation,
                "public-dc"
        );
        
        // Broker
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);
        
        // VMs
        List<VmSimple> vms = createVms(Config.VMConfig.NUMBER);
        
        // Cloudlets
        List<CloudletSimple> cloudlets = createCloudlets(Config.CloudletConfig.NUMBER);
        
        // Submit
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);
        
        // Compute and store integrity hashes of each VM (snapshot)
        for(VmSimple vm : vms) {
            vmIntegrityMap.put(
                    vm.getId(),
                    IntegrityChecker.computeHash(vm)
            );
        }
        
        System.out.println("==========CONFIG==========");
        System.out.print(Config.asString());
        System.out.println("==========END CONFIG==========");
        
        // Run Simulation
        simulation.addOnClockTickListener(event -> {
            double time = simulation.clock();
            double failTime = 5;
            
            // Main Simulation
            if(time >= failTime && !failoverFlag) {
                failoverFlag = true;
                
                System.out.println("<INIT> VMs created: " + broker.getVmCreatedList());
                System.out.println("<INIT> Cloudlets created: " +
                        broker.getCloudletSubmittedList());
                
                System.out.println("<INIT> VMs and their hashes:");
                for(Map.Entry<Long, String> entry : vmIntegrityMap.entrySet()) {
                    System.out.printf(
                            "<INIT> VM=%d | Hash=%s%n",
                            entry.getKey(),
                            entry.getValue()
                    );
                }
                
                System.out.printf(
                        "==========FAILURE AT T=%.3f==========%n",
                        time
                );
                
                // Shutting down hosts
                System.out.printf(
                        "<INFO> Shutting down all hosts in private DC at t=%.3f%n",
                        simulation.clock()
                );
                for(Host host : privateDC.getHostList()) {
                    host.setFailed(true);
                }
                
                System.out.printf(
                        "<INFO> Starting secure failover to public DC at t=%.3f%n",
                        simulation.clock()
                );
                
                // Build and sign migration request
                MigrationRequest rq = new MigrationRequest(
                        privateDC.getName(),
                        publicDC.getName(),
                        System.currentTimeMillis()
                );
                rq.setSignature(securityAuthority.sign(rq));
                System.out.printf(
                        "<MIGRATION-REQUEST> Signature: %s%n",
                        rq.getSignature()
                );
                
                // Verify signature/auth
                if(!securityAuthority.verify(rq)) {
                    System.out.println("<AUTH> AUTH CHECK FAILED - ABORT");
                    return;
                }
                System.out.println("<MIGRATION> Migration request auth verified");
                
                // VM recreation in public DC
                for(VmSimple vm : vms) {
                    final long oldId = vm.getId();
                    final String expectedHash = vmIntegrityMap.get(oldId);
                    final String currentHash = IntegrityChecker.computeHash(vm);
                    
                    if(!Objects.equals(
                            expectedHash,
                            currentHash
                    )) {
                        System.out.printf(
                                "<INTEGRITY> INTEGRITY MISMATCH FOR VM %d (EXPECTED: " +
                                        "%s, GOT %s)- SKIPPING...%n",
                                oldId,
                                expectedHash,
                                currentHash
                        );
                        continue;
                    }
                    
                    // Create new VM with same specs
                    VmSimple newVm = new VmSimple(
                            vm.getMips(),
                            vm.getPesNumber()
                    );
                    newVm
                            .setRam(vm
                                    .getRam()
                                    .getCapacity())
                            .setBw(vm
                                    .getBw()
                                    .getCapacity())
                            .setSize(vm
                                    .getStorage()
                                    .getCapacity())
                            .setCloudletScheduler(new CloudletSchedulerSpaceShared());
                    
                    // Add VM to migrating list
                    newVms.add(newVm);
                    
                    System.out.printf(
                            "<VM-MIGRATION> VM %d integrity OK | Expected: %s | Got: %s%n",
                            oldId,
                            expectedHash,
                            currentHash
                    );
                }
                
                // Broker submits migrating VMs to available DC (public-dc)
                broker.submitVmList(newVms);
                
                // Rebind cloudlets
                for(CloudletSimple cloudlet : cloudlets) {
                    VmSimple vmToBeBound = newVms.get((int) (cloudlet.getId() %
                            Config.VMConfig.NUMBER));
                    
                    if(!cloudlet.isFinished()) {
                        broker.bindCloudletToVm(
                                cloudlet,
                                vmToBeBound
                        );
                    }
                    
                    System.out.printf(
                            "<CLOUDLET-REBIND> Rebind cloudlet %d to VM %d%n",
                            cloudlet.getId(),
                            vmToBeBound.getId()
                    );
                }
                
                System.out.println("==========SECURE FAILOVER COMPLETED==========");
            }
        });
        
        simulation.start();
        
        System.out.println("==========SIMULATION FINISHED==========");
        printFinalCloudletStatus(broker);
        printAllVMLocations(broker);
    }
    
    private static void printAllVMLocations(DatacenterBrokerSimple broker) {
        broker
                .getVmCreatedList()
                .forEach(vm -> {
                    String status;
                    Host host = vm.getHost();
                    
                    if(vm.isFailed() || host == Host.NULL) {
                        status = "OFFLINE";
                    } else {
                        String dcName = host.getDatacenter() != Datacenter.NULL ? host
                                .getDatacenter()
                                .getName() : "UNKNOWN DC";
                        
                        status = String.format(
                                "Host %d in %s",
                                host.getId(),
                                dcName
                        );
                    }
                    
                    System.out.printf(
                            "<VM-LOG> VM=%d Status=%s%n",
                            vm.getId(),
                            status
                    );
                });
    }
    
    private static DatacenterSimple createDatacenter(CloudSimPlus simulation, String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        
        for(int i = 0; i < Config.DatacenterConfig.PE_NUMBER; i++) {
            peList.add(new PeSimple(Config.DatacenterConfig.PE_MIPS_CAPACITY));
        }
        
        for(int i = 0; i < Config.DatacenterConfig.HOST_NUMBER; i++) {
            HostSimple host = new HostSimple(
                    Config.DatacenterConfig.HOST_RAM,
                    Config.DatacenterConfig.HOST_BW,
                    Config.DatacenterConfig.HOST_STORAGE,
                    peList
            );
            hostList.add(host);
        }
        
        DatacenterSimple dc = new DatacenterSimple(
                simulation,
                hostList
        );
        dc.setName(name);
        return dc;
    }
    
    private static List<VmSimple> createVms(int count) {
        List<VmSimple> vms = new ArrayList<>();
        
        for(int i = 0; i < count; i++) {
            VmSimple vm = new VmSimple(
                    Config.VMConfig.MIPS_CAPACITY,
                    Config.VMConfig.PE_NUMBER
            );
            vm
                    .setRam(Config.VMConfig.RAM)
                    .setBw(Config.VMConfig.BW)
                    .setSize(Config.VMConfig.STORAGE)
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vms.add(vm);
        }
        return vms;
    }
    
    private static List<CloudletSimple> createCloudlets(int count) {
        List<CloudletSimple> cloudlets = new ArrayList<>();
        
        for(int i = 0; i < count; i++) {
            CloudletSimple cloudlet = new CloudletSimple(
                    Config.CloudletConfig.LENGTH,
                    Config.CloudletConfig.PE_NUMBER
            );
            cloudlet
                    .setFileSize(Config.CloudletConfig.FILE_SIZE)
                    .setOutputSize(Config.CloudletConfig.OUTPUT_SIZE);
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
    
    private static void printFinalCloudletStatus(DatacenterBrokerSimple broker) {
        broker
                .getCloudletFinishedList()
                .forEach(cloudlet -> System.out.printf(
                        "<CLOUDLET-LOG> Cloudlet %d finished on VM %d at %.3f, status=%s%n",
                        cloudlet.getId(),
                        cloudlet.getVm() != Vm.NULL ? cloudlet
                                .getVm()
                                .getId() : -1,
                        cloudlet.getFinishTime(),
                        cloudlet.getStatus()
                ));
    }
    
    static class IntegrityChecker {
        static String computeHash(VmSimple vm) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                String data = String.format(
                        "%d-%.3f-%d-%d-%d-%d",
                        vm.getId(),
                        vm.getMips(),
                        vm.getPesNumber(),
                        vm
                                .getRam()
                                .getCapacity(),
                        vm
                                .getBw()
                                .getCapacity(),
                        vm
                                .getStorage()
                                .getCapacity()
                );
                byte[] digest = messageDigest.digest(data.getBytes());
                StringBuilder hash = new StringBuilder();
                for(byte b : digest) {
                    hash.append(String.format(
                            "%02x",
                            b
                    ));
                }
                
                return hash.toString();
            } catch(Exception e) {
                System.out.println(e.getMessage());
                return "error";
            }
        }
    }
    
    static class MigrationRequest {
        final String sourceDC;
        final String destinationDC;
        final long timestamp;
        private String signature;
        
        MigrationRequest(String sourceDC, String destinationDC, long timestamp) {
            this.sourceDC = sourceDC;
            this.destinationDC = destinationDC;
            this.timestamp = timestamp;
        }
        
        String serialize() {
            return sourceDC + "->" + destinationDC + "|" + timestamp;
        }
        
        String getSignature() {
            return signature;
        }
        
        void setSignature(String signature) {
            this.signature = signature;
        }
    }
    
    // Rough simulated authority (Secret + Message). IRL uses asymmetric signatures and KMS
    static class SecurityAuthority {
        private final String secret = "guiyixiajiaowobaba";
        
        private String computeHash(String string) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] digest = messageDigest.digest(string.getBytes());
                StringBuilder hash = new StringBuilder();
                for(byte b : digest) {
                    hash.append(String.format(
                            "%02x",
                            b
                    ));
                }
                return hash.toString();
            } catch(Exception e) {
                System.out.println(e.getMessage());
                return "error";
            }
        }
        
        String sign(MigrationRequest rq) {
            return computeHash(rq.serialize() + "|" + secret);
        }
        
        boolean verify(MigrationRequest rq) {
            if(rq.getSignature() == null) {
                return false;
            }
            
            return rq
                    .getSignature()
                    .equals(sign(rq));
        }
    }
}
