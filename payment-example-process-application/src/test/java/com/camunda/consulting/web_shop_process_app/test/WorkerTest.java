package com.camunda.consulting.web_shop_process_app.test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import com.camunda.consulting.web_shop_process_app.worker.CustomerCreditHandler;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WorkerTest {

  @Mock(stubOnly = true)
  ActivatedJob mockedJob;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testCustomerCreditWorker() {
    given(mockedJob.getVariablesAsMap())
        .willReturn(Map.of("customerId", "testCustomer40", "orderTotal", 75.0));
    CustomerCreditHandler customerCreditHandler = new CustomerCreditHandler(new CustomerService());

    Map<String, Object> variables = customerCreditHandler.handle(mockedJob);
    assertThat(variables).contains(entry("remainingAmount", 35.0));
  }
}
