package org.example.camunda.process.solution.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.config.BackupProperties;
import org.example.camunda.process.solution.config.EsBackupConfig;
import org.example.camunda.process.solution.model.BackupRestoreRequest;
import org.example.camunda.process.solution.model.BackupStatusResponse;
import org.example.camunda.process.solution.model.RegisterRepositoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class BackupService {

    @Autowired
    private BackupProperties backupProperties;

    @Autowired
    private ZeebeBackupService zeebeBackupService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Prerequisite: keystore has s3.client.default.access_key & s3.client.default.secret_key 0.
     * prepare elasticsearch for Operate, Tasklist(, Optimize?) backups
     */
    public void prepareBackup() {
        // reload security settings (elasticsearch keystore)
        ResponseEntity<String> reloadSecuritySettingsResponse = restTemplate.postForEntity(backupProperties.getElasticsearchUrl() + "/_nodes/reload_secure_settings?pretty", null, String.class);
        log.info("Reloading security settings. Response: {}, {}", reloadSecuritySettingsResponse.getStatusCodeValue(), reloadSecuritySettingsResponse.getBody());

        // register es repos
        this.registerRepo(backupProperties.getZeebe(), backupProperties.getElasticsearchUrl());
        this.registerRepo(backupProperties.getOperate(), backupProperties.getElasticsearchUrl());
        this.registerRepo(backupProperties.getTasklist(), backupProperties.getElasticsearchUrl());
        this.registerRepo(backupProperties.getOptimize(), backupProperties.getElasticsearchUrl());
    }

    @SneakyThrows
    public void backup(BackupRestoreRequest backupRequest) throws InterruptedException {
        // create directory for files
        Files.createDirectories(Paths.get("backup-restore-example/backups/" + backupRequest.getBackupId()));

        // 1. Trigger a backup x of Optimize. See how to take an Optimize backup.
        this.triggerBackup(backupProperties.getOptimize().getUrl(), backupRequest.getBackupId());

        // 2. Trigger a backup x of Operate. See how to take an Operate backup.
        this.triggerBackup(backupProperties.getOperate().getUrl(), backupRequest.getBackupId());

        // 3. Trigger a backup x of Tasklist. See how to take a Tasklist backup.
        this.triggerBackup(backupProperties.getTasklist().getUrl(), backupRequest.getBackupId());

        // 4. Wait until the backup x of Optimize is complete. See how to monitor an Optimize backup.
        this.checkBackupStatus(backupProperties.getOptimize().getComponent(), backupProperties.getOptimize().getUrl(), backupRequest.getBackupId());

        // 5. Wait until the backup x of Operate is complete. See how to monitor an Operate backup.
        this.checkBackupStatus(backupProperties.getOperate().getComponent(), backupProperties.getOperate().getUrl(), backupRequest.getBackupId());

        // 6. Wait until the backup x of Tasklist is complete. See how to monitor a Tasklist backup.
        this.checkBackupStatus(backupProperties.getTasklist().getComponent(), backupProperties.getTasklist().getUrl(), backupRequest.getBackupId());

        // 7. Pause exporting in Zeebe. See Zeebe management API.
        zeebeBackupService.pauseExporting(backupProperties.getZeebe());

        // 8. Take a backup x of the exported Zeebe records in Elasticsearch using the Elasticsearch
        // Snapshots API.
        zeebeBackupService.triggerEsBackup(backupProperties.getZeebe().getEsRepository(), backupRequest.getBackupId(), backupProperties.getElasticsearchUrl());

        // monitor zeebe ES backup status
        zeebeBackupService.checkEsBackupStatus(backupProperties.getElasticsearchUrl(), backupProperties.getZeebe().getEsRepository());

        // 9. Take a backup x of Zeebe. See how to take a Zeebe backup. Wait until the backup x of the
        // exported Zeebe records is complete and wait until the backup x of Zeebe is completed before
        // proceeding. See how to monitor a Zeebe backup.
        this.triggerBackup(backupProperties.getZeebe().getUrl(), backupRequest.getBackupId());
        this.checkBackupStatus(backupProperties.getZeebe().getComponent(), backupProperties.getZeebe().getUrl(), backupRequest.getBackupId());

        // 10. Resume exporting in Zeebe. See Zeebe management API.
        zeebeBackupService.resumeExporting(backupProperties.getZeebe());

        log.info("==================");
        log.info("Backup {} completed", backupRequest.getBackupId());
    }

    public void registerRepo(EsBackupConfig esBackupConfig, String elasticsearchUrl) {
        Map<String, Object> registerRepoRequest = Map.of("type", "s3", "settings", Map.of("bucket", esBackupConfig.getEsBucket(), "region", esBackupConfig.getEsRegion()));
        ResponseEntity<RegisterRepositoryResponse> registerRepoResponse = restTemplate.postForEntity(elasticsearchUrl + "/_snapshot/" + esBackupConfig.getEsRepository(), registerRepoRequest, RegisterRepositoryResponse.class);
        log.info("register repo response for url={}: {}, {}", esBackupConfig.getUrl(), registerRepoResponse.getStatusCodeValue(), registerRepoResponse.getBody());
    }

    @SneakyThrows
    public void checkBackupStatus(String component, String url, int backupId) {
        boolean written = false;
        ResponseEntity<BackupStatusResponse> backupStatusResponse = null;
        do {
            log.info("checking backup status for url {}", url);
            Thread.sleep(10_000L);
            backupStatusResponse = restTemplate.getForEntity(url + "/actuator/backups/" + backupId, BackupStatusResponse.class);
            log.info("backup status response for url {}: {}, {}", url, backupStatusResponse.getStatusCodeValue(), backupStatusResponse.getBody());

            if(!written) {
                writeSnapshotNames(backupId, component, backupStatusResponse.getBody());
                written = true; //only write once (this can surely be done better :-))
            }

        } while (backupStatusResponse.getStatusCode().is2xxSuccessful() && backupStatusResponse.getBody() != null && !backupStatusResponse.getBody().getState().equals("COMPLETED"));

    }

    static void writeSnapshotNames(int backupId, String component, BackupStatusResponse backupStatusResponse) throws IOException {
        String csvSnapshotNames = "";
        BufferedWriter writer = new BufferedWriter(new FileWriter("backup-restore-example/backups/" +backupId +"/snapshots_" + component));
        for(int i = 0; i< backupStatusResponse.getDetails().size(); i++) {
            csvSnapshotNames += backupStatusResponse.getDetails().get(i).getSnapshotName() + ",";
        }
        log.info("snapshot names for {}: {}", component, csvSnapshotNames);
        writer.write(csvSnapshotNames);
        writer.close();
    }

    @SneakyThrows
    public void triggerBackup(String url, int backupId) {
        Map<String, Object> backupBody = Map.of("backupId", backupId);
        ResponseEntity<String> backupResponse = restTemplate.postForEntity(url + "/actuator/backups", backupBody, String.class);
        log.info("backup status response for url {}: {}, {}", url, backupResponse.getStatusCodeValue(), backupResponse.getBody());
    }
}
