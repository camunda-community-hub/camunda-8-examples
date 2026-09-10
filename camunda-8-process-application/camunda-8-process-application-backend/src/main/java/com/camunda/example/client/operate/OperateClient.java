package com.camunda.example.client.operate;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
@Getter
public class OperateClient {
  private final OperateProcessDefinitionsEndpoint processDefinitionsEndpoint;
  private final OperateProcessInstancesEndpoint processInstancesEndpoint;
  private final OperateIncidentsEndpoint incidentsEndpoint;
  private final OperateFlowNodeInstancesEndpoint flowNodeInstancesEndpoint;
  private final OperateVariablesEndpoint variablesEndpoint;

  public OperateClient(
      OperateProcessDefinitionsEndpoint processDefinitionsEndpoint,
      OperateProcessInstancesEndpoint processInstancesEndpoint,
      OperateIncidentsEndpoint incidentsEndpoint,
      OperateFlowNodeInstancesEndpoint flowNodeInstancesEndpoint,
      OperateVariablesEndpoint variablesEndpoint
  ) {
    this.processDefinitionsEndpoint = processDefinitionsEndpoint;
    this.processInstancesEndpoint = processInstancesEndpoint;
    this.incidentsEndpoint = incidentsEndpoint;
    this.flowNodeInstancesEndpoint = flowNodeInstancesEndpoint;
    this.variablesEndpoint = variablesEndpoint;
  }


}
