package core.backup;

public class BackupRecord {
    private final byte[] payload;
    private final String checksum;
    private final double timestamp;
    
    public BackupRecord(byte[] payload, String checksum, double timestamp) {
        this.payload = payload.clone();
        this.checksum = checksum;
        this.timestamp = timestamp;
    }
    
    public int getPayloadSize() {
        return payload.length;
    }
    
    public byte[] getPayload() {
        return payload.clone();
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public double getTimestamp() {
        return timestamp;
    }
}
