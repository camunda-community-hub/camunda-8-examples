package org.camunda.bpm.run.producer;

import com.camunda.consulting.DeployEventExporter;
import com.camunda.consulting.EventType;
import com.camunda.consulting.TaskEventExporter;
import com.camunda.consulting.impl.BpmnDiagram;
import com.camunda.consulting.impl.UserTask;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.kafka.core.KafkaTemplate;

public class UserTaskProducer implements TaskEventExporter, DeployEventExporter {

  private final String sourceId;
  private final String userTaskTopic;
  private final String deployTopic;
  private final KafkaTemplate<String, Object> template;

  public UserTaskProducer(String sourceId, String userTaskTopic, String deployTopic, KafkaTemplate<String, Object> template) {
    this.sourceId = sourceId;
    this.userTaskTopic = userTaskTopic;
    this.deployTopic = deployTopic;
    this.template = template;
  }

  @Override
  public void exportTaskEvent(UserTask userTask) {
    this.template.send(userTaskTopic, userTask.getUserTaskId(), userTask);
  }

  public void sendMessage(DelegateTask delegateTask, EventType eventType) {
    String dueDate = delegateTask.getDueDate() != null ? delegateTask.getDueDate().toString() : null;
    String followUpDate = delegateTask.getFollowUpDate() != null ? delegateTask.getFollowUpDate().toString() : null;
    UserTask task = UserTask.builder()
        .userTaskId(delegateTask.getId())
        .source(sourceId)
        .eventType(eventType)
        .processInstanceId(delegateTask.getProcessInstanceId())
        .processDefinitionId(delegateTask.getProcessDefinitionId())
        .taskElementName(delegateTask.getTaskDefinitionKey())
        .formKey(delegateTask.getBpmnModelElementInstance().getCamundaFormKey())
        .assignee(delegateTask.getAssignee())
        .candidateGroups(delegateTask.getBpmnModelElementInstance().getCamundaCandidateGroupsList())
        .candidateUsers(delegateTask.getBpmnModelElementInstance().getCamundaCandidateUsersList())
        .dueDate(dueDate)
        .followUpDate(followUpDate)
        .priority(delegateTask.getPriority())
        .variables(delegateTask.getVariables())
        .build();
    exportTaskEvent(task);
  }

  @Override
  public void exportDeployEvent(BpmnDiagram bpmnDiagram) {
    this.template.send(deployTopic, bpmnDiagram.getDiagramId(), bpmnDiagram);
  }
}
