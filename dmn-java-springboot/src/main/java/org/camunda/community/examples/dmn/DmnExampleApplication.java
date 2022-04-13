package org.camunda.community.examples.dmn;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.camunda.community.examples.dmn.rest.OnboardCustomerRestApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = { "classpath*:*.bpmn", "classpath*:*.dmn"})
public class DmnExampleApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DmnExampleApplication.class, args);

		context.getBean(OnboardCustomerRestApi.class).startOnboarding("prepaid", 75, 10);
		context.getBean(OnboardCustomerRestApi.class).startOnboarding("invoice", 25, 10);
	}

}
