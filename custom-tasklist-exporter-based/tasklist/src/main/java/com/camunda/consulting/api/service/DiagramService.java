package com.camunda.consulting.api.service;

import com.camunda.consulting.api.repository.DiagramRepository;
import com.camunda.consulting.impl.BpmnDiagram;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DiagramService {

  private final DiagramRepository diagramRepository;

  public DiagramService(DiagramRepository diagramRepository) {
    this.diagramRepository = diagramRepository;
  }

  public void save(BpmnDiagram diagram) {
    diagramRepository.save(diagram);
  }

  public Optional<BpmnDiagram> diagramById(String id) {
    return diagramRepository.findById(id);
  }

}
