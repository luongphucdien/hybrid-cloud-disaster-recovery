package core;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;

import java.util.ArrayList;
import java.util.List;

public final class HostFactory {
    private HostFactory() {}
    
    public static List<Host> createHosts(
            int count, int cores, double mipsCapacity, long ram,
            long bandwidth, long storage
    ) {
        List<Host> hosts = new ArrayList<>();
        List<Pe> cpu = createCPU(
                cores,
                mipsCapacity
        );
        
        for(int i = 0; i < count; i++) {
            Host host = new HostSimple(
                    ram,
                    bandwidth,
                    storage,
                    cpu
            );
            host.setVmScheduler(new VmSchedulerTimeShared());
            hosts.add(host);
        }
        
        return hosts;
    }
    
    private static List<Pe> createCPU(int count, double mipsCapacity) {
        List<Pe> peList = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            peList.add(new PeSimple(mipsCapacity));
        }
        
        return peList;
    }
}

