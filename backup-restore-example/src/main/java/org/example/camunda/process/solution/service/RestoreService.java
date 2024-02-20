package org.example.camunda.process.solution.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.config.BackupProperties;
import org.example.camunda.process.solution.config.EsBackupConfig;
import org.example.camunda.process.solution.model.BackupRestoreRequest;
import org.example.camunda.process.solution.model.RestoreSnapshotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RestoreService {

    @Autowired
    private BackupProperties backupProperties;

    @Autowired
    private RestTemplate restTemplate;

    public void restore(BackupRestoreRequest restoreRequest) {
        // Restore the state of Operate, Tasklist, and Optimize.
        restoreSnapshots(restoreRequest.getBackupId(), backupProperties.getOperate());
        restoreSnapshots(restoreRequest.getBackupId(), backupProperties.getTasklist());
        restoreSnapshots(restoreRequest.getBackupId(), backupProperties.getOptimize());

        // Restore zeebe-records* indices from Elasticsearch snapshot.
        restoreZeebeIndices(restoreRequest, backupProperties.getZeebe());

        // Restore Zeebe (via HELM init container)

        // Start Zeebe, Operate, Tasklist, and Optimize.

        // Profit!
    }

    @SneakyThrows
    private void restoreZeebeIndices(BackupRestoreRequest restoreRequest, EsBackupConfig zeebe) {
        log.info("Attempting to restore snapshot: {}", restoreRequest.getBackupId());
        ResponseEntity<RestoreSnapshotResponse> restoreSnapshotResponse = restTemplate.postForEntity(
                backupProperties.getElasticsearchUrl() + "/_snapshot/" + zeebe.getEsRepository() + "/" + restoreRequest.getBackupId() + "/_restore?wait_for_completion=true", null, RestoreSnapshotResponse.class);
        if(!restoreSnapshotResponse.getStatusCode().is2xxSuccessful() || restoreSnapshotResponse.getBody().getSnapshot().getShards().getFailed() > 0) {
            log.error("Restore has failed for {}", restoreSnapshotResponse.getBody());
            throw new RuntimeException("Restore has failed for zeebe");
        }
        log.info("Restored snapshot: {}", restoreRequest.getBackupId());
    }

    @SneakyThrows
    private void restoreSnapshots(int backupId, EsBackupConfig webComponentConfig) {
        // read snapshots
        BufferedReader reader =
                new BufferedReader(new FileReader("backup-restore-example/backups/" + backupId + "/snapshots_" + webComponentConfig.getComponent()));
        String line = reader.readLine();
        List<String> snapshotsList = Arrays.asList(line.split(","));

        //restore snapshots
        for(String snapshot: snapshotsList) {
            log.info("Attempting to restore snapshot: {}", snapshot);
            ResponseEntity<RestoreSnapshotResponse> restoreSnapshotResponse = restTemplate.postForEntity(
                    backupProperties.getElasticsearchUrl() + "/_snapshot/" + webComponentConfig.getEsRepository() + "/" + snapshot + "/_restore?wait_for_completion=true", null, RestoreSnapshotResponse.class);
            if(!restoreSnapshotResponse.getStatusCode().is2xxSuccessful() || restoreSnapshotResponse.getBody().getSnapshot().getShards().getFailed() > 0) {
                log.error("Restore has failed for {}", restoreSnapshotResponse.getBody());
                throw new RuntimeException("Restore has failed for " +  webComponentConfig);
            }
            log.info("Restored snapshot: {}", snapshot);
        }
    }
}
