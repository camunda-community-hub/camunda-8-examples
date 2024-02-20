package org.example.camunda.process.solution.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(value = "backup")
@Component
@Data
public class BackupProperties {

    @NestedConfigurationProperty
    private EsBackupConfig zeebe;

    @NestedConfigurationProperty
    private EsBackupConfig operate;

    @NestedConfigurationProperty
    private EsBackupConfig tasklist;

    @NestedConfigurationProperty
    private EsBackupConfig optimize;

    private String elasticsearchUrl;
}
