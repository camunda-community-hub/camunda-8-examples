package com.camunda.consulting;

import com.camunda.consulting.InternalTask.State;
import com.camunda.consulting.InternalTask.SyncType;

import java.util.Map;

public record TaskDto(long key, Map<String, Object> variables, Object form, State state, SyncType syncType) {}
