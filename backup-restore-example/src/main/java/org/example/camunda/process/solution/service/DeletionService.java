package org.example.camunda.process.solution.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.config.BackupProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DeletionService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BackupProperties backupProperties;


    @SneakyThrows
    public void deleteIndices() {
        List<String> prefixes = List.of("zeebe-record-", "operate", "optimize", "tasklist");

        for (String prefix : prefixes) {
            log.info("Going to delete prefix {}", prefix);
            ResponseEntity<String> indicesResponse = restTemplate.getForEntity(backupProperties.getElasticsearchUrl() + "/_cat/indices/" + prefix + "*", String.class);

            String indices = indicesResponse.getBody();
            String csvIndices = "";
            List<String> lines = Arrays.asList(indices.split("\n"));
            for (String line : lines) {
                String s = line.split(" ")[2];
                csvIndices += s + ",";
            }

            URI uri = new URI(backupProperties.getElasticsearchUrl() + "/" + csvIndices);
            restTemplate.delete(uri);
            log.info("Deleted prefix {}", prefix);
        }


    }
}
