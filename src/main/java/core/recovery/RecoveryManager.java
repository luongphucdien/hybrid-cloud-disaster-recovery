package core.recovery;

import config.Config;
import core.backup.BackupRecord;
import core.backup.BackupRepository;
import core.security.IntegrityCheck;

import java.util.Optional;

public class RecoveryManager {
    private final BackupRepository repository;
    private final IntegrityCheck checker;
    
    public RecoveryManager(BackupRepository repository, IntegrityCheck checker) {
        this.repository = repository;
        this.checker = checker;
    }
    
    public RecoveryResult restore(int cloudletId, double failTime) {
        Optional<BackupRecord> backupExistence = repository.latestBefore(
                cloudletId,
                failTime
        );
        
        if(backupExistence.isEmpty()) {
            return new RecoveryResult(
                    cloudletId,
                    false,
                    -1,
                    "no-snapshot",
                    0
            );
        }
        
        BackupRecord backup = backupExistence.get();
        
        byte[] restored;
        if(backup.getPayloadSize() > 256) {
            restored = backup.getPayload();
        } else {
            restored = new byte[1024];
            System.arraycopy(
                    backup.getPayload(),
                    0,
                    restored,
                    0,
                    backup.getPayload().length
            );
        }
        
        boolean isIntact = checker.validate(
                backup,
                restored
        );
        
        double size = Math.max(
                1,
                restored.length / 1024
        ); // in Mb
        double transferTime = size / Config.BACKUP_BANDWIDTH;
        double recoveryEndTime = failTime + transferTime;
        
        return new RecoveryResult(
                cloudletId,
                isIntact,
                backup.getTimestamp(),
                isIntact ? "recovery-ok" : "recovery-corrupted",
                recoveryEndTime
        );
    }
}
