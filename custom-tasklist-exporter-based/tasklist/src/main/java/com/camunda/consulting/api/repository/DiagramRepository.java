package com.camunda.consulting.api.repository;

import com.camunda.consulting.impl.BpmnDiagram;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiagramRepository extends MongoRepository<BpmnDiagram, String> {

}
