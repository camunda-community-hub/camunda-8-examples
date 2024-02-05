package com.camunda.consulting;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericTests {

  @ParameterizedTest
  @ValueSource(strings = {"/c8-to-c8-instance_migration.bpmn"})
  public void thereIsAWorkerForEachTaskTypeInModel(String bpmnFileName) {
    Class<?> workerClass = Main.class;
    Set<String> jobTypesInModel = getJobTypesFromModel(bpmnFileName);
    assert(Arrays.stream(workerClass.getDeclaredMethods())
        .map(method -> {
          if (method.isAnnotationPresent(JobWorker.class)) {
            String type = method.getAnnotation(JobWorker.class).type();
            return type.equals("") ? method.getName() : type;
          }
          return null;
        }).collect(Collectors.toSet())
        .containsAll(jobTypesInModel));
  }

  private Set<String> getJobTypesFromModel(String bpmnFile) {
    BpmnModelInstance bpmn = Bpmn.readModelFromStream(this.getClass().getResourceAsStream(bpmnFile));
    return bpmn.getModelElementsByType(ZeebeTaskDefinition.class).stream()
        .map(ZeebeTaskDefinition::getType)
        .collect(Collectors.toSet());
  }

}
