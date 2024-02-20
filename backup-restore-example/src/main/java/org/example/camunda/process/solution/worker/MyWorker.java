package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class MyWorker {

    @JobWorker
    public Map<String, Object> invokeMyService(@VariablesAsType Map<String, Object> variables) {
        log.info("Invoking myService with variables: " + variables);

        variables.put("invokedMyservice", true);

        return variables;
    }
}
