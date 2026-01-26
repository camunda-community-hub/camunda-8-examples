package com.camunda.consulting;

import java.util.Map;

public record TaskDto(Map<String, Object> variables, Object formSchema, String status) {}
