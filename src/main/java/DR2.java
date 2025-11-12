import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class DR2 {
    private static boolean failoverFlag = false;
    private static boolean summaryFlag = false;
    private static int VM_NUMBER = 4;
    private static int CLOUDLET_NUMBER = 12;
    
    public static void main(String[] args) {
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
        List<VmSimple> vms = createVms(VM_NUMBER);
        
        // Cloudlets
        List<CloudletSimple> cloudlets = createCloudlets(CLOUDLET_NUMBER);
        
        // Submit
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);
        
        // Run Simulation
        simulation.addOnClockTickListener(event -> {
            double time = simulation.clock();
            double failTime = 5;
            
            if(time >= failTime && !failoverFlag) {
                failoverFlag = true;
                
                System.out.printf(
                        "==========FAILURE AT T=%.3f==========%n",
                        time
                );
                
                System.out.printf(
                        "<INFO> Shutting down all hosts in private DC at t=%.3f%n",
                        simulation.clock()
                );
                for(Host host : privateDC.getHostList()) {
                    host.setFailed(true);
                }
                
                System.out.printf(
                        "<INFO> Starting failover to public DC at t=%.3f%n",
                        simulation.clock()
                );
                
                // VM recreation in public DC
                List<VmSimple> newVms = new ArrayList<>();
                for(VmSimple vm : vms) {
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
                    newVms.add(newVm);
                }
                
                // Broker submits new VMs
                broker.submitVmList(newVms);
                
                // Rebind cloudlets
                for(CloudletSimple cloudlet : cloudlets) {
                    VmSimple vmToBeBound = newVms.get((int) (cloudlet.getId() % VM_NUMBER));
                    
                    if(!cloudlet.isFinished()) {
                        broker.bindCloudletToVm(
                                cloudlet,
                                vmToBeBound
                        );
                    }
                    
                    System.out.printf(
                            "<INFO> Rebind cloudlet %d to VM %d%n",
                            cloudlet.getId(),
                            vmToBeBound.getId()
                    );
                }
            }
            
            System.out.printf(
                    "==========SUMMARY (T=%.3f)==========%n",
                    simulation.clock()
            );
            printAllVMLocations(broker);
        });
        simulation.start();
    }
    
    private static void printAllVMLocations(DatacenterBrokerSimple broker) {
        broker
                .getVmCreatedList()
                .forEach(vm -> {
                    String status;
                    
                    if(vm.isFailed() || vm.getHost() == Host.NULL) {
                        status = "FAILED";
                    } else {
                        status = String.format(
                                "Host %d in %s",
                                vm
                                        .getHost()
                                        .getId(),
                                vm
                                        .getHost()
                                        .getDatacenter()
                                        .getName()
                        );
                    }
                    
                    System.out.printf(
                            "<INFO> VM=%d Status=%s%n",
                            vm.getId(),
                            status
                    );
                });
    }
    
    private static DatacenterSimple createDatacenter(CloudSimPlus simulation, String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        
        int PE_NUMBER = 4;
        double PE_MIPS_CAPACITY = 2000;
        for(int i = 0; i < PE_NUMBER; i++) {
            peList.add(new PeSimple(PE_MIPS_CAPACITY));
        }
        
        long HOST_RAM = 1024 * 16, HOST_BW = 10_000, HOST_STORAGE = 1_000_000;
        HostSimple host = new HostSimple(
                HOST_RAM,
                HOST_BW,
                HOST_STORAGE,
                peList
        );
        hostList.add(host);
        
        DatacenterSimple dc = new DatacenterSimple(
                simulation,
                hostList
        );
        dc.setName(name);
        return dc;
    }
    
    private static List<VmSimple> createVms(int count) {
        List<VmSimple> vms = new ArrayList<>();
        
        double VM_MIPS_CAPACITY = 1000;
        long VM_PE_NUMBER = 1, VM_RAM = 1024 * 2, VM_BW = 1000, VM_STORAGE = 10_000;
        for(int i = 0; i < count; i++) {
            VmSimple vm = new VmSimple(
                    VM_MIPS_CAPACITY,
                    VM_PE_NUMBER
            );
            vm
                    .setRam(VM_RAM)
                    .setBw(VM_BW)
                    .setSize(VM_STORAGE);
            vms.add(vm);
        }
        return vms;
    }
    
    private static List<CloudletSimple> createCloudlets(int count) {
        List<CloudletSimple> cloudlets = new ArrayList<>();
        
        long CLOUDLET_LENGTH = 10_000, CLOUDLET_FILE_SIZE = 300, CLOUDLET_OUTPUT_SIZE = 300;
        int CLOUDLET_PE_NUMBER = 1;
        for(int i = 0; i < count; i++) {
            CloudletSimple cloudlet = new CloudletSimple(
                    CLOUDLET_LENGTH,
                    CLOUDLET_PE_NUMBER
            );
            cloudlet
                    .setFileSize(CLOUDLET_FILE_SIZE)
                    .setOutputSize(CLOUDLET_OUTPUT_SIZE);
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
}
