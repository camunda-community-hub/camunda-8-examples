package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.example.camunda.process.solution.service.StateConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class StateWorker {

    @Autowired
    private StateConversationRepository stateConversationRepository;

  @JobWorker(type = "request-state-task", autoComplete = true)
  public Map<String, Object> requestState(ActivatedJob activatedJob) {
    Map<String, Object> processVariables = activatedJob.getVariablesAsMap();

    String myId = (String) processVariables.get("myId");

    CompletableFuture<Map<String, Object>> conversation = stateConversationRepository.getConversation(myId);

    // push result via `complete` into CompletableFuture
    if (conversation != null) {
      conversation.complete(processVariables);
      stateConversationRepository.removeConversation(myId);
    } else {
      System.out.println("conversation is null for myId=" + myId);
    }

    return processVariables;
  }
}
