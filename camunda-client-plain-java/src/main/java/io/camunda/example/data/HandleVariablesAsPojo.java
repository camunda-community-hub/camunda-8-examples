package io.camunda.example.data;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import io.camunda.client.api.worker.JobHandler;
import io.camunda.example.ClientProvider;
import io.camunda.example.ClientProvider.AuthMethod;
import java.util.Scanner;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>It uses {@link ClientProvider} to create a client with the appropriate authentication method.
 */
public final class HandleVariablesAsPojo {
  public static void main(final String[] args) {
    try (final CamundaClient client = ClientProvider.createCamundaClient(AuthMethod.none)) {
      final Order order = new Order();
      order.setOrderId(31243);

      client
          .newCreateInstanceCommand()
          .bpmnProcessId("demoProcess")
          .latestVersion()
          .variables(order)
          .execute();

      client.newWorker().jobType("foo").handler(new DemoJobHandler()).open();

      // run until System.in receives exit command
      waitUntilSystemInput("exit");
    }
  }

  private static void waitUntilSystemInput(final String exitCode) {
    try (final Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        final String nextLine = scanner.nextLine();
        if (nextLine.contains(exitCode)) {
          return;
        }
      }
    }
  }

  public static class Order {
    private long orderId;
    private double totalPrice;

    public long getOrderId() {
      return orderId;
    }

    public void setOrderId(final long orderId) {
      this.orderId = orderId;
    }

    public double getTotalPrice() {
      return totalPrice;
    }

    public void setTotalPrice(final double totalPrice) {
      this.totalPrice = totalPrice;
    }
  }

  private static class DemoJobHandler implements JobHandler {
    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
      // read the variables of the job
      final Order order = job.getVariablesAsType(Order.class);
      System.out.println("new job with orderId: " + order.getOrderId());

      // update the variables and complete the job
      order.setTotalPrice(46.50);

      client.newCompleteCommand(job.getKey()).variables(order).send();
    }
  }
}
