package com.camunda.consulting.web_shop_process_app.test;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.camunda.consulting.web_shop_process_app.service.CreditCardExpiredException;
import com.camunda.consulting.web_shop_process_app.service.CreditCardService;
import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@ZeebeSpringTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessIntegrationTest {

  @Autowired ZeebeClient zeebeClient;

  @Autowired ZeebeTestEngine engine;

  @MockBean CustomerService mockedCustomerService;

  @MockBean CreditCardService mockedCreditCardService;

  @Test
  public void testPappyPath() throws InterruptedException, TimeoutException {
    given(mockedCustomerService.deductCredit(anyString(), anyDouble(), anyDouble()))
        .willReturn(90.0);
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .variables(
                Map.of("customerId", "testCustomer", "orderTotal", 190.0, "expiryDate", "10/24"))
            .send()
            .join();

    waitForProcessInstanceCompleted(processInstance);

    assertThat(processInstance)
        .hasPassedElement("Activity_0nppgjk")
        .hasVariableWithValue("remainingAmount", 90.0);
    verify(mockedCustomerService).getCustomerCredit("testCustomer");
  }

  @Test
  public void testNoCreditCardRequired() throws InterruptedException, TimeoutException {
    given(mockedCustomerService.deductCredit(anyString(), anyDouble(), anyDouble()))
        .willReturn(0.0);
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .startBeforeElement("Activity_192ju2a")
            .variables(Map.of("customerId", "testCustomer", "orderTotal", 50.0))
            .send()
            .join();
    waitForProcessInstanceCompleted(processInstance);

    assertThat(processInstance).hasNotPassedElement("Activity_0nppgjk");
  }

  @Test
  public void testIncident() throws InterruptedException, TimeoutException {
    given(mockedCustomerService.deductCredit(anyString(), anyDouble(), anyDouble()))
        .willThrow(RuntimeException.class);
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .variables(Map.of("customerId", "testCustomer", "orderTotal", 190.0))
            .send()
            .join();

    engine.waitForIdleState(Duration.ofSeconds(20));

    assertThat(processInstance).isActive().hasAnyIncidents();
  }

  @Test
  public void testInvalidExpiryDate() throws InterruptedException {
    doThrow(new CreditCardExpiredException("expired"))
        .when(mockedCreditCardService)
        .chargeAmount("1234 5678", "123", "05/23", 100.0);

    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .variables(
                Map.of(
                    "cardNumber",
                    "1234 5678",
                    "cvc",
                    "123",
                    "expiryDate",
                    "05/23",
                    "remainingAmount",
                    100.0))
            .startBeforeElement("Activity_0nppgjk")
            .send()
            .join();

    waitForProcessInstanceHasPassedElement(processInstance, "Event_0u18e53");

    assertThat(processInstance)
        .isActive()
        .isWaitingAtElements("Activity_0tug4lk")
        .hasPassedElement("Event_0u18e53");
  }

  @Test
  public void testCheckPayment() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .startBeforeElement("Activity_0tug4lk")
            .send()
            .join();

    completeUserTask(Map.of("errorResolved", false));

    waitForProcessInstanceCompleted(processInstance);

    assertThat(processInstance).isCompleted().hasPassedElement("Event_1854135");
  }

  @Test
  public void testRetryPayment() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .startBeforeElement("Activity_0tug4lk")
            .variables(Map.of("remainingAmount", 10.0))
            .send()
            .join();

    completeUserTask(Map.of("errorResolved", true));

    waitForProcessInstanceHasPassedElement(processInstance, "Gateway_1ymklbs");

    assertThat(processInstance).hasPassedElement("Activity_0nppgjk");
  }

  protected void completeUserTask(Map<String, Object> variables)
      throws InterruptedException, TimeoutException {
    engine.waitForIdleState(Duration.ofSeconds(2));
    List<ActivatedJob> userTaskJobs =
        zeebeClient
            .newActivateJobsCommand()
            .jobType(USER_TASK_JOB_TYPE)
            .maxJobsToActivate(1)
            .send()
            .join()
            .getJobs();
    assertThat(userTaskJobs).hasSize(1);
    ActivatedJob activatedJob = userTaskJobs.get(0);
    zeebeClient.newCompleteCommand(activatedJob).variables(variables).send().join();
  }
}
