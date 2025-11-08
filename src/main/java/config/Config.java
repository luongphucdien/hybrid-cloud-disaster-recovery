package config;

public final class Config {
    // HOST CONFIG
    public static final int PRIVATE_HOSTS = 3;
    public static final int PUBLIC_HOSTS = 6;
    public static final int HOST_CORES = 2;
    public static final int HOST_MIPS = 2_000;
    public static final long HOST_RAM = 1024 * 8;
    public static final long HOST_BW = 10_000;
    public static final long HOST_STORAGE = 1_000_000;
    
    // VM CONFIG
    public static final int VM_COUNT = 6;
    public static final int VM_CORES = 1;
    public static final int VM_MIPS = 1000;
    public static final long VM_RAM = 1024 * 2;
    public static final long VM_BW = 1000;
    public static final long VM_STORAGE = 10_000;
    
    // CLOUDLET CONFIG
    public static final int CLOUDLET_COUNT = 12;
    public static final int CLOUDLET_CORES = 1;
    public static final long CLOUDLET_FILE_SIZE = 300;
    public static final long CLOUDLET_OUTPUT_SIZE = 300;
    
    // BACKUP CONFIG
    public static final double BACKUP_INTERVAL_CRITICAL = 2;
    public static final double BACKUP_INTERVAL_HIGH = 4;
    public static final double BACKUP_INTERVAL_NORMAL = 8;
    public static final double BACKUP_BANDWIDTH = 50; // in Mbps
    public static final int BACKUP_COUNT = 4;
    
    // COST CONFIG
    public static final double STORAGE_COST = .00005; // $ per Mb
    public static final double TRANSFER_COST = .0001; // $ per Mb
    public static final double COMPUTE_COST = .00002; // $ per sec
    
    // SIMULATION TIME CONFIG
    public static final double SIMULATION_TIME = 300;
    
    private Config() {}
}
