package com.camunda.consulting;


import com.camunda.consulting.CarConnectorInput.Make.Audi;
import com.camunda.consulting.CarConnectorInput.Make.Volkswagen;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.camunda.connector.generator.java.annotation.TemplateSubType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CarConnectorInput(@NotNull @Valid Make make, @NotNull Gearbox gearbox) {
  enum Gearbox {Manual, Automatic}

  @JsonTypeInfo(use = Id.NAME, property = "make")
  @JsonSubTypes({
      @Type(value = Audi.class, name = "Audi_make"), @Type(value = Volkswagen.class, name = "Volkswagen_make")
  })
  public sealed interface Make {
    @TemplateSubType(id = "Audi_make", label = "Audi")
    record Audi(@NotNull AudiModel audiModel) implements Make {
      enum AudiModel {
        A1, A3, A4, A5, A6, A7, A8 // no Q, because SUVs are s***
      }
    }

    @TemplateSubType(id = "Volkswagen_make", label = "VW")
    record Volkswagen(@NotNull VolkswagenModel volkswagenModel) implements Make {
      enum VolkswagenModel {
        Polo, Golf, Golf_Variant, Jetta, Passat, Arteon, T7
      }
    }
  }
}
