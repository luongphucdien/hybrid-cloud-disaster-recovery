import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class DR1 {
    private static boolean shutdownFlag = false;
    
    public static void main(String[] args) {
        CloudSimPlus simulation = new CloudSimPlus();
        
        DatacenterSimple privateDC = createDatacenter(
                simulation,
                "private-DC"
        );
        DatacenterSimple publicDC = createDatacenter(
                simulation,
                "public-DC"
        );
        
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);
        List<Vm> vmList = createVms(4);
        broker.submitVmList(vmList);
        
        List<CloudletSimple> cloudlets = createCloudlets(4);
        broker.submitCloudletList(cloudlets);
        
        for(int i = 0; i < cloudlets.size(); i++) {
            broker.bindCloudletToVm(
                    cloudlets.get(i),
                    vmList.get(i)
            );
        }
        
        simulation.addOnClockTickListener(eventInfo -> {
            double time = simulation.clock();
            
            if(time == 0) {
                System.out.println("==========INITIALIZATION (t=0)==========");
                printAllVMLocations(vmList);
            }
            
            if(time >= 5 && !shutdownFlag) {
                shutdownFlag = true;
                
                System.out.println("==========PHASE 1A: PRE-SHUTDOWN PRIVATE DC==========");
                printAllVMLocations(vmList);
                
                System.out.println("==========PHASE 1B: SHUTTING DOWN PRIVATE DC==========");
                for(Host host : privateDC.getHostList()) {
                    host.setFailed(true);
                }
                
                System.out.println("==========PHASE 1C: ALL HOSTS REMOVED==========");
                printAllVMLocations(vmList);
                
                System.out.println("==========PHASE 2A: REQUESTING MIGRATION==========");
                for(Vm vm : vmList) {
                    Host target = publicDC
                            .getHostList()
                            .getFirst();
                    if(target != null) {
                        System.out.printf(
                                "<INFO> Requesting migration of VM %d to Host %d at " +
                                        "Public DC%n",
                                vm.getId(),
                                target.getId()
                        );
                        publicDC.requestVmMigration(vm);
                    }
                }
                System.out.println(publicDC
                        .getHost(0)
                        .getVmList());
                
                System.out.println("==========PHASE 2B: AFTER REQUESTING==========");
                printAllVMLocations(vmList);
            }
        });
        
        simulation.start();
        
        System.out.println("==========SUMMARY==========");
        printAllVMLocations(vmList);
        
        System.out.println("==========CLOUDLET RESULTS==========");
        broker
                .getCloudletFinishedList()
                .forEach(cloudlet -> {
                    System.out.printf(
                            "Cloudlet %d finished on VM %d%n",
                            cloudlet.getId(),
                            cloudlet
                                    .getVm()
                                    .getId()
                    );
                });
    }
    
    private static void printAllVMLocations(List<Vm> vms) {
        for(Vm vm : vms) {
            Host host = vm.getHost();
            Datacenter dc = host != Host.NULL ? host.getDatacenter() : null;
            
            if(dc == null) {
                System.out.printf(
                        "<INFO> VM %d is not hosted%n",
                        vm.getId()
                );
                continue;
            }
            
            System.out.printf(
                    "<INFO> VM=%d DC=%s Host=%d Migrating=%b%n",
                    vm.getId(),
                    dc.getName(),
                    host.getId(),
                    vm.isInMigration()
            );
        }
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
    
    private static List<Vm> createVms(int count) {
        List<Vm> vms = new ArrayList<>();
        
        double VM_MIPS_CAPACITY = 1000;
        long VM_PE_NUMBER = 1, VM_RAM = 1024 * 2, VM_BW = 1000, VM_STORAGE = 10_000;
        for(int i = 0; i < count; i++) {
            Vm vm = new VmSimple(
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
