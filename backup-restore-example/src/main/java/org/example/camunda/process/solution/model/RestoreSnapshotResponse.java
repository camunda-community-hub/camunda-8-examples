package org.example.camunda.process.solution.model;

import lombok.Data;

import java.util.List;

@Data
public class RestoreSnapshotResponse {

    private InternalSnapshotResponse snapshot;

    @Data
    public static class InternalSnapshotResponse {
        private List<String> indices;
        private String snapshot;
        private Shards shards;
    }

    @Data
    public static class Shards {
        private int total;
        private int failed;
        private int successful;
    }
}
