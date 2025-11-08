package core.recovery;

public class RecoveryResult {
    private final int cloudletId;
    private final boolean integrityOk;
    private final double snapshotTime;
    private final String status;
    private final double recoveryEndTime;
    
    public RecoveryResult(
            int cloudletId, boolean integrityOk, double snapshotTime, String status,
            double recoveryEndTime
    ) {
        this.cloudletId = cloudletId;
        this.integrityOk = integrityOk;
        this.snapshotTime = snapshotTime;
        this.status = status;
        this.recoveryEndTime = recoveryEndTime;
    }
    
    public int getCloudletId() {
        return cloudletId;
    }
    
    public boolean isIntegrityOk() {
        return integrityOk;
    }
    
    public double getSnapshotTime() {
        return snapshotTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public double getRecoveryEndTime() {
        return recoveryEndTime;
    }
    
}
