package org.camunda.community.examples.dmn;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.camunda.community.examples.dmn.process.OnboardingProcessVariables;
import org.camunda.community.examples.dmn.rest.OnboardCustomerRestApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ZeebeSpringTest
public class TestOnboardingProcess {

    @Autowired
    private ZeebeClient zeebe;

    // TODO: We should probably get rid of this in Spring tests or at least hide it somewhere
    // At the moment we have two different ways of waiting: Multi-threaded waiting, and the "engine run to completion"
    @Autowired
    private ZeebeTestEngine zeebeTestEngine;

    @Autowired
    private OnboardCustomerRestApi onboardCustomerApi;

    @Test
    public void testAutomaticApproval() throws Exception {
        // kick of new onboarding process
        onboardCustomerApi.startOnboarding("prepaid", 75, 10);

        // find the corresponding PI, it should have been automatically completed
        InspectedProcessInstance processInstance = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
        waitForProcessInstanceCompleted(processInstance);

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .isCompleted()
                .hasPassedElement("EndEvent_ProcessedAutomatically");
    }

    @Test
    public void testManualApproval() throws Exception {
        // Using workflow engine API is also possible (as an alternative to using the API above, even if I would prefer using the `onboardCustomerApi`)
        // Still nice to show both ways here
        OnboardingProcessVariables variables = new OnboardingProcessVariables()
                .setPaymentType("invoice")
                .setMonthlyPayment(100)
                .setCustomerRegionScore(50);

        // start a process instance
        ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand() //
            .bpmnProcessId("CustomerOnboarding").latestVersion() //
            .variables(variables) //
            .send().join();

        //  And then retrieve the UserTask and complete it with 'approved = true'
        waitForUserTaskAndComplete("UserTask_HandleCustomerOrder");

        // Now the process should run to the end
        waitForProcessInstanceCompleted(processInstance);

        // Let's assert that it passed certain BPMN elements (more to show off features here)
        assertThat(processInstance)
                .hasPassedElement("EndEvent_ProcessedManually")
                .isCompleted();
    }

    /**
     * This method might be moved to spring-zeebe-test library soon
     */
    public void waitForUserTaskAndComplete(String userTaskId) throws InterruptedException, TimeoutException {
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

        // And complete it
        zeebe.newCompleteCommand(userTaskJob.getKey()).send().join();
    }

}
