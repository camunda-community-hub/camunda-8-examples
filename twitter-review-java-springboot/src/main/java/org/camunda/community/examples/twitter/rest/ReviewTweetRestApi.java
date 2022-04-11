package org.camunda.community.examples.twitter.rest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.camunda.community.examples.twitter.process.TwitterProcessVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class ReviewTweetRestApi {

    @Autowired
    private ZeebeClient zeebeClient;

    @PutMapping("/tweet")
    public ResponseEntity<String> startTweetReviewProcess(ServerWebExchange exchange) {
        // TODO: add data to the process instance from REST request
        String reference = startTweetReviewProcess("bernd", "Hello World", "Zeebot");

        // And just return something for the sake of the example
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Started process instance " + reference);
    }

    public String startTweetReviewProcess(String author, String tweet, String boss) {
        TwitterProcessVariables processVariables = new TwitterProcessVariables().setAuthor(author).setTweet(tweet).setBoss(boss);

        ProcessInstanceEvent processInstance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("TwitterDemoProcess")
                .latestVersion()
                .variables(processVariables)
                .send().join();// blocking call!

        return String.valueOf( processInstance.getProcessInstanceKey() );
    }
}
