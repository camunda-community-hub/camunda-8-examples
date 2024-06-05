package com.camunda.consulting;

import com.camunda.consulting.CarConnectorInput.Make;
import com.camunda.consulting.CarConnectorInput.Make.Audi;
import com.camunda.consulting.CarConnectorInput.Make.Volkswagen;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;

@OutboundConnector(
    name = "Car Connector",
    type = "com.camunda.consulting:carConnector:1",
    inputVariables = {"make", "gearbox"})
@ElementTemplate(
    id = "com.camunda.consulting:carConnector",
    name = "Car Connector",
    description = "A connector to get a car from a selection",
    documentationRef = "https://github.com/camunda-community-hub/camunda-8-examples",
    inputDataClass = CarConnectorInput.class,
    version = 1)
public class CarConnector implements OutboundConnectorFunction {
  @Override
  public Object execute(OutboundConnectorContext outboundConnectorContext) throws Exception {
    var input = outboundConnectorContext.bindVariables(CarConnectorInput.class);
    Make make = input.make();
    if (make instanceof Audi audi) {
      return new CarConnectorOutput("Audi", audi.audiModel().name(), input.gearbox().name());
    } else if (make instanceof Volkswagen volkswagen) {
      return new CarConnectorOutput(
          "Volkswagen", volkswagen.volkswagenModel().name(), input.gearbox().name());
    }
    throw new IllegalStateException("Unknown make type: " + make);
  }
}
