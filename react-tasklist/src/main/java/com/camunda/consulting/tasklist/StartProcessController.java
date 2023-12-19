package com.camunda.consulting.tasklist;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class StartProcessController {
  private final RestTemplate camundaWebhookClient;

  public StartProcessController(
      @Qualifier("camundaWebhookClient") RestTemplate camundaWebhookClient) {
    this.camundaWebhookClient = camundaWebhookClient;
  }

  @PostMapping("/start-process")
  public ResponseEntity<Void> startProcess(@RequestBody Map<String, Object> body) {
    camundaWebhookClient.postForEntity("/startTheProcess", body, Void.class);
    return ResponseEntity.status(204).build();
  }
}
