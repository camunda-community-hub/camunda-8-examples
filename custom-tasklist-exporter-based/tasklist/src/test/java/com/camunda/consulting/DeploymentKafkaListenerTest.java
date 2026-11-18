package com.camunda.consulting;

import static org.junit.jupiter.api.Assertions.*;

import com.camunda.consulting.api.service.DiagramService;
import com.camunda.consulting.impl.BpmnDiagram;
import com.camunda.consulting.listener.DeploymentKafkaListener;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeploymentKafkaListenerTest {

  @Autowired
  private DiagramService diagramService;

  private final static String DIAGRAM_ID = "diagramId";

  @Test
  public void whenDeployedAddToKafka() {
    // given
    DeploymentKafkaListener listener = new DeploymentKafkaListener(diagramService);
    BpmnDiagram bpmnDiagram = new BpmnDiagram();
    bpmnDiagram.setDiagramId(DIAGRAM_ID);
    bpmnDiagram.setProcessDefinition("<xml></xml>");
    // when
    listener.listen(bpmnDiagram);
    // then
    BpmnDiagram diagram = diagramService.diagramById(DIAGRAM_ID).get();
    assertNotNull(diagram);
    assertTrue(EqualsBuilder.reflectionEquals(bpmnDiagram, diagram));
  }

}
