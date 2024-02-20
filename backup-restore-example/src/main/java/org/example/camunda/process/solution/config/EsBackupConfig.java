package org.example.camunda.process.solution.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class EsBackupConfig {
    private String url;
    private String esBucket;
    private String esRegion;
    private String esRepository;
    private String component;
}
