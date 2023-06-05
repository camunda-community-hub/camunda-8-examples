package org.camunda.community.examples.dmn.process;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

@Component
public class ProcessingWorker {

  @JobWorker(type = "processing")
  public void handleTweet() throws Exception {
    System.out.println("AUTOMATIC PROCESSING HAPPENING");
  }
}
