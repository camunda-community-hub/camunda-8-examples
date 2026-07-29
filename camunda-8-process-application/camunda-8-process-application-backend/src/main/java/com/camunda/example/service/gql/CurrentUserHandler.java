package com.camunda.example.service.gql;

import com.camunda.example.client.tasklist.model.GraphQLOperationDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.language.OperationDefinition.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserHandler implements ResponseHandler {
  private static final Logger log = LoggerFactory.getLogger(CurrentUserHandler.class);

  @Override
  public boolean canHandle(GraphQLOperationDefinition operationDefinition) {
    return operationDefinition
        .getOperationDefinitionType()
        .equals(Operation.QUERY) && operationDefinition
        .getOperationName()
        .equals("currentUser");
  }

  @Override
  public void handleResponse(GraphQLOperationDefinition operationDefinition, JsonNode response, ObjectNode requestVariables) {
    Optional
        .ofNullable(SecurityContextHolder.getContext())
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getPrincipal)
        .filter(principal -> OidcUser.class.isAssignableFrom(principal.getClass()))
        .map(OidcUser.class::cast)
        .ifPresent(principal -> {
          ObjectNode responseObject = (ObjectNode) response;
          // userId
          setIfPresent(responseObject, "userId", JsonNodeFactory.instance.textNode(principal.getPreferredUsername()));
          // displayName
          setIfPresent(responseObject, "displayName", JsonNodeFactory.instance.textNode(principal.getFullName()));
          // TODO figure out permissions

          // roles

          // salesPlanType
        });

  }

  private void setIfPresent(ObjectNode responseObject, String property, JsonNode value) {
    if (responseObject.has(property)) {
      JsonNode replace = responseObject.replace(property, value);
      log.info("Replaced property {}: old value: {} , new value {}", property, replace, value);
    }
  }
}
