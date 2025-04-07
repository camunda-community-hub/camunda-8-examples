package org.example.camunda.process.solution.service;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class StateService {

    @Autowired
    private StateConversationRepository stateConversationRepository;

    @Autowired
    private ZeebeClient zeebeClient;

    public CompletableFuture<Map<String, Object>> getState(String myId) {
        return zeebeClient
                .newPublishMessageCommand()
                .messageName("RequestStateMessage")
                .correlationKey(myId)
                .send()
                .thenCompose(
                        publishMessageResponse -> {
                            // create CompletableFuture that gets completed via conversation.complete in StateWorker
                            CompletableFuture<Map<String, Object>> response = new CompletableFuture<>();
                            stateConversationRepository.addConversation(myId, response);
                            return response;
                        })
                // timeout if JobWorker doesn't complete within 5 seconds
                .toCompletableFuture().orTimeout(5, TimeUnit.SECONDS);
    }
}
