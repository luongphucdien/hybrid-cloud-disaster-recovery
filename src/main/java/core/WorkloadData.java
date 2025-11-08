package core;

public class WorkloadData {
    private final byte[] payload;
    private final String checksum;
    
    public WorkloadData(byte[] payload, String checksum) {
        this.payload = payload.clone();
        this.checksum = checksum;
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
}
