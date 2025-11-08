package core;

import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public final class VMFactory {
    private VMFactory() {}
    
    public static List<Vm> createVMs(
            int count, double mipsCapacity, int cores, long ram,
            long bandwidth, long storage
    ) {
        List<Vm> vms = new ArrayList<>(count);
        
        for(int i = 0; i < count; i++) {
            Vm vm = new VmSimple(
                    mipsCapacity,
                    cores
            );
            vm
                    .setRam(ram)
                    .setBw(bandwidth)
                    .setSize(storage)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
            vms.add(vm);
        }
        
        return vms;
    }
}
