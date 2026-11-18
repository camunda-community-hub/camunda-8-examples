package org.camunda.consulting.example.interceptor;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InterceptedActivatedJob {

  private long key;
  private String type;
  private String customHeaders;
  private long processInstanceKey;
  private String bpmnProcessId;
  private int processDefinitionVersion;
  private long processDefinitionKey;
  private String elementId;
  private long elementInstanceKey;
  private String worker;
  private int retries;
  private long deadline;
  @JsonProperty("variables")
  private void unpackVariables(String variables) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      this.variables = objectMapper.readValue(variables, new TypeReference<>() {
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @JsonIgnore
  private Map<String, Object> variables;

}
