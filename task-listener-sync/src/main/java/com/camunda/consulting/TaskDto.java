package com.camunda.consulting;

import com.camunda.consulting.InternalTask.State;

import java.util.Map;

public record TaskDto(
    Map<String, Object> variables, Object formSchema, State status) {}
