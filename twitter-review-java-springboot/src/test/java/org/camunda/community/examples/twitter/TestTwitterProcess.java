package org.camunda.community.examples.twitter;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.camunda.community.examples.twitter.business.DuplicateTweetException;
import org.camunda.community.examples.twitter.business.TwitterService;
import org.camunda.community.examples.twitter.process.TwitterProcessVariables;
import org.camunda.community.examples.twitter.rest.ReviewTweetRestApi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

@SpringBootTest
@ZeebeSpringTest
public class TestTwitterProcess {

    @Autowired
    private ZeebeClient zeebe;

    // TODO: We should probably get rid of this in Spring tests or at least hide it somewhere
    // At the moment we have two different ways of waiting: Multi-threaded waiting, and the "engine run to completion"
    @Autowired
    private ZeebeTestEngine zeebeTestEngine;

    @MockBean
    private TwitterService twitterService;

    @Test
    public void testTweetApproved() throws Exception {
        // Prepare data input
        TwitterProcessVariables variables = new TwitterProcessVariables()
            .setTweet("Hello world")
            .setBoss("Zeebot");

        // start a process instance
        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand() //
            .bpmnProcessId("TwitterDemoProcess").latestVersion() //
            .variables(variables) //
            .send().join();

        // And then retrieve the UserTask and complete it with 'approved = true'
        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", true));

        // Now the process should run to the end
        waitForProcessInstanceCompleted(processInstance);

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .hasPassedElement("end_event_tweet_published")
                .hasNotPassedElement("end_event_tweet_rejected")
                .isCompleted();

        // And verify it caused the right side effects b calling the business methods
        Mockito.verify(twitterService).tweet("Hello world");
        Mockito.verifyNoMoreInteractions(twitterService);
    }



    @Test
    public void testRejectionPath() throws Exception {
        TwitterProcessVariables variables = new TwitterProcessVariables()
          .setTweet("Hello world")
          .setBoss("Zeebot");

        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand() //
                .bpmnProcessId("TwitterDemoProcess").latestVersion() //
                .variables(variables) //
                .send().join();

        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", false));

        waitForProcessInstanceCompleted(processInstance);
        waitForProcessInstanceHasPassedElement(processInstance, "end_event_tweet_rejected");
        Mockito.verify(twitterService, never()).tweet(anyString());
    }

    @Test
    public void testDuplicateTweet() throws Exception {
        // throw exception simulating duplicateM
        Mockito.doThrow(new DuplicateTweetException("DUPLICATE")).when(twitterService).tweet(anyString());

        TwitterProcessVariables variables = new TwitterProcessVariables()
                .setTweet("Hello world")
                .setAuthor("bernd")
                .setBoss("Zeebot");

        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand() //
                .bpmnProcessId("TwitterDemoProcess").latestVersion() //
                .variables(variables) //
                .send().join();

        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", true));
        waitForProcessInstanceHasPassedElement(processInstance, "boundary_event_tweet_duplicated");
        waitForUserTaskAndComplete("user_task_handle_duplicate", new HashMap<>());
        // second try :-) --> TODO: Think about isolation of test cases when we can better cleanup the engine

        Mockito.doNothing().when(twitterService).tweet(anyString());
        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", true));
        waitForProcessInstanceCompleted(processInstance);
    }

    public void waitForUserTaskAndComplete(String userTaskId, Map<String, Object> variables) throws InterruptedException, TimeoutException {
        // Let the workflow engine do whatever it needs to do
        zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));

        // Now get all user tasks
        List<ActivatedJob> jobs = zeebe.newActivateJobsCommand().jobType(USER_TASK_JOB_TYPE).maxJobsToActivate(1).workerName("waitForUserTaskAndComplete").send().join().getJobs();

        // Should be only one
        assertTrue(jobs.size()>0, "Job for user task '" + userTaskId + "' does not exist");
        ActivatedJob userTaskJob = jobs.get(0);
        // Make sure it is the right one
        if (userTaskId!=null) {
            assertEquals(userTaskId, userTaskJob.getElementId());
        }

        // And complete it passing the variables
        if (variables!=null && variables.size()>0) {
            zeebe.newCompleteCommand(userTaskJob.getKey()).variables(variables).send().join();
        } else {
            zeebe.newCompleteCommand(userTaskJob.getKey()).send().join();
        }
    }

    @Autowired
    private ReviewTweetRestApi restApi;

    /**
     * This is an alternative test that uses the REST API code instead of directly starting a process instance
     * This is even more realistic, as it also validates the data input mapping
     */
    @Test
    public void testTweetApprovedByRestApi() throws Exception {
        ProcessInstanceEvent processInstance = restApi.startTweetReviewProcess("bernd", "Hello REST world", "Zeebot");

        // Let the workflow engine do whatever it needs to do
        // And then retrieve the UserTask and complete it with 'approved = true'
        waitForUserTaskAndComplete("user_task_review_tweet", Collections.singletonMap("approved", true));

        waitForProcessInstanceCompleted(processInstance);

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .hasPassedElement("end_event_tweet_published")
                .hasNotPassedElement("end_event_tweet_rejected")
                .isCompleted();

        // And verify it caused the right side effects b calling the business methods
        Mockito.verify(twitterService).tweet("Hello REST world");
        Mockito.verifyNoMoreInteractions(twitterService);
    }
}
