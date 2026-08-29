package com.camunda.consulting;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.camunda.consulting.api.service.UserTaskService;
import com.camunda.consulting.impl.UserTask;
import com.camunda.consulting.listener.UserTaskKafkaListener;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserTaskKafkaListenerTest {

  @Autowired
  private UserTaskService userTaskService;
  private static UserTask userTask = new UserTask();
  private static UserTaskKafkaListener userTaskKafkaListener;

  private static String userTaskId = "userTaskId";

  @BeforeEach
  public void setup() {
    userTask.setUserTaskId(userTaskId);
    userTask.setProcessInstanceId("processInstanceId");
    userTask.setTaskElementName("taskElementName");
    userTask.setFormKey("formKey");
    userTask.setAssignee("assignee");
    userTask.setDueDate("dueDate");
    userTask.setFollowUpDate("followUpDate");
    userTask.setPriority(1);
    userTask.setVariables(Map.of("key", "value"));
    userTask.setSource("source");
    userTaskKafkaListener = new UserTaskKafkaListener(this.userTaskService);
  }

  @Test
  public void whenNewUserTaskReceivedCreate() {
    // given
    userTask.setEventType(EventType.CREATED);
    assertTrue(userTaskService.userTaskById(userTaskId).isEmpty());
    // when
    userTaskKafkaListener.listen(userTask);
    // then
    assertTrue(userTaskService.userTaskById(userTaskId).isPresent());

  }

  @Test
  public void whenUserTaskUpdated() {
    //given
    userTask.setEventType(EventType.CREATED);
    userTaskKafkaListener.listen(userTask);
    assertEquals(1,userTaskService.filteredUserTasks(Optional.of(userTaskId),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
        List.of(),List.of(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()).size());
    // when
    userTask.setEventType(EventType.COMPLETED);
    userTaskKafkaListener.listen(userTask);
    // then
    assertEquals(1,userTaskService.filteredUserTasks(Optional.of(userTaskId),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
        List.of(),List.of(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()).size());
    assertEquals(EventType.COMPLETED,userTaskService.userTaskById(userTaskId).get().getEventType());
  }

}
