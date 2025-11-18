public class Config {
    public static String asString() {
        return String.format(
                "Datacenter Config%n" +
                        "PEs=%d | MIPS=%.1f%n" +
                        "Hosts=%d | RAM=%d | BW=%d | Storage=%d%n" +
                        "VM Config%n" +
                        "Number=%d | MIPS=%.1f%n" +
                        "PEs=%d | RAM=%d | BW=%d | Storage=%d%n" +
                        "Cloudlet Config%n" +
                        "Number=%d%n" +
                        "Length=%d | File Size=%d | Output Size=%d | PEs=%d%n",
                DatacenterConfig.PE_NUMBER,
                DatacenterConfig.PE_MIPS_CAPACITY,
                DatacenterConfig.HOST_NUMBER,
                DatacenterConfig.HOST_RAM,
                DatacenterConfig.HOST_BW,
                DatacenterConfig.HOST_STORAGE,
                VMConfig.NUMBER,
                VMConfig.MIPS_CAPACITY,
                VMConfig.PE_NUMBER,
                VMConfig.RAM,
                VMConfig.BW,
                VMConfig.STORAGE,
                CloudletConfig.NUMBER,
                CloudletConfig.LENGTH,
                CloudletConfig.FILE_SIZE,
                CloudletConfig.OUTPUT_SIZE,
                CloudletConfig.PE_NUMBER
        );
    }
    
    public static class DatacenterConfig {
        public static final int PE_NUMBER = 4;
        public static final double PE_MIPS_CAPACITY = 2000;
        public static final int HOST_NUMBER = 4;
        public static final long HOST_RAM = 1024 * 16, HOST_BW = 10_000, HOST_STORAGE = 1_000_000;
    }
    
    public static class VMConfig {
        public static final int NUMBER = 4;
        public static final double MIPS_CAPACITY = 1000;
        public static final long PE_NUMBER = 1, RAM = 1024 * 2, BW = 1000, STORAGE = 10_000;
    }
    
    public static class CloudletConfig {
        public static final int NUMBER = 8;
        public static final long LENGTH = 10_000, FILE_SIZE = 300, OUTPUT_SIZE = 300;
        public static final int PE_NUMBER = 1;
    }
}
