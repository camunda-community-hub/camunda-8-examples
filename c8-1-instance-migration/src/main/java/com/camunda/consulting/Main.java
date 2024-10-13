package com.camunda.consulting;

import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.dto.ProcessDefinition;
import io.camunda.operate.dto.ProcessInstance;
import io.camunda.operate.dto.ProcessInstanceState;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.search.ProcessDefinitionFilter;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import io.camunda.operate.search.VariableFilter;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class);
  }

  private static Logger LOG = LoggerFactory.getLogger(Main.class);

  @Autowired
  ZeebeClient client;
  @Autowired
  CamundaOperateClient operate;

  @JobWorker
  public Map<String, Object> fetchInstances(@Variable String bpmnProcessId) throws OperateException {
    List<Long> processInstanceIds = new ArrayList<>();
    ProcessDefinitionFilter filter = new ProcessDefinitionFilter.Builder().bpmnProcessId(bpmnProcessId).build();
    Sort sort = new Sort("version", SortOrder.DESC);
    SearchQuery query = new SearchQuery.Builder().filter(filter).sort(sort).build();
    List<ProcessDefinition> definitions = operate.searchProcessDefinitions(query);
    LOG.info("Found {} versions of process {}", definitions.size(), bpmnProcessId);
    if (definitions.size() >= 2) {
      ProcessInstanceFilter instanceFilter = new ProcessInstanceFilter.Builder().bpmnProcessId(bpmnProcessId).processVersion(definitions.get(1).getVersion()).state(
          ProcessInstanceState.ACTIVE).build();
      processInstanceIds = operate.searchProcessInstances(new SearchQuery.Builder().filter(instanceFilter).build()).stream()
          .map(ProcessInstance::getKey).toList();
    }
    LOG.info("{} process instances will be migrated to latest version", processInstanceIds.size());
    return Map.of("processInstanceKeys", processInstanceIds);
  }

  @JobWorker
  public Map<String, Object> startInstance(@Variable Long processInstanceKey, @Variable String bpmnProcessId, @Variable String startBeforeElement)
      throws OperateException {
    Map<String, Object> variables = new HashMap<>();
    VariableFilter filter = new VariableFilter.Builder().processInstanceKey(processInstanceKey).build();
    List<io.camunda.operate.dto.Variable> vars = operate.searchVariables(new SearchQuery.Builder().filter(filter).build());
    vars.forEach(var -> {
      if (var.getTruncated()) {
        try {
          io.camunda.operate.dto.Variable fullVar = operate.getVariable(var.getKey());
          variables.put(fullVar.getName(), fullVar.getValue());
        } catch (OperateException e) {
          throw new RuntimeException(e);
        }
      } else {
        variables.put(var.getName(), var.getValue());
      }
    });
    variables.put("oldInstanceKey", processInstanceKey);
    ProcessInstanceEvent newInstance = client.newCreateInstanceCommand()
        .bpmnProcessId(bpmnProcessId)
        .latestVersion()
        .startBeforeElement(startBeforeElement)
        .variables(variables)
        .send()
        .join();
    LOG.info("Migration of instance {} completed. New instance ID is {}.", processInstanceKey, newInstance.getProcessInstanceKey());
    return Map.of("newInstanceKey", newInstance.getProcessInstanceKey());
  }

  @JobWorker
  public void deleteOldInstance(@Variable Long processInstanceKey) {
    client.newCancelInstanceCommand(processInstanceKey).send().join();
    LOG.info("Instance {} has been cancelled, because it has been migrated to a new process model version.", processInstanceKey);
  }
}