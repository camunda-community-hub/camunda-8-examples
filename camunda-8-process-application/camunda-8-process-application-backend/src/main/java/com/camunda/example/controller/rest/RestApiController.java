package com.camunda.example.controller.rest;

import com.camunda.example.controller.rest.model.*;
import com.camunda.example.service.business.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;

import java.util.*;
import java.util.stream.*;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class RestApiController {
  private final InsuranceApplicationService insuranceApplicationService;

  @PostMapping("insurance-application/create")
  public InsuranceApplicationIdDto startInstance(@RequestBody CreateInsuranceApplicationDto requestDto) {
    return insuranceApplicationService.create(requestDto);
  }

  @GetMapping("insurance-application")
  public Page<InsuranceApplicationDto> list(Pageable pageable) {
    return insuranceApplicationService
        .page(pageable);
  }

  @DeleteMapping("insurance-application/id/{id}")
  public ResponseEntity<InsuranceApplicationDto> delete(@PathVariable("id") String id) {
    return ResponseEntity.of(insuranceApplicationService.delete(id));
  }

  @GetMapping("insurance-application/id/{id}")
  public ResponseEntity<InsuranceApplicationDto> get(@PathVariable("id") String id) {
    return ResponseEntity.of(insuranceApplicationService.getDto(id));
  }
}
