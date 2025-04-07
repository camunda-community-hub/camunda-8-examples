package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskWorker {

    @JobWorker
    public Map<String, Object> task1(@VariablesAsType Map<String, Object> variables) {
        variables.put("task1", System.currentTimeMillis());

        return variables;
    }

    @JobWorker
    public Map<String, Object> task2(@VariablesAsType Map<String, Object> variables) {
        variables.put("task2", System.currentTimeMillis());

        return variables;
    }

    @JobWorker
    public Map<String, Object> task3(@VariablesAsType Map<String, Object> variables) {
        variables.put("task3", System.currentTimeMillis());

        return variables;
    }

    @JobWorker
    public Map<String, Object> task4(@VariablesAsType Map<String, Object> variables) {
        variables.put("task4", System.currentTimeMillis());

        return variables;
    }

}
