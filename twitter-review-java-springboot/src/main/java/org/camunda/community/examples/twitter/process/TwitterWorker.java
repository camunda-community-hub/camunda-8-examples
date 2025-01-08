package org.camunda.community.examples.twitter.process;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError;
import java.util.Map;
import org.camunda.community.examples.twitter.business.DuplicateTweetException;
import org.camunda.community.examples.twitter.business.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TwitterWorker {

  @Autowired private TwitterService twitterService;

  @JobWorker(type = "publish-tweet")
  public void handleTweet(@VariablesAsType TwitterProcessVariables variables) throws Exception {
    try {
      twitterService.tweet(variables.getTweet());
    } catch (DuplicateTweetException ex) {
      throw new ZeebeBpmnError(
          "duplicateMessage", "Could not post tweet, it is a duplicate.", Map.of());
    }
  }

  @JobWorker(type = "send-rejection")
  public void sendRejection(@VariablesAsType TwitterProcessVariables variables) throws Exception {
    // same thing as above, do data transformation and delegate to real business code / service
  }
}
