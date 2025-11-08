package core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MetricsLogger {
    private final List<String> lines = new ArrayList<>();
    
    public MetricsLogger() {
        lines.add("Cloudlet ID,Fail Time,Snapshot Time,Integrity,RPO,RTO,Status,Estimated Cost");
    }
    
    public synchronized void add(
            int cloudletId, double failTime, double snapshotTime, boolean integrity, double rpo,
            double rto, String status, double estimatedCost
    ) {
        lines.add(String.format(
                "%d, %.3f, %.3f, %b, %.3f, %.3f, %s, %.6f",
                cloudletId,
                failTime,
                snapshotTime,
                integrity,
                rpo,
                rto,
                status,
                estimatedCost
        ));
    }
    
    public void writeToFile(String filename) throws IOException {
        try(PrintWriter writer = new PrintWriter(filename)) {
            for(String line : lines) {
                writer.println(line);
            }
        }
    }
    
    public String toString() {
        return String.join(
                "\n",
                lines
        );
    }
}
