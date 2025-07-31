package io.camunda.example;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.example.cluster.TopologyViewer;
import io.camunda.example.data.HandleVariablesAsPojo;
import io.camunda.example.decision.EvaluateDecisionCreator;
import io.camunda.example.job.JobWorkerCreator;
import io.camunda.example.process.NonBlockingProcessInstanceCreator;
import io.camunda.example.process.ProcessDeployer;
import io.camunda.example.process.ProcessInstanceCreator;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class DocsConsistencyTest {

  static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(TopologyViewer.class, "io.camunda.example.cluster.TopologyViewer"),
        Arguments.of(JobWorkerCreator.class, "io.camunda.example.job.JobWorkerCreator"),
        Arguments.of(
            NonBlockingProcessInstanceCreator.class,
            "io.camunda.example.process.NonBlockingProcessInstanceCreator"),
        Arguments.of(ProcessDeployer.class, "io.camunda.example.process.ProcessDeployer"),
        Arguments.of(
            ProcessInstanceCreator.class, "io.camunda.example.process.ProcessInstanceCreator"),
        Arguments.of(HandleVariablesAsPojo.class, "io.camunda.example.data.HandleVariablesAsPojo"),
        Arguments.of(
            EvaluateDecisionCreator.class, "io.camunda.example.decision.EvaluateDecisionCreator"));
  }

  @ParameterizedTest
  @MethodSource("data")
  void todo(Class<?> exampleClass, String expectedClassName) {
    assertThat(exampleClass.getName())
        .withFailMessage(
            "This class's source code is referenced from the java-client-example docs. "
                + "Make sure to adapt them as well.")
        .isEqualTo(expectedClassName);
  }
}
