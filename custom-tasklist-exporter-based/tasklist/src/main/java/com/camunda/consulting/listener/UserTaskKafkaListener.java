package com.camunda.consulting.listener;

import com.camunda.consulting.EventType;
import com.camunda.consulting.api.service.UserTaskService;
import com.camunda.consulting.impl.UserTask;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserTaskKafkaListener {


  private final Logger LOGGER = Logger.getLogger(UserTaskKafkaListener.class.getName());

  private final UserTaskService userTaskService;

  public UserTaskKafkaListener(UserTaskService userTaskService) {
    this.userTaskService = userTaskService;
  }

  @KafkaListener(topics = "${tasklist.kafka.user-task-topic}")
  public void listen(UserTask userTask) {
    try {
      LOGGER.fine("Received UserTask: " + userTask);
      String taskId = userTask.getUserTaskId();
      switch (userTask.getEventType()) {
        case CREATED:
          userTaskService.save(userTask);
          break;
        case COMPLETED:
          updateTask(taskId, EventType.COMPLETED);
          break;
        case ENDED:
          updateTask(taskId, EventType.ENDED);
          break;
        default:
          LOGGER.warning("Received Message for unknown Event Type: " + userTask.getEventType());
      }
    } catch (Exception e) {
      LOGGER.severe("Error while processing UserTask: " + userTask + " - " + e.getMessage());
    }
  }

  private void updateTask(String taskId, EventType eventType) {
    userTaskService.userTaskById(taskId).ifPresentOrElse(
        task -> {
          task.setEventType(eventType);
          userTaskService.save(task);
        },
        () -> {
          LOGGER.warning("Task not found: " + taskId);
        }
    );
  }

}
