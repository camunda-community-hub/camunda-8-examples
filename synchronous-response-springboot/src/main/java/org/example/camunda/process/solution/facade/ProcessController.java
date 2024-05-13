package org.example.camunda.process.solution.facade;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.spring.client.annotation.value.ZeebeWorkerValue;
import io.camunda.zeebe.spring.client.jobhandling.JobWorkerManager;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/process")
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  public static final String BPMN_PROCESS_ID = "responseProcess";

  @Autowired private ZeebeClient zeebe;

  @Autowired private JobWorkerManager jobWorkerManager;

  @PostMapping("/start")
  public Mono<String> startProcessInstance() {
    LOG.info("Starting process `" + BPMN_PROCESS_ID + "`");
    String requestId = UUID.randomUUID().toString();
    Map<String, String> variables = Collections.singletonMap("requestId", requestId);

    // TODO: Add exceptionally
    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId(BPMN_PROCESS_ID)
        .latestVersion()
        .variables(variables)
        .send();

    // TODO: Think about exception handling here as well
    // TODO: Where to define timeout exactly?
    return Mono.create(
        sink -> {
          // define a unique job type just for this conversation
          String jobType = "responseFor_" + requestId;
          // And start a worker for it
          ZeebeWorkerValue jobWorkerConfig = new ZeebeWorkerValue();

          jobWorkerConfig.setType(jobType);
          jobWorkerConfig.setAutoComplete(true);
          jobWorkerConfig.setName(jobType);
          JobWorker jobWorker =
              jobWorkerManager.openWorker(
                  zeebe,
                  jobWorkerConfig,
                  (client, job) -> {
                    // Read payload from process
                    String response = (String) job.getVariablesAsMap().get("response");
                    LOG.info(".. finished with response: `" + response + "`");
                    // When the job is there, read the response payload and return our response via
                    // the Mono
                    sink.success(response);
                  });
          // Make sure this worker is closed once the response was received
          sink.onDispose(() -> jobWorkerManager.closeWorker(jobWorker));
        });
  }
}
