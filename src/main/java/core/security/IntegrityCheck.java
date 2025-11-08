package core.security;

import core.backup.BackupRecord;
import core.backup.BackupRepository;
import utils.Utils;

public class IntegrityCheck {
    private final BackupRepository repository;
    
    public IntegrityCheck(BackupRepository repository) {
        this.repository = repository;
    }
    
    public boolean validate(BackupRecord backup, byte[] restoredPayload) {
        String restoredChecksum = Utils.ChecksumCalc(restoredPayload);
        return restoredChecksum != null && restoredChecksum.equals(backup.getChecksum());
    }
}
