package com.camunda.consulting;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class StringService {
  public String get() {
    return RandomStringUtils.randomAlphabetic(10);
  }
}
