# React Tasklist

## Features

* Uses Camunda Tasklist REST API to work with tasks
* Uses a webhook to start a process instance
* renders Camunda Forms using a dedicated component
* renders other forms using react

## Prerequisites

* run Camunda platform
* ensure you have access to:
  * Tasklist REST API
  * Connectors inbound endpoint
  * Keycloak REST API
* deploy _example.bpmn_ upfront (the application does not actually rely on a zeebe client, so no deployment)

>For this, you may have to generate your own "Application" in Identity

## How to use

* adjust the `application.yaml` to match your connection details
* run it using `mvn spring-boot:run`