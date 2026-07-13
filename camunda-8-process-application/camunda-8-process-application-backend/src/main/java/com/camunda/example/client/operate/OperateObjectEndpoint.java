package com.camunda.example.client.operate;

import com.camunda.example.client.operate.model.*;

public interface OperateObjectEndpoint<T> {
  ResultsDto<T> search(QueryDto<T> query);

  T get(Long key);
}
