package com.camunda.example.service.business;

import com.camunda.example.client.operate.*;
import com.camunda.example.client.operate.model.*;
import com.camunda.example.client.zeebe.model.*;
import com.camunda.example.controller.rest.model.*;
import com.camunda.example.repository.*;
import com.camunda.example.repository.model.*;
import com.camunda.example.service.business.model.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import io.camunda.zeebe.client.*;
import io.camunda.zeebe.client.api.response.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceApplicationService {
  private final InsuranceApplicationRepository insuranceApplicationRepository;
  private final ZeebeClient zeebeClient;
  private final OperateClient operateClient;
  private final ObjectMapper objectMapper;
  private final Random random = new Random();

  public InsuranceApplicationIdDto create(CreateInsuranceApplicationDto dto) {
    InsuranceApplicationEntity entity = initEntity(dto);
    InsuranceApplicationVariables variables = initVariables(dto, entity);
    ProcessInstanceEvent event = startApplicationProcess(variables);
    return createIdResponse(saveEntity(entity, event));
  }

  public Page<InsuranceApplicationDto> page(Pageable pageable) {
    return insuranceApplicationRepository
        .findAll(pageable)
        .map(this::fromEntity);
  }

  public Optional<InsuranceApplicationDto> getDto(String id) {
    return getEntity(id).map(this::fromEntity);
  }

  public Optional<InsuranceApplicationEntity> getEntity(String id) {
    return insuranceApplicationRepository.findById(id);
  }

  public Optional<InsuranceApplicationDto> delete(String id) {
    return insuranceApplicationRepository
        .findById(id)
        .map(entity -> {
          try {
            operateClient
                .getProcessInstancesEndpoint()
                .delete(entity.getProcessInstanceKey());
            insuranceApplicationRepository.delete(entity);
          } catch (ErrorDto e) {
            log.error("Error while deleting insurance application", e);
            if (e
                .getStatus()
                .equals("404")) {
              insuranceApplicationRepository.delete(entity);
            }
          }
          return fromEntity(entity);
        });
  }

  public boolean setApplicationState(String id, String state) {
    return insuranceApplicationRepository
        .findById(id)
        .map(entity -> {
          entity.setApplicationState(state);
          return insuranceApplicationRepository.save(entity);
        })
        .isPresent();
  }

  private InsuranceApplicationDto fromEntity(InsuranceApplicationEntity entity) {
    log.debug("Creating dto from entity {}",entity.getId());
    InsuranceApplicationVariables variables = getProcessVariables(entity, true);
    InsuranceApplicationProcessState processState = getProcessState(entity);
    InsuranceApplicationDto dto = createResponse(entity, variables, processState);
    log.debug("Created dto from entity {}",entity.getId());
    return dto;
  }

  private InsuranceApplicationProcessState getProcessState(InsuranceApplicationEntity entity) {
    log.debug("Getting process state {}",entity.getId());
    InsuranceApplicationProcessState state = new InsuranceApplicationProcessState();
    try {
      ProcessInstanceDto processInstance = operateClient
          .getProcessInstancesEndpoint()
          .get(entity.getProcessInstanceKey());
      state.setProcessState(processInstance.getState());
    } catch (ErrorDto e) {
      log.error("Error while fetching process state", e);
      state.setProcessState("UNKNOWN");
    }
    log.debug("Got process state {}",entity.getId());
    return state;
  }

  @SneakyThrows
  private InsuranceApplicationVariables getProcessVariables(InsuranceApplicationEntity entity, boolean enforceFind) {
    log.debug("Getting process variables {}",entity.getId());
    operateClient
        .getProcessInstancesEndpoint()
        .get(entity.getProcessInstanceKey());
    VariableDto example = new VariableDto();
    example.setProcessInstanceKey(entity.getProcessInstanceKey());
    Set<VariableDto> processVariables = new HashSet<>();
    while (enforceFind && processVariables.isEmpty()) {
      processVariables.addAll(queryProcessVariables(example).getItems());
    }
    Map<String, JsonNode> mappedVariables = processVariables
        .stream()
        .map(processVariable -> {
          String value = processVariable.getValue();
          if (processVariable.getTruncated()) {
            value = operateClient
                .getVariablesEndpoint()
                .get(processVariable.getKey())
                .getValue();
          }
          try {
            return Map.entry(processVariable.getName(), objectMapper.readTree(processVariable.getValue()));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    InsuranceApplicationVariables variables = objectMapper.treeToValue(objectMapper.valueToTree(mappedVariables), InsuranceApplicationVariables.class);
    log.debug("Got process variables {}",entity.getId());
    return variables;
  }

  private ResultsDto<VariableDto> queryProcessVariables(VariableDto example) {
    log.debug("Query process variables");
    ResultsDto<VariableDto> dto = operateClient
        .getVariablesEndpoint()
        .search(QueryDto
            .<VariableDto>builder()
            .filter(example)
            .size(999L)
            .build());
    log.debug("Queried process variables");
    return dto;
  }

  private InsuranceApplicationEntity initEntity(CreateInsuranceApplicationDto dto) {
    log.debug("Initializing entity");
    InsuranceApplicationEntity entity = new InsuranceApplicationEntity();
    int bound = 10000;
    String id = "A-" + random.nextInt(1000, bound);
    while (insuranceApplicationRepository.existsById(id)) {
      bound +=1;
      id = "A-" + random.nextInt(1000, bound);
    }
    entity.setId(id);
    entity.setApplicantName(dto.getName());
    entity.setEmail(dto.getEmail());
    entity.setApplicationState("Pending");
    log.debug("Initialized entity {}",entity.getId());
    return entity;
  }

  private InsuranceApplicationVariables initVariables(
      CreateInsuranceApplicationDto dto, InsuranceApplicationEntity entity
  ) {
    log.debug("Initializing variables {}",entity.getId());
    InsuranceApplicationVariables variables = new InsuranceApplicationVariables();
    variables.setAge(dto.getAge());
    variables.setVehicleManufacturer(dto.getVehicleManufacturer());
    variables.setVehicleModel(dto.getVehicleModel());
    variables.setApplicationId(entity.getId());
    log.debug("Initialized variables {}",entity.getId());
    return variables;
  }

  private ProcessInstanceEvent startApplicationProcess(InsuranceApplicationVariables variables) {
    log.debug("Starting application process {}",variables.getApplicationId());
    ProcessInstanceEvent processInstanceEvent = zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("InsuranceApplication")
        .latestVersion()
        .variables(variables)
        .send()
        .join();
    log.debug("Started Application process {}",variables.getApplicationId());
    return processInstanceEvent;
  }

  private InsuranceApplicationEntity saveEntity(InsuranceApplicationEntity entity, ProcessInstanceEvent event) {
    log.debug("Saving entity {}",entity.getId());
    entity.setProcessInstanceKey(event.getProcessInstanceKey());
    entity.setProcessDefinitionKey(event.getProcessDefinitionKey());
    entity = insuranceApplicationRepository.save(entity);
    log.debug("Entity saved {}",entity.getId());
    return entity;
  }

  private InsuranceApplicationDto createResponse(
      InsuranceApplicationEntity entity,
      InsuranceApplicationVariables variables,
      InsuranceApplicationProcessState processState
  ) {
    log.debug("Creating response {}",entity.getId());
    InsuranceApplicationDto dto = new InsuranceApplicationDto();
    dto.setApplicantName(entity.getApplicantName());
    dto.setVehicleModel(variables.getVehicleModel());
    dto.setId(entity.getId());
    dto.setApplicantAge(variables.getAge());
    dto.setVehicleManufacturer(variables.getVehicleManufacturer());
    dto.setProcessState(processState.getProcessState());
    dto.setApplicationState(entity.getApplicationState());
    dto.setEmail(entity.getEmail());
    log.debug("Created response {}",entity.getId());
    return dto;
  }

  private InsuranceApplicationIdDto createIdResponse(
      InsuranceApplicationEntity entity
  ) {
    log.debug("Creating id response {}",entity.getId());
    InsuranceApplicationIdDto dto = new InsuranceApplicationIdDto();
    dto.setId(entity.getId());
    log.debug("Created id response {}",entity.getId());
    return dto;
  }

}
