package com.camunda.consulting.web_shop_process_app.test;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.*;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.camunda.consulting.web_shop_process_app.service.CreditCardExpiredException;
import com.camunda.consulting.web_shop_process_app.service.CreditCardService;
import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import com.camunda.consulting.web_shop_process_app.worker.CreditCardHandler;
import com.camunda.consulting.web_shop_process_app.worker.CustomerCreditHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ZeebeProcessTest
@ExtendWith({ProcessEngineCoverageExtension.class, MockitoExtension.class})
public class ProcessUnitTest {

  ZeebeClient zeebeClient;
  ZeebeTestEngine engine;

  @Mock CustomerService mockedCustomerService;

  @Mock CreditCardService mockedCreditCardService;

  private void assertProcessInstanceCompleted(ProcessInstanceEvent processInstance)
      throws InterruptedException, TimeoutException {
    engine.waitForIdleState(Duration.ofSeconds(10));
    assertThat(processInstance).isCompleted();
  }

  private void assertProcessInstanceHasPassedElement(
      ProcessInstanceEvent processInstance, String elementId)
      throws InterruptedException, TimeoutException {
    engine.waitForIdleState(Duration.ofSeconds(10));
    assertThat(processInstance).hasPassedElement(elementId, 1);
  }

  @BeforeEach
  void init() {
    zeebeClient
        .newDeployResourceCommand()
        .addResourceFromClasspath("check-payment.form")
        .addResourceFromClasspath("payment_process.bpmn")
        .send()
        .join();
  }

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
    completeCustomerCreditHandling();
    completeCreditCardCharging();
    assertProcessInstanceCompleted(processInstance);

    assertThat(processInstance)
        .hasPassedElement("Activity_0nppgjk")
        .hasVariableWithValue("remainingAmount", 90.0);
    verify(mockedCustomerService).getCustomerCredit("testCustomer");
  }

  private void completeCreditCardCharging() throws InterruptedException, TimeoutException {
    completeJob(
        "creditCardCharging",
        (activatedJob) ->
            new CreditCardHandler(mockedCreditCardService).handle(zeebeClient, activatedJob));
  }

  private void completeCustomerCreditHandling() throws InterruptedException, TimeoutException {
    completeJob(
        "customerCreditHandling",
        (activatedJob) -> {
          try {
            Map<String, Object> result =
                new CustomerCreditHandler(mockedCustomerService).handle(activatedJob);
            zeebeClient.newCompleteCommand(activatedJob).variables(result).send().join();
          } catch (Exception e) {
            zeebeClient.newFailCommand(activatedJob).retries(0).send().join();
          }
        });
  }

  private void completeJob(String jobType, Consumer<ActivatedJob> handler)
      throws InterruptedException, TimeoutException {
    // wait for idle state
    engine.waitForIdleState(Duration.ofSeconds(10));
    // find a job
    ActivateJobsResponse jobsResponse =
        zeebeClient.newActivateJobsCommand().jobType(jobType).maxJobsToActivate(1).send().join();
    // expect exactly one
    assertThat(jobsResponse).isNotNull();
    assertThat(jobsResponse.getJobs()).hasSize(1);
    // use the handler
    handler.accept(jobsResponse.getJobs().get(0));
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
    completeCustomerCreditHandling();
    assertProcessInstanceCompleted(processInstance);

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

    completeCustomerCreditHandling();

    assertThat(processInstance).isActive().hasAnyIncidents();
  }

  @Test
  public void testInvalidExpiryDate() throws InterruptedException, TimeoutException {
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
    completeCreditCardCharging();
    assertProcessInstanceHasPassedElement(processInstance, "Event_0u18e53");

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

    assertProcessInstanceCompleted(processInstance);

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
    completeCreditCardCharging();

    assertProcessInstanceHasPassedElement(processInstance, "Gateway_1ymklbs");

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
