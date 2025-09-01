package com.camunda.consulting;

import java.util.Map;

public record TaskDto(
    Map<String, Object> variables, Map<String, Object> formSchema, String status) {}
