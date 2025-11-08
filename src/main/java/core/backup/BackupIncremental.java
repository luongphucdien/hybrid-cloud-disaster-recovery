package core.backup;

import core.WorkloadData;
import org.cloudsimplus.core.CloudSimPlus;
import utils.Utils;

import java.util.List;
import java.util.Map;

public class BackupIncremental extends BackupManager {
    public BackupIncremental(BackupRepository repository, Map<Integer, WorkloadData> dataMap,
                             CloudSimPlus simulation) {
        super(repository, dataMap, simulation);
    }
    
    @Override
    public void backup(int cloudletId) {
        WorkloadData workloadData = this.dataMap.get(cloudletId);
        if(workloadData == null) {
            return;
        }
        
        List<BackupRecord> existingRecords = repository.get(cloudletId);
        byte[] payload = workloadData.getPayload();
        byte[] stored;
        
        if(existingRecords.isEmpty()) {
            stored = payload.clone();
        } else {
            int length = Math.max(1, payload.length / 10);
            stored = new byte[length];
            System.arraycopy(payload, 0, stored, 0, length);
        }
        
        String checksum = Utils.ChecksumCalc(payload);
        BackupRecord record = new BackupRecord(stored, checksum, simulation.clock());
        repository.add(cloudletId, record);
        
        System.out.printf(this.toString(cloudletId, record));
    }
}
