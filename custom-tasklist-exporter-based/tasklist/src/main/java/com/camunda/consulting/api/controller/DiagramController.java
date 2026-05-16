package com.camunda.consulting.api.controller;

import com.camunda.consulting.api.service.DiagramService;
import com.camunda.consulting.impl.BpmnDiagram;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DiagramController {

  @Autowired
  private DiagramService diagramService;

  @QueryMapping
  public Optional<BpmnDiagram> diagramById(@Argument String diagramId) {
    return diagramService.diagramById(diagramId);
  }

}
