package org.example.camunda.process.solution.facade;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.example.camunda.process.solution.ProcessConstants;
import org.example.camunda.process.solution.model.CreateDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.Charset.defaultCharset;

@Slf4j
@RestController
@RequestMapping("/sample-data")
public class SampleDataController {

    @Autowired
    private ZeebeClient zeebe;

    private String exampledata = null;

    @PostConstruct
    public void init() throws IOException {
        exampledata = StreamUtils.copyToString(new ClassPathResource("exampledata.txt").getInputStream(), defaultCharset());
    }

    @PostMapping("/createData")
    public void createData(@RequestBody CreateDataRequest createDataRequest) {
        log.info("Starting {} processes with processId={}", createDataRequest.iterations(), ProcessConstants.BPMN_PROCESS_ID);

        for (int i = createDataRequest.startNumber(); i < createDataRequest.startNumber() + createDataRequest.iterations(); i++) {

            log.info("Starting process #{}", i);
            zeebe.newCreateInstanceCommand().bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID).latestVersion()
                    .variables(Map.of("iteration", i, "payload", exampledata)).send().join();
            log.info("Started process #{}", i);
        }
    }

}
