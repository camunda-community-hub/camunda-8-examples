package io.camunda.processStateQuery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.dto.FlownodeInstance;
import io.camunda.operate.dto.Incident;
import io.camunda.operate.dto.ProcessDefinition;
import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.Variable;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.FlownodeInstanceFilter;
import io.camunda.operate.search.IncidentFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.VariableFilter;
import io.camunda.processStateQuery.ProcessStateDto.ElementInstanceDto;
import io.camunda.processStateQuery.ProcessStateDto.IncidentDto;
import io.camunda.processStateQuery.ProcessStateDto.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
public class ProcessStateController {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessStateController.class);
  private final CamundaOperateClient camundaOperateClient;
  private final ObjectMapper objectMapper;

  @Autowired
  public ProcessStateController(CamundaOperateClient camundaOperateClient, ObjectMapper objectMapper) {
    this.camundaOperateClient = camundaOperateClient;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/process-states/{key}")
  public ProcessStateDto getProcessState(@PathVariable("key") long key) throws OperateException {
    LOG.info("Fetching process instance with key {}", key);
    ProcessInstance processInstance = camundaOperateClient.getProcessInstance(key);
    ProcessDefinition processDefinition = camundaOperateClient.getProcessDefinition(processInstance.getProcessDefinitionKey());
    List<FlownodeInstance> flownodeInstances = camundaOperateClient.searchFlownodeInstances(new SearchQuery.Builder()
        .filter(new FlownodeInstanceFilter.Builder()
            .processInstanceKey(processInstance.getKey())
            .build())
        .build());
    List<Incident> incidents = camundaOperateClient.searchIncidents(new SearchQuery.Builder()
        .filter(new IncidentFilter.Builder()
            .processInstanceKey(processInstance.getKey())
            .build())
        .build());
    List<Variable> variables = camundaOperateClient
        .searchVariables(new SearchQuery.Builder()
            .filter(new VariableFilter.Builder()
                .processInstanceKey(processInstance.getKey())
                .build())
            .build())
        .stream()
        .map(this::getVariable)
        .toList();
    return new ProcessStateDto(
        processInstance.getKey(),
        processInstance.getProcessDefinitionKey(),
        processInstance.getBpmnProcessId(),
        processDefinition.getName(),
        buildElementInstances(flownodeInstances, variables),
        buildIncidents(incidents),
        buildVariables(variables, processInstance.getKey()),
        processInstance
            .getState()
            .toString(),
        fromDate(processInstance.getStartDate()),
        fromDate(processInstance.getEndDate())
    );
  }

  private List<IncidentDto> buildIncidents(List<Incident> incidents) {
    return incidents
        .stream()
        .map(i -> new IncidentDto(i.getKey(), i.getMessage(), i.getState(), fromDate(i.getCreationTime())))
        .toList();
  }

  private Variable getVariable(Variable variable) {
    try {
      return variable.getTruncated() ? camundaOperateClient.getVariable(variable.getKey()) : variable;
    } catch (OperateException e) {
      throw new RuntimeException(e);
    }
  }

  private List<ElementInstanceDto> buildElementInstances(
      List<FlownodeInstance> flownodeInstances, List<Variable> variables
  ) {
    return flownodeInstances
        .stream()
        .map(fni -> buildElementInstance(fni, variables))
        .toList();
  }

  private ElementInstanceDto buildElementInstance(FlownodeInstance flownodeInstance, List<Variable> variables) {
    return new ElementInstanceDto(
        flownodeInstance.getKey(),
        flownodeInstance.getFlowNodeId(),
        flownodeInstance.getFlowNodeName(),
        buildVariables(variables, flownodeInstance.getKey()),
        flownodeInstance
            .getState()
            .toString(),
        fromDate(flownodeInstance.getStartDate()),
        fromDate(flownodeInstance.getEndDate())
    );
  }

  private List<VariableDto> buildVariables(List<Variable> variables, Long scopeKey) {
    return variables
        .stream()
        .filter(v -> v
            .getScopeKey()
            .equals(scopeKey))
        .map(v -> new VariableDto(v.getKey(), v.getName(), getVariableValue(v)))
        .toList();
  }

  private JsonNode getVariableValue(Variable variable) {
    try {
      return objectMapper.readTree(variable.getValue());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private LocalDateTime fromDate(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
