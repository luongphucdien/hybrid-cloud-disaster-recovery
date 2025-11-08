import config.Config;
import core.*;
import core.backup.BackupFull;
import core.backup.BackupIncremental;
import core.backup.BackupManager;
import core.backup.BackupRepository;
import core.recovery.RecoveryManager;
import core.recovery.RecoveryResult;
import core.security.IntegrityCheck;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisasterRecovery {
    public static void main(String[] args) throws Exception {
        System.out.println("==========BEGIN DISASTER RECOVERY SIMULATION===========");
        CloudSimPlus simulation = new CloudSimPlus();
        
        // Create Datacenters
        List<Host> privateHosts = HostFactory.createHosts(
                Config.PRIVATE_HOSTS,
                Config.HOST_CORES,
                Config.HOST_MIPS,
                Config.HOST_RAM,
                Config.HOST_BW,
                Config.HOST_STORAGE
        );
        
        List<Host> publicHosts = HostFactory.createHosts(
                Config.PUBLIC_HOSTS,
                Config.HOST_CORES,
                Config.HOST_MIPS,
                Config.HOST_RAM,
                Config.HOST_BW,
                Config.HOST_STORAGE
        );
        
        DatacenterSimple privateDC = DatacenterFactory.createDatacenter(
                simulation,
                privateHosts,
                "private_datacenter"
        );
        DatacenterSimple publicDC = DatacenterFactory.createDatacenter(
                simulation,
                publicHosts,
                "public_datacenter"
        );
        
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // Workloads and data
        List<CloudletInfo> cloudletInfos = WorkloadFactory.createCloudletInfos(
                Config.CLOUDLET_COUNT,
                Config.CLOUDLET_CORES,
                Config.CLOUDLET_FILE_SIZE,
                Config.CLOUDLET_OUTPUT_SIZE
        );
        List<Cloudlet> cloudlets = new ArrayList<>();
        for(CloudletInfo cloudletInfo : cloudletInfos) {
            cloudlets.add(cloudletInfo.getCloudlet());
        }
        
        // VMs
        List<Vm> vms = VMFactory.createVMs(
                Config.VM_COUNT,
                Config.VM_MIPS,
                Config.VM_CORES,
                Config.VM_RAM,
                Config.VM_BW,
                Config.VM_STORAGE
        );
        
        // Submit
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);
        
        // Backup and recovery
        Map<Integer, WorkloadData> dataMap = WorkloadFactory.createWorkloads(cloudletInfos);
        
        BackupRepository repository = new BackupRepository();
        BackupManager fullBackup = new BackupFull(
                repository,
                dataMap,
                simulation
        );
        BackupManager incrementalBackup = new BackupIncremental(
                repository,
                dataMap,
                simulation
        );
        
        IntegrityCheck checker = new IntegrityCheck(repository);
        RecoveryManager recoveryManager = new RecoveryManager(
                repository,
                checker
        );
        
        MetricsLogger logger = new MetricsLogger();
        
        // Backup pipeline
        for(CloudletInfo cloudletInfo : cloudletInfos) {
            int id = (int) cloudletInfo
                    .getCloudlet()
                    .getId();
            CloudletInfo.PriorityLevel priorityLevel = cloudletInfo.getPriority();
            
            double interval;
            switch(priorityLevel) {
                case CRITICAL -> interval = Config.BACKUP_INTERVAL_CRITICAL;
                case HIGH -> interval = Config.BACKUP_INTERVAL_HIGH;
                default -> interval = Config.BACKUP_INTERVAL_NORMAL;
            }
            
            for(int i = 0; i < Config.BACKUP_COUNT; i++) {
                double backupInterval = interval * i;
                simulation.addOnClockTickListener((sim) -> {
                    if(Math.abs(sim.getTime() - backupInterval) < 1) {
                        if(priorityLevel == CloudletInfo.PriorityLevel.CRITICAL) {
                            fullBackup.backup(id);
                        } else {
                            incrementalBackup.backup(id);
                        }
                    }
                });
            }
            
        }
        
        // Disaster Simulation
        double failTime = 12;
        System.out.printf(
                "==========DISASTER STARTED (T = %s)==========%n",
                Utils.ReadableTime(failTime)
        );
        
        simulation.addOnClockTickListener((sim) -> {
            double delta = Math.abs(sim.getTime() - failTime);
            if(delta < 0.5) {
                System.out.println("<DISASTER> Private DC outage at t = " +
                        Utils.ReadableTime(sim.getTime()));
                // Simulate disaster by disabling hosts
                privateDC
                        .getHostList()
                        .forEach(host -> host.setActive(false));
                
                // Restoration
                for(CloudletInfo cloudletInfo : cloudletInfos) {
                    int id = (int) cloudletInfo
                            .getCloudlet()
                            .getId();
                    
                    RecoveryResult recoveryResult = recoveryManager.restore(
                            id,
                            sim.getTime()
                    );
                    
                    double RPO = recoveryResult.getSnapshotTime() >= 0 ? sim.getTime() -
                            recoveryResult.getSnapshotTime() : Double.NaN;
                    double RTO = recoveryResult.getRecoveryEndTime() - sim.getTime();
                    
                    // Cost Estimation
                    //                double size = 1 * Math.max(1,1024)
                    logger.add(
                            id,
                            sim.getTime(),
                            recoveryResult.getSnapshotTime(),
                            recoveryResult.isIntegrityOk(),
                            RPO,
                            RTO,
                            recoveryResult.getStatus(),
                            0
                    );
                    
                    System.out.println(logger);
                }
                
                //                double failbackTime = sim.getTime() + 30;
                //                simulation.addOnClockTickListener(simFailback -> {
                //                    if(Math.abs(simFailback.getTime() - failbackTime) < 1e-3) {
                //                        System.out.println("<FAILBACK> Reinstate private DC at
                //                        t = " +
                //                                Utils.ReadableTime(simFailback.getTime()));
                //                        privateDC
                //                                .getHostList()
                //                                .addAll(HostFactory.createHosts(
                //                                        Config.PRIVATE_HOSTS,
                //                                        Config.HOST_CORES,
                //                                        Config.HOST_MIPS,
                //                                        Config.HOST_RAM,
                //                                        Config.HOST_BW,
                //                                        Config.HOST_STORAGE
                //                                ));
                //                    }
                //                });
            }
        });
        
        // Run Simulation
        simulation.start();
        
        System.out.printf(
                "==========SIMULATION END (T = %s)==========%n)",
                Utils.ReadableTime(simulation.clock())
        );
        
        logger.writeToFile("./metrics.csv");
    }
}
