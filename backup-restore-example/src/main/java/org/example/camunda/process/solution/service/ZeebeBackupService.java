package org.example.camunda.process.solution.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.config.EsBackupConfig;
import org.example.camunda.process.solution.model.ZeebeBackupStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ZeebeBackupService {

    @Autowired
    private RestTemplate restTemplate;

    public void pauseExporting(EsBackupConfig esBackupConfig) {
        ResponseEntity<String> pauseZeebeExporting = restTemplate.postForEntity(esBackupConfig.getUrl() + "/actuator/exporting/pause", new HashMap<>(), String.class);
        log.info("pause zeebe export response: {}, {}", pauseZeebeExporting.getStatusCodeValue(), pauseZeebeExporting.getBody());
    }

    public void resumeExporting(EsBackupConfig esBackupConfig) {
        ResponseEntity<String> zeebeResumeResponse = restTemplate.postForEntity(esBackupConfig.getUrl() + "/actuator/exporting/resume", new HashMap<>(), String.class);
        log.info("resume zeebe export response: {}", zeebeResumeResponse.getStatusCodeValue());
    }

    public void triggerEsBackup(String esRepository, int backupId, String elasticsearchUrl) {
        HttpEntity<?> zeebeEsBackupRequest = new HttpEntity<>(Map.of("indices", "zeebe-record*", "feature_states", List.of("none")));
        ResponseEntity<String> zeebeEsBackupResponse = restTemplate.exchange(elasticsearchUrl + "/_snapshot/" + esRepository + "/" + backupId, HttpMethod.PUT, zeebeEsBackupRequest, String.class);
        log.info("trigger zeebe es backup response: {}, {}", zeebeEsBackupResponse.getStatusCodeValue(), zeebeEsBackupResponse.getBody());
    }

    @SneakyThrows
    public void checkEsBackupStatus(String elasticsearchUrl, String esRepository) {

        ResponseEntity<ZeebeBackupStatusResponse> backupStatusResponse = null;
        do {
            log.info("checking backup status for url {}", elasticsearchUrl);
            backupStatusResponse = restTemplate.getForEntity(elasticsearchUrl + "/_snapshot/" + esRepository + "/_status?pretty", ZeebeBackupStatusResponse.class);
            Thread.sleep(100L);
            log.info("backup status response for url {}: {}, {}", elasticsearchUrl, backupStatusResponse.getStatusCodeValue(), backupStatusResponse.getBody());

        } while (backupStatusResponse.getStatusCode().is2xxSuccessful() &&
                backupStatusResponse.getBody() != null && !backupStatusResponse.getBody().getSnapshots().get(0).getState().equals("SUCCESS")

        );

    }
}
