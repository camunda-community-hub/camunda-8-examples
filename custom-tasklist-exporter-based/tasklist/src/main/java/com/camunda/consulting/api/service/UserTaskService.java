package com.camunda.consulting.api.service;

import com.camunda.consulting.EventType;
import com.camunda.consulting.api.graphql.UserTaskDTO;
import com.camunda.consulting.api.repository.UserTaskRepository;
import com.camunda.consulting.impl.CompletedTaskMessage;
import com.camunda.consulting.impl.UserTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserTaskService {

  @Autowired
  private UserTaskRepository userTaskRepository;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  private ObjectMapper mapper = new ObjectMapper();

  private static final int DEFAULT_PAGE_SIZE = 25;

  public Optional<UserTaskDTO> userTaskByIdDto(String userTaskId) {
    return userTaskRepository.findById(userTaskId).map(UserTaskDTO::new);

  }

  public Optional<UserTask> userTaskById(String userTaskId) {
    return userTaskRepository.findById(userTaskId);

  }

  public List<UserTaskDTO> allUserTasks(Optional<Integer> page, Optional<Integer> size) {
    if (page.isPresent() && size.isPresent()) {
      return userTaskRepository.findAll(PageRequest.of(page.get(), size.get())).stream()
          .map(UserTaskDTO::new).collect(java.util.stream.Collectors.toList());
    }
    return userTaskRepository.findAll().stream().map(UserTaskDTO::new)
        .collect(java.util.stream.Collectors.toList());
  }

  public List<UserTaskDTO> filteredUserTasks(
      @Argument Optional<String> userTaskId,
      @Argument Optional<String> processInstanceId,
      @Argument Optional<String> taskElementName,
      @Argument Optional<String> formKey,
      @Argument Optional<String> assignee,
      @Argument List<String> candidateUsers,
      @Argument List<String> candidateGroups,
      @Argument Optional<String> dueDate,
      @Argument Optional<String> followUpDate,
      @Argument Optional<Integer> priority,
      @Argument Optional<String> source,
      @Argument Optional<EventType> eventType,
      @Argument Optional<Integer> page,
      @Argument Optional<Integer> size) {

    Query query = new Query();
    if (userTaskId.isPresent()) {
      query.addCriteria(Criteria.where("userTaskId").is(userTaskId.get()));
    }
    if (processInstanceId.isPresent()) {
      query.addCriteria(Criteria.where("processInstanceId").is(processInstanceId.get()));
    }
    if (taskElementName.isPresent()) {
      query.addCriteria(Criteria.where("taskElementName").is(taskElementName.get()));
    }
    if (formKey.isPresent()) {
      query.addCriteria(Criteria.where("formKey").is(formKey.get()));
    }
    if (assignee.isPresent()) {
      query.addCriteria(Criteria.where("assignee").is(assignee.get()));
    }
    if (candidateUsers != null && !candidateUsers.isEmpty()) {
      query.addCriteria(Criteria.where("candidateUsers").in(candidateUsers));
    }
    if (candidateGroups != null && !candidateGroups.isEmpty()) {
      query.addCriteria(Criteria.where("candidateGroups").in(candidateGroups));
    }
    if (dueDate.isPresent()) {
      query.addCriteria(Criteria.where("dueDate").is(dueDate.get()));
    }
    if (followUpDate.isPresent()) {
      query.addCriteria(Criteria.where("followUpDate").is(followUpDate.get()));
    }
    if (priority.isPresent()) {
      query.addCriteria(Criteria.where("priority").is(priority.get()));
    }

    if (source.isPresent()) {
      query.addCriteria(Criteria.where("source").is(source.get()));
    }
    if (eventType.isPresent()) {
      query.addCriteria(Criteria.where("eventType").is(eventType.get()));
    }

    if (page.isPresent() && size.isPresent()) {
      query.with(PageRequest.of(page.get(), size.get()));
    } else {
      query.with(PageRequest.of(0, DEFAULT_PAGE_SIZE));
    }

    return mongoTemplate.find(query, UserTask.class).stream().map(UserTaskDTO::new)
        .collect(java.util.stream.Collectors.toList());

  }


  public void save(UserTask userTask) {
    userTaskRepository.save(userTask);
  }

  public UserTaskDTO completeUserTask(String userTaskId, String variables) {
    Optional<UserTask> userTask = userTaskRepository.findById(userTaskId);
    if (userTask.isPresent() && userTask.get().getEventType() == EventType.CREATED) {
      UserTask task = userTask.get();
      String topic = task.getSource();
      String id;
      if (task.getJobKey() != null) {
        id = task.getJobKey();
      } else {
        id = task.getUserTaskId();
      }
      CompletedTaskMessage message = new CompletedTaskMessage();
      message.setId(id);
      try {
        message.setVariables(mapper.readValue(variables, Map.class));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      kafkaTemplate.send(topic, message);
      return new UserTaskDTO(userTask.get());
    }
    return null;
  }

  public UserTaskDTO assignUserTask(String userTaskId, String assignee) {
    Optional<UserTask> userTask = userTaskRepository.findById(userTaskId);
    if (userTask.isPresent()) {
      userTask.get().setAssignee(assignee);
      userTaskRepository.save(userTask.get());
      return new UserTaskDTO(userTask.get());
    }
    return null;
  }
}
