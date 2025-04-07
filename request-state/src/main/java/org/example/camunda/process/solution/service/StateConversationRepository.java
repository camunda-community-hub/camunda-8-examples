package org.example.camunda.process.solution.service;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class StateConversationRepository {

  private final ConcurrentMap<String, CompletableFuture<Map<String, Object>>> conversations =
      new ConcurrentHashMap<>();

  public void addConversation(
      String myId, CompletableFuture<Map<String, Object>> conversation) {
    conversations.put(myId, conversation);
  }

  public CompletableFuture<Map<String, Object>> getConversation(String myId) {
    return conversations.get(myId);
  }

  public void removeConversation(String myId) {
    conversations.remove(myId);
  }
}
