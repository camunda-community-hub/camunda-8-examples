package org.camunda.consulting.example.services;

import java.util.Optional;

public interface EngineService {

  Object startInstance(String bpmnProcessId,String correlationKey, Optional<Object> payload, Optional<Integer> version);

  Object sendMessage(String messageName,String correlationKey, Optional<Object> payload);

}
