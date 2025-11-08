package core;

import org.cloudsimplus.cloudlets.Cloudlet;

public class CloudletInfo {
    private final Cloudlet cloudlet;
    private final PriorityLevel priority;
    
    public CloudletInfo(Cloudlet cloudlet, PriorityLevel priority) {
        this.cloudlet = cloudlet;
        this.priority = priority;
    }
    
    public Cloudlet getCloudlet() {
        return cloudlet;
    }
    
    public PriorityLevel getPriority() {
        return priority;
    }
    
    public enum PriorityLevel {
        CRITICAL, HIGH, NORMAL, LOW
    }
}
