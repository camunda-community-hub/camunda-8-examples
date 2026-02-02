[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)

# Camunda 8 Examples

A collection of technical examples demonstrating how to use Camunda 8 features and solve common implementation challenges. These examples are designed for developers who want to get started with Camunda 8 or explore specific technical patterns and integrations.

For business-focused tutorials and process blueprints, see the [Camunda 8 Tutorials](https://github.com/camunda/camunda-8-tutorials) repository.

## What's in this Repository

This repository contains standalone Maven projects (and some Python examples) that demonstrate specific technical patterns and use cases:

### Getting Started & Client Usage

- **[camunda-client-plain-java](camunda-client-plain-java/)** - Basic examples using the Camunda Java client ([docs](https://docs.camunda.io/docs/apis-tools/java-client-examples/))
- **[zeebe-client-plain-java](zeebe-client-plain-java/)** - Examples using the Zeebe Java client ([docs](https://docs.camunda.io/docs/product-manuals/clients/java-client-examples/index))
- **[weatherinfo-pyzeebe-connectors](weatherinfo-pyzeebe-connectors/)** - Python worker example using pyzeebe with REST and SendGrid connectors

### Complete Process Applications

- **[payment-example-process-application](payment-example-process-application/)** - Full Spring Boot application with BPMN deployment, workers, forms, and REST API. Includes Kubernetes deployment tutorial
- **[twitter-review-java-springboot](twitter-review-java-springboot/)** - Spring Boot process application example

### Architectural Patterns

- **[async-service-task](async-service-task/)** - Implement asynchronous service calls as synchronous-looking tasks with idempotent requests and polling
- **[synchronous-response-springboot](synchronous-response-springboot/)** - Return synchronous responses from process instances by blocking until a specific state is reached
- **[event-processing](event-processing/)** - Achieve consistency when processing events from RabbitMQ/Kafka and persisting to databases
- **[rollback-on-error-example](rollback-on-error-example/)** - Implement synchronous validation with rollback capability in an async environment
- **[parallel-operations](parallel-operations/)** - Run Camunda 7 and Camunda 8 in parallel using shared delegates

### Advanced Features

- **[large-multi-instance-example](large-multi-instance-example/)** - Handle very large sequences (40,000+ elements) using multi-instance subprocesses with chunking strategy
- **[ProcessMetadataListener](ProcessMetadataListener/)** - Generic metadata listener that provides process context information (keys, versions) as variables
- **[timer-testing](timer-testing/)** - Unit testing approaches for timer events in Zeebe

### Connectors & Extensions

- **[extended-connector-runtime](extended-connector-runtime/)** - Extend connector runtime with custom FEEL functions using SPI
- **[element-template-generation](element-template-generation/)** - Generate element templates with dynamic dropdowns and conditional properties
- **[secret-provider-as-credentials-provider](secret-provider-as-credentials-provider/)** - Implement custom secret providers to abstract OAuth and credential management

### User Interface & Task Management

- **[react-tasklist](react-tasklist/)** - Custom tasklist using Tasklist REST API with Camunda Forms rendering and webhook integration
- **[task-micro-frontend](task-micro-frontend/)** - Micro frontend for embedding Camunda tasks in custom tasklists

### External Integrations

- **[powerapps-dataverse](powerapps-dataverse/)** - Connect Camunda 8 REST Connector to Microsoft PowerApps Dataverse via OAuth2

## How to Use These Examples

1. **Clone the repository**:
   ```bash
   git clone https://github.com/camunda-community-hub/camunda-8-examples.git
   cd camunda-8-examples
   ```

2. **Navigate to an example**:
   ```bash
   cd <example-directory>
   ```

3. **Follow the README**: Each example includes:
   - Purpose and use case description
   - Architecture and implementation details
   - Prerequisites and setup instructions
   - How to run and test the example

4. **Requirements**: Most examples require:
   - Java 17+ and Maven 3.6+ (for Java examples)
   - Python 3.8+ (for Python examples)
   - Access to a Camunda 8 cluster (SaaS or self-managed)
   - Example-specific dependencies (Docker, Kubernetes, etc.)

## Prerequisites

Most examples assume you have access to a Camunda 8 environment. You can:
- Sign up for [Camunda 8 SaaS](https://signup.camunda.com/) (free trial available)
- Set up [Camunda 8 Self-Managed](https://docs.camunda.io/docs/self-managed/setup/overview/) using Docker Compose

## Report Issues

If you encounter problems with an example, please [file an issue](../../issues) including:
- Which example you're using
- What didn't work as expected
- Your environment (Java version, OS, Camunda 8 version, etc.)
- Steps to reproduce the problem
