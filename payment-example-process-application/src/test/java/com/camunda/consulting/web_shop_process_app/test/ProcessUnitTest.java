package com.camunda.consulting.web_shop_process_app.test;

import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.camunda.consulting.web_shop_process_app.service.CreditCardExpiredException;
import com.camunda.consulting.web_shop_process_app.service.CreditCardService;
import com.camunda.consulting.web_shop_process_app.service.CustomerService;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.search.response.UserTask;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CamundaSpringProcessTest
@SpringBootTest
public class ProcessUnitTest {

  @Autowired ZeebeClient zeebeClient;
  @MockitoBean CustomerService mockedCustomerService;
  @MockitoBean CreditCardService mockedCreditCardService;
  @Autowired private CamundaProcessTestContext processTestContext;

  @BeforeEach
  void init() {
    CamundaAssert.setAssertionTimeout(Duration.ofMinutes(1));
    zeebeClient
        .newDeployResourceCommand()
        .addResourceFromClasspath("check-payment.form")
        .addResourceFromClasspath("payment_process.bpmn")
        .send()
        .join();
  }

  @Test
  public void testHappyPath() throws InterruptedException, TimeoutException {
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

    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElements(byName("Charge credit card"))
        .hasVariable("remainingAmount", 90.0);
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
            .startBeforeElement("ChargeCustomerCreditTask")
            .variables(Map.of("customerId", "testCustomer", "orderTotal", 50.0))
            .send()
            .join();

    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElements(byName("no credit card payment required"));
    // missing: assert on not completed elements
  }

  @Test
  @Disabled("further research requiried")
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
    CamundaAssert.assertThat(processInstance)
        .isActive()
        .hasCompletedElements(byName("Payment requested"))
        .hasActiveElements(byName("Charge customer credit"));
    // missing: assert on incident state
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
            .startBeforeElement("ChargeCreditCardTask")
            .send()
            .join();

    CamundaAssert.assertThat(processInstance)
        .isActive()
        .hasActiveElements(byName("Check payment data"))
        .hasCompletedElements(byName("Invalid expiry\n" + "date"));
  }

  @Test
  @Disabled("user task testing")
  public void testCheckPayment() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .startBeforeElement("CheckPaymentDataTask")
            .send()
            .join();

    CamundaAssert.assertThat(processInstance)
        .isActive()
        .hasActiveElement("CheckPaymentDataTask", 1);
    completeZeebeUserTask(processInstance.getProcessInstanceKey(), Map.of("errorResolved", false));

    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElements(byName("Payment failed"));
  }

  @Test
  @Disabled("user task testing")
  public void testRetryPayment() throws InterruptedException, TimeoutException {
    ProcessInstanceEvent processInstance =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("paymentProcess")
            .latestVersion()
            .startBeforeElement("CheckPaymentDataTask")
            .variables(Map.of("remainingAmount", 10.0))
            .send()
            .join();
    CamundaAssert.assertThat(processInstance)
        .isActive()
        .hasActiveElement("CheckPaymentDataTask", 1)
        .hasVariable("remainingAmount", 10.0);
    completeZeebeUserTask(processInstance.getProcessInstanceKey(), Map.of("errorResolved", true));

    CamundaAssert.assertThat(processInstance).hasCompletedElements(byName("Charge credit card"));
  }

  protected void completeZeebeUserTask(long processInstanceKey, Map<String, Object> variables) {

    System.out.println("Searching user task");

    var response =
        zeebeClient
            .newUserTaskQuery()
            .filter(t -> t.processInstanceKey(processInstanceKey).elementId("CheckPaymentDataTask"))
            .send()
            .join();

    UserTask userTask = response.items().get(0);
    System.out.println("User Task: " + userTask);
    zeebeClient.newUserTaskCompleteCommand(userTask.getKey()).variables(variables).send().join();
  }

  protected void completeUserTask(long processInstanceKey, Map<String, Object> variables)
      throws InterruptedException, TimeoutException {
    List<ActivatedJob> userTaskJobs =
        Awaitility.await()
            .until(
                () ->
                    zeebeClient
                        .newActivateJobsCommand()
                        .jobType("io.camunda.zeebe:userTask")
                        .maxJobsToActivate(1000)
                        .send()
                        .join()
                        .getJobs()
                        .stream()
                        .filter(j -> j.getProcessInstanceKey() == processInstanceKey)
                        .toList(),
                list -> list.size() == 1);
    assertThat(userTaskJobs).hasSize(1);
    ActivatedJob activatedJob = userTaskJobs.get(0);
    zeebeClient.newCompleteCommand(activatedJob).variables(variables).send().join();
  }
}
