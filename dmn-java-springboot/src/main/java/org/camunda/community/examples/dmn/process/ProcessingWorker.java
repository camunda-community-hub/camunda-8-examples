package org.camunda.community.examples.dmn.process;

import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Component;

@Component
public class ProcessingWorker {

    @ZeebeWorker( type = "processing", autoComplete = true)
    public void handleTweet() throws Exception {
        System.out.println("AUTOMATIC PROCESSING HAPPENING");
    }

}
