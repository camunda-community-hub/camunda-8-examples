package org.camunda.bpm.run.listener;

import com.camunda.consulting.impl.BpmnDiagram;
import java.util.logging.Logger;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.run.EventExporter;

public class BpmnDeployListener implements Deployer {

  private final Logger LOGGER = Logger.getLogger(BpmnDeployListener.class.getName());

  @Override
  public void deploy(DeploymentEntity deploymentEntity) {

    deploymentEntity.getDeployedProcessDefinitions().forEach(processDefinitionEntity -> {
      String id = processDefinitionEntity.getId();
      ResourceEntity entity = deploymentEntity.getResource(
          processDefinitionEntity.getResourceName());
      String processDefinition = new String(entity.getBytes());
      BpmnDiagram diagram = new BpmnDiagram(id, processDefinition);
      LOGGER.info("Process deployed: " + id);
      EventExporter.getUserTaskProducer().exportDeployEvent(diagram);
    });
  }
}
