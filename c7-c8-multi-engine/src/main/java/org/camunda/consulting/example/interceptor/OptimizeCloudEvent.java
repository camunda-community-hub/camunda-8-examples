package org.camunda.consulting.example.interceptor;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class OptimizeCloudEvent {

  @Builder.Default String specversion = "1.0";
  String id;
  String source;
  String type;
  @Builder.Default String time = Instant.now().toString();
  Object data;
  String group;
  String traceid;


}
