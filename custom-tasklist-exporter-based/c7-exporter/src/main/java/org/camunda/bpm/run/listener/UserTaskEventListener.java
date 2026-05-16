package org.camunda.bpm.run.listener;

import com.camunda.consulting.EventType;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.run.EventExporter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserTaskEventListener {

  private final Logger LOGGER = Logger.getLogger(UserTaskEventListener.class.getName());

  @EventListener(condition = "#delegateTask.eventName.equals('create')")
  public void taskCreated(DelegateTask delegateTask) {
    LOGGER.finest("Task Created: " + delegateTask.getTaskDefinitionKey());
    EventExporter.getUserTaskProducer().sendMessage(delegateTask, EventType.CREATED);
  }

  @EventListener(condition = "#delegateTask.eventName.equals('delete')")
  public void taskDeleted(DelegateTask delegateTask) {
    LOGGER.finest("Task Deleted: " + delegateTask.getTaskDefinitionKey());
    EventExporter.getUserTaskProducer().sendMessage(delegateTask, EventType.ENDED);
  }

  @EventListener(condition = "#delegateTask.eventName.equals('complete')")
  public void taskCompleted(DelegateTask delegateTask) {
    LOGGER.finest("Task Completed: " + delegateTask.getTaskDefinitionKey());
    EventExporter.getUserTaskProducer().sendMessage(delegateTask, EventType.COMPLETED);
  }

}
