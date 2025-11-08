package core.backup;

import java.util.*;

public class BackupRepository {
    private final Map<Integer, List<BackupRecord>> backups = new HashMap<>();
    
    public synchronized void add(int cloudletId, BackupRecord record) {
        backups
                .computeIfAbsent(cloudletId, k -> new ArrayList<>())
                .add(record);
    }
    
    public synchronized List<BackupRecord> get(int cloudletId) {
        return backups.getOrDefault(cloudletId, Collections.emptyList());
    }
    
    public synchronized Optional<BackupRecord> latestBefore(int cloudletId, double time) {
        return get(cloudletId)
                .stream()
                .filter(record -> record.getTimestamp() <= time)
                .max(Comparator.comparingDouble(BackupRecord::getTimestamp));
    }
}
