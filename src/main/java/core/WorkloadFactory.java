package core;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import utils.Utils;

import java.util.*;

public final class WorkloadFactory {
    private WorkloadFactory() {}
    
    public static List<CloudletInfo> createCloudletInfos(
            int count, int cores, long fileSize,
            long outputSize
    ) {
        List<CloudletInfo> cloudletInfos = new ArrayList<>();
        
        for(int i = 0; i < count; i++) {
            long length = 4000 + (i % 5) * 1500;
            CloudletSimple cloudlet = new CloudletSimple(
                    length,
                    cores
            );
            cloudlet
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(new UtilizationModelFull());
            
            CloudletInfo.PriorityLevel priorityLevel;
            switch(i % 4) {
                case 0 -> priorityLevel = CloudletInfo.PriorityLevel.CRITICAL;
                case 1 -> priorityLevel = CloudletInfo.PriorityLevel.HIGH;
                case 2 -> priorityLevel = CloudletInfo.PriorityLevel.NORMAL;
                default -> priorityLevel = CloudletInfo.PriorityLevel.LOW;
            }
            
            cloudletInfos.add(new CloudletInfo(
                    cloudlet,
                    priorityLevel
            ));
        }
        
        return cloudletInfos;
    }
    
    public static Map<Integer, WorkloadData> createWorkloads(List<CloudletInfo> cloudletInfos) {
        Map<Integer, WorkloadData> workloads = new HashMap<Integer, WorkloadData>();
        Random rand = new Random(69420);
        
        for(CloudletInfo cloudletInfo : cloudletInfos) {
            Cloudlet cloudlet = cloudletInfo.getCloudlet();
            byte[] payload = new byte[1024];
            rand.nextBytes(payload);
            String checksum = Utils.ChecksumCalc(payload);
            WorkloadData workloadData = new WorkloadData(
                    payload,
                    checksum
            );
            workloads.put(
                    (int) cloudlet.getId(),
                    workloadData
            );
        }
        
        return workloads;
    }
}
