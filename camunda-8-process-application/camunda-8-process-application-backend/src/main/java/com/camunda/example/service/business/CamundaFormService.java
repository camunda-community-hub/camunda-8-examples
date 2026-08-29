package com.camunda.example.service.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CamundaFormService {
  private final Map<String, String> forms = new HashMap<>();
  private final ObjectMapper objectMapper;

  @PostConstruct
  public void init() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath*:**.form");
    Arrays
        .stream(resources)
        .forEach(resource -> {
          try (InputStream in = resource.getInputStream()) {
            JsonNode jsonNode = objectMapper.readTree(in);
            String id = jsonNode
                .get("id")
                .textValue();
            log.info("Found form with id '{}' in {}",id,resource.getURL());
            forms.put(id, jsonNode.toString());
          } catch (Exception e) {
            log.error("Error while importing resource", e);
          }
        });
  }

  public String getSchema(String id) {
    return forms.get(id);
  }
}
