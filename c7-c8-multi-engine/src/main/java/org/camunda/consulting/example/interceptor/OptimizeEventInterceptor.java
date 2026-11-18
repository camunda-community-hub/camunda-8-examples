package org.camunda.consulting.example.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;


public class OptimizeEventInterceptor implements ClientInterceptor {

  private static final Logger LOG = Logger.getLogger(OptimizeEventInterceptor.class.getName());
  private String optimizeEndpoint;
  private String token;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient = WebClient.create();

  public OptimizeEventInterceptor(String optimizeUrl, String optimizeToken) {
    this.optimizeEndpoint = optimizeUrl;
    this.token = optimizeToken;

  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
      CallOptions callOptions,
      Channel channel) {
    // Create a new client call by delegating to the original channel
    ClientCall<ReqT, RespT> clientCall = channel.newCall(methodDescriptor, callOptions);

    return new ForwardingClientCall.SimpleForwardingClientCall<>(clientCall) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(new MyClientCallListener<>(responseListener, objectMapper), headers);
      }
    };
  }

  private class MyClientCallListener<RespT> extends ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {

    private final ObjectMapper objectMapper;
    protected MyClientCallListener(Listener<RespT> delegate, ObjectMapper objectMapper) {
      super(delegate);
      this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(RespT message) {
      if(message.toString().startsWith("jobs")) {
        try {
          JsonNode jobs = objectMapper
              .readTree(JsonFormat.printer()
              .print((MessageOrBuilder) message))
              .get("jobs");
          List<InterceptedActivatedJob> activatedJobs = objectMapper.convertValue(jobs, TypeFactory.defaultInstance()
              .constructCollectionType(List.class, InterceptedActivatedJob.class));
          ingestToOptimize(activatedJobs);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
      super.onMessage(message);
  }

  private void ingestToOptimize(List<InterceptedActivatedJob> jobs) {
      List<OptimizeCloudEvent> cloudEvents = jobs
          .stream()
          .map( job -> OptimizeCloudEvent
          .builder()
          .id(String.valueOf(job.getKey()))
          .source(job.getWorker())
          .type(job.getType())
          .data(job.getVariables())
          .group(job.getElementId())
          .traceid(job.getVariables().get("correlationKey") != null ? job.getVariables().get("correlationKey").toString() : String.valueOf(job.getProcessInstanceKey()))
          .build())
          .collect(Collectors.toList());

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    LOG.fine("Ingesting to Optimize: " + cloudEvents);

    webClient.post()
        .uri(optimizeEndpoint)
        .headers(httpHeaders -> httpHeaders.addAll(headers))
        .bodyValue(cloudEvents)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
}
