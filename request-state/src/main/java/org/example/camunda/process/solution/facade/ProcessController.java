package org.example.camunda.process.solution.facade;

import io.camunda.zeebe.client.ZeebeClient;
import org.example.camunda.process.solution.service.StateConversationRepository;
import org.example.camunda.process.solution.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/process")
public class ProcessController {


    @Autowired
    private ZeebeClient zeebe;

    @Autowired
    private StateService stateService;

    private final Map<String, CompletableFuture<Map<String, Object>>> map = new HashMap<>();
    @Autowired
    private StateConversationRepository stateConversationRepository;

    @PostMapping("/start")
  public void startProcessInstance(@RequestBody Map<String, Object> variables) {
    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId("request-state-process")
        .latestVersion()
        .variables(variables)
        .send();
  }

    @GetMapping("/state/{myId}")
    public CompletableFuture<Map<String, Object>> getState(@PathVariable String myId) {
        try {
            return stateService.getState(myId);
        } catch (Exception e) {
            stateConversationRepository.removeConversation(myId);
            throw new RuntimeException(e);
        }
    }

}
