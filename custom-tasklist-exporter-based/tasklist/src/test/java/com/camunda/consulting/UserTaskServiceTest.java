package com.camunda.consulting;

import static org.junit.jupiter.api.Assertions.*;

import com.camunda.consulting.api.service.UserTaskService;
import com.camunda.consulting.impl.UserTask;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserTaskServiceTest {

  @Autowired
  private UserTaskService userTaskService;

  private static UserTask userTask = new UserTask();

  @BeforeAll
  public static void setup() {
    userTask.setUserTaskId("userTaskId");
    userTask.setProcessInstanceId("processInstanceId");
    userTask.setTaskElementName("taskElementName");
    userTask.setFormKey("formKey");
    userTask.setAssignee("assignee");
    userTask.setDueDate("dueDate");
    userTask.setFollowUpDate("followUpDate");
    userTask.setPriority(1);
    userTask.setVariables(Map.of("key", "value"));
    userTask.setSource("source");
    userTask.setEventType(EventType.CREATED);
  }
  @Test
  public void whenNotUsingPageWithAll() {
    // given
    assertEquals(0,userTaskService.allUserTasks(Optional.empty(),Optional.empty()).size());
    // when
    userTaskService.save(userTask);
    // then
    assertEquals(1,userTaskService.allUserTasks(Optional.empty(),Optional.empty()).size());
  }

  @Test
  public void whenUsingPageWithAll() {
    // given
    assertEquals(0,userTaskService.allUserTasks(Optional.of(0),Optional.of(10)).size());
    // when
    userTaskService.save(userTask);
    // then
    assertEquals(1,userTaskService.allUserTasks(Optional.of(0),Optional.of(10)).size());
  }

  @Test
  public void whenNotUsingPageWithFiltered() {
    // given
    assertEquals(0,
        userTaskService.filteredUserTasks(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(),
            List.of(), List.of(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).size());
    // when
    userTaskService.save(userTask);
    // then
    assertEquals(1,
        userTaskService.filteredUserTasks(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(),
            List.of(), List.of(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).size());
  }

  @Test
  public void whenUsingPageWithFiltered() {
    // given
    assertEquals(0,userTaskService.filteredUserTasks(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
        List.of(),List.of(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()).size());
    // when
    userTaskService.save(userTask);
    // then
    assertEquals(1,userTaskService.filteredUserTasks(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
        List.of(),List.of(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty()).size());
  }




}