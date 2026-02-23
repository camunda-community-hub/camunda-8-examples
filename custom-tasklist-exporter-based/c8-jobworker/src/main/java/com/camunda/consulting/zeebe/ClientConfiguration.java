package com.camunda.consulting.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

  final String JOB_TYPE = "io.camunda.zeebe:userTask";

  @Bean
  public ZeebeClient zeebeClient() {
    final String defaultAddress = "localhost:26500";
    final String envVarAddress = System.getenv("ZEEBE_ADDRESS");

    final ZeebeClientBuilder clientBuilder;
    if (envVarAddress != null) {
      /* Connect to Camunda Cloud Cluster, assumes that credentials are set in environment variables.
       * See JavaDoc on class level for details
       */
      clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(envVarAddress).usePlaintext();
    } else {
      // connect to local deployment; assumes that authentication is disabled
      clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();
    }

    return clientBuilder
        .defaultJobWorkerName("userTaskWorker")
        .build();
  }

  @Bean
  public JobWorker jobWorker(ZeebeClient client) {
    return client
        .newWorker()
        .jobType(JOB_TYPE)
        .handler(new UserTaskHandler())
        .timeout(Duration.ofDays(30))
        .open();
  }


}
