package com.camunda.consulting.connector;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(name = "example-connector", type = "com.camunda.consulting:example", inputVariables = {})
public class ExampleConnector implements OutboundConnectorFunction {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleConnector.class);
private final MyBean myBean;


  public ExampleConnector(MyBean myBean) {
    this.myBean = myBean;
  }

  @Override
  public Object execute(OutboundConnectorContext context) throws Exception {
    LOG.info("Example connector has been invoked");
    return null;
  }
}
