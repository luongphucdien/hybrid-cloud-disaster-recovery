package core.backup;

import core.WorkloadData;
import org.cloudsimplus.core.CloudSimPlus;
import utils.Utils;

import java.util.Map;

public abstract class BackupManager {
    protected final BackupRepository repository;
    protected final Map<Integer, WorkloadData> dataMap;
    protected final CloudSimPlus simulation;
    
    public BackupManager(BackupRepository repository, Map<Integer, WorkloadData> dataMap,
                         CloudSimPlus simulation) {
        this.repository = repository;
        this.dataMap = dataMap;
        this.simulation = simulation;
    }
    
    public void backup(int cloudletId) {}
    
    public String toString(int cloudletId, BackupRecord record) {
        return String.format("<Backup> Cloudlet: %d | Time: %s | Checksum: %s | Size: %dB%n",
                cloudletId, Utils.ReadableTime(simulation.clock()), record.getChecksum(),
                record.getPayloadSize());
    }
}
