package com.camunda.consulting;

import com.camunda.consulting.impl.BpmnDiagram;

public interface DeployEventExporter {

  void exportDeployEvent(BpmnDiagram diagram);

}
