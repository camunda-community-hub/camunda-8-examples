package org.example.camunda.process.solution.model;

import lombok.Data;

import java.util.List;

@Data
public class BackupStatusResponse {
    private int backupId;
    private String state;
    private String failureReason;
    private List<Details> details;

    @Data
    public static class Details {
        private String snapshotName;
        private String state;
        private String startTime;
        private List<String> failures;
    }
}
