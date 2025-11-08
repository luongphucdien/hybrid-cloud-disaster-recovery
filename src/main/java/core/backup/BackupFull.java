package core.backup;

import core.WorkloadData;
import org.cloudsimplus.core.CloudSimPlus;

import java.util.Map;

public class BackupFull extends BackupManager {
    public BackupFull(BackupRepository repository, Map<Integer, WorkloadData> dataMap,
                      CloudSimPlus simulation) {
        super(repository, dataMap, simulation);
    }
    
    @Override
    public void backup(int cloudletId) {
        WorkloadData workloadData = dataMap.get(cloudletId);
        if(workloadData == null) {
            return;
        }
        
        BackupRecord record = new BackupRecord(workloadData.getPayload(),
                workloadData.getChecksum(), simulation.clock());
        repository.add(cloudletId, record);
        
        System.out.printf(this.toString(cloudletId, record));
    }
}
