package com.camunda.consulting.api.repository;

import com.camunda.consulting.impl.UserTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserTaskRepository extends MongoRepository<UserTask, String> {

  public Page<UserTask> findAll(Pageable pageable);

}
