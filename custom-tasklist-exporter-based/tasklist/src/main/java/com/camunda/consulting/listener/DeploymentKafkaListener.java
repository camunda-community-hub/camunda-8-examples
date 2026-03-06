package com.camunda.consulting.listener;

import com.camunda.consulting.api.service.DiagramService;
import com.camunda.consulting.impl.BpmnDiagram;
import java.util.logging.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeploymentKafkaListener {

  private final Logger LOGGER = Logger.getLogger(UserTaskKafkaListener.class.getName());


  private final DiagramService diagramService;

  public DeploymentKafkaListener(DiagramService diagramService) {
    this.diagramService = diagramService;
  }

  @KafkaListener(topics = "${tasklist.kafka.deployment-topic}")
  public void listen(BpmnDiagram diagram) {
    LOGGER.info("Received BpmnDiagram: " + diagram.getDiagramId());
    diagramService.save(diagram);
  }



}
