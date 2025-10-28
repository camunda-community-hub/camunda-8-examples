package org.camunda.community.examples.twitter;

import static io.camunda.process.test.api.assertions.ProcessInstanceSelectors.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Collections;
import org.camunda.community.examples.twitter.business.DuplicateTweetException;
import org.camunda.community.examples.twitter.business.TwitterService;
import org.camunda.community.examples.twitter.process.TwitterProcessVariables;
import org.camunda.community.examples.twitter.rest.ReviewTweetRestApi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
public class TestTwitterProcess {

  @Autowired private CamundaClient camundaClient;

  @Autowired private CamundaProcessTestContext testContext;

  @MockitoBean private TwitterService twitterService;
  @Autowired private ReviewTweetRestApi restApi;

  @Test
  public void testTweetApproved() throws Exception {
    // Prepare data input
    TwitterProcessVariables variables =
        new TwitterProcessVariables().setTweet("Hello world").setBoss("Zeebot");

    // start a process instance
    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("TwitterDemoProcess")
            .latestVersion()
            .variables(variables)
            .send()
            .join();

    // And then retrieve the UserTask and complete it with 'approved = true'
    testContext.completeJob(
        "io.camunda.zeebe:userTask", Collections.singletonMap("approved", true));

    // Let's assert that it passed certain BPMN elements (more to show off features here)
    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElement("end_event_tweet_published", 1)
        .hasNotActivatedElements("end_event_tweet_rejected");

    // And verify it caused the right side effects b calling the business methods
    Mockito.verify(twitterService).tweet("Hello world");
    Mockito.verifyNoMoreInteractions(twitterService);
  }

  @Test
  public void testRejectionPath() throws Exception {
    TwitterProcessVariables variables =
        new TwitterProcessVariables().setTweet("Hello world").setBoss("Zeebot");

    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("TwitterDemoProcess")
            .latestVersion()
            .variables(variables)
            .startBeforeElement("gateway_approved")
            .send()
            .join();

    CamundaAssert.assertThat(processInstance)
        .isCompleted()
        .hasCompletedElement("end_event_tweet_rejected", 1);
    Mockito.verify(twitterService, never()).tweet(anyString());
  }

  @Test
  public void testDuplicateTweet() throws Exception {
    // throw exception simulating duplicateM
    Mockito.doThrow(new DuplicateTweetException("DUPLICATE"))
        .when(twitterService)
        .tweet(anyString());

    TwitterProcessVariables variables =
        new TwitterProcessVariables()
            .setTweet("Hello world")
            .setAuthor("bernd")
            .setBoss("Zeebot")
            .setApproved(false);

    ProcessInstanceEvent processInstance =
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("TwitterDemoProcess")
            .latestVersion()
            .variables(variables)
            .startBeforeElement("service_task_publish_on_twitter")
            .send()
            .join();

    CamundaAssert.assertThat(processInstance)
        .hasCompletedElement("boundary_event_tweet_duplicated", 1);
    testContext.completeJob("io.camunda.zeebe:userTask");

    Mockito.doNothing().when(twitterService).tweet(anyString());
    testContext.completeJob(
        "io.camunda.zeebe:userTask", Collections.singletonMap("approved", true));
    CamundaAssert.assertThat(processInstance).isCompleted();
  }

  /**
   * This is an alternative test that uses the REST API code instead of directly starting a process
   * instance This is even more realistic, as it also validates the data input mapping
   */
  @Test
  public void testTweetApprovedByRestApi() throws Exception {
    restApi.startTweetReviewProcess("bernd", "Hello REST world", "Zeebot");
    testContext.completeJob(
        "io.camunda.zeebe:userTask", Collections.singletonMap("approved", true));
    CamundaAssert.assertThat(byProcessId("TwitterDemoProcess"))
        .isCompleted()
        .hasCompletedElement("end_event_tweet_published", 1)
        .hasNotActivatedElements("end_event_tweet_rejected");
    // And verify it caused the right side effects b calling the business methods
    Mockito.verify(twitterService).tweet("Hello REST world");
    Mockito.verifyNoMoreInteractions(twitterService);
  }
}
