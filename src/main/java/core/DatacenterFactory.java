package core;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;

import java.util.List;

public final class DatacenterFactory {
    private DatacenterFactory() {}
    
    public static DatacenterSimple createDatacenter(
            CloudSimPlus simulation, List<Host> hosts,
            String name
    ) {
        DatacenterSimple datacenter = new DatacenterSimple(
                simulation,
                hosts
        );
        datacenter.setName(name);
        return datacenter;
    }
}
