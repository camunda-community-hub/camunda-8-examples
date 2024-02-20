package org.example.camunda.process.solution;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@EnableZeebeClient
@Deployment(resources = "classpath*:/models/*.*")
public class BackupRestoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackupRestoreApplication.class, args);
    }
}
