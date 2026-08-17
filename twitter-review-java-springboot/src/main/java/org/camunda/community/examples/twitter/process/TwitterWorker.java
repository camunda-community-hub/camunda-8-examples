package org.camunda.community.examples.twitter.process;

import io.camunda.client.CamundaError;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.VariablesAsType;
import org.camunda.community.examples.twitter.business.DuplicateTweetException;
import org.camunda.community.examples.twitter.business.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TwitterWorker {

  @Autowired private TwitterService twitterService;

  @JobWorker
  public void publishTweet(@VariablesAsType TwitterProcessVariables variables) throws Exception {
    try {
      twitterService.tweet(variables.getTweet());
    } catch (DuplicateTweetException ex) {
      throw CamundaError.bpmnError("duplicateMessage", "Could not post tweet, it is a duplicate.");
    }
  }

  @JobWorker
  public void sendRejection(@VariablesAsType TwitterProcessVariables variables) throws Exception {
    // same thing as above, do data transformation and delegate to real business code / service
  }
}
