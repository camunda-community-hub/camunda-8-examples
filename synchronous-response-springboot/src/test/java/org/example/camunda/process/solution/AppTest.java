package org.example.camunda.process.solution;

import static org.assertj.core.api.Assertions.*;

import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.example.camunda.process.solution.facade.ProcessController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ZeebeSpringTest
public class AppTest {

  @Autowired ProcessController processController;

  @Test
  void shouldWork() {
    String response = processController.startProcessInstance().block();
    assertThat(response).isEqualTo("The response is - of course - 42");
  }
}
