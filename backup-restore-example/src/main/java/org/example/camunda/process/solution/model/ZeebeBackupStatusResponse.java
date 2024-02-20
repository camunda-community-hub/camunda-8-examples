package org.example.camunda.process.solution.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZeebeBackupStatusResponse {
    List<InnterZeebeBackupStatusResponse> snapshots;

    @Data
    public static class InnterZeebeBackupStatusResponse {
        private String snapshot;
        private String repository;
        private String state;
    }
}
