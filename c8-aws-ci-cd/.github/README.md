# Camunda 8 CI/CD Example with AWS
This repository contains a working example of a process application. This process application is also found [here in our camunda-8-examples repository](https://github.com/camunda-community-hub/camunda-8-examples/tree/main/payment-example-process-application). For information on how this process is built, please refer to the linked repository.

What this repository focuses on is creating a comprehensive CI/CD pipline using github actions and AWS. This pipeline includes:

* Unit Tests
* Process Tests
* Integration Tests [NOT DONE]
* Deployment of bpmn,dmn, and Camunda Form resources to a Camunda Cluster
* Deployment of job workers to an AWS EKS cluster

## Repository Layout
In this repository you'll find several folders containing the resources needed to implement this CI/CD.

| Folder | Description                                                                |
|--------|----------------------------------------------------------------------------|
| .github | contains github actions workflows                                          |
| docker | contains Dockerfile for building a docker image of the process application |
| kube | contains kubernetes manifests                                              |
| src   | contains the process application                                           |

## Setup Steps
This repository does not cover installation of a Camunda cluster, or a Kubernetes cluster. Please be sure to do the following before starting this project.
1. Create a Camunda cluster, either self-managed or on Saas.
2. Create a client with authorization to read + write to Zeebe.
3. Create an EKS cluster
4. Generate access keys for a user which has access to the EKS cluster.

## Configuration - AWS
This repository requires the creation of several github action secrets to function.

| Secret Name | Description   |
|-------------|---------------|
| AWS_ACCESS_KEY_ID | access key id |
| AWS_SECRET_ACCESS_KEY | secret access key | 
| AWS_REGION | aws region of ECR repository and EKS cluster |
| ECR_REGISTRY_URI | can be found in your aws console for ECR. omit /<repository-name> from the end of this URI. |
| ECR_REPOSITORY | ECR repository name |
| EKS_CLUSTER_NAME | the name of the EKS cluster you want to deploy to |

## Configuration - SaaS
| Secret Name         | Description                                                          |
|---------------------|----------------------------------------------------------------------|
| CLUSTER_REGION      | region of your cluster, available in console                         |
| CLUSTER_ID          | your cluster id, available in console                                |
| ZEEBE_CLIENT_ID     | client id of application which has read + write permissions to Zeebe |
| ZEEBE_CLIENT_SECRET | client secret for ZEEBE_CLIENT_ID                                    | 
| OAUTH_TOKEN_URL     | https://login.cloud.camunda.io/oauth/token                           | 

## Configuration - Self-Managed (SM)
| Secret Name         | Description                                                          |
|---------------------|----------------------------------------------------------------------|
| ZEEBE_URL           | url of Zeebe Rest API, is typically the gateway |
| ZEEBE_CLIENT_ID     | client id of application which has read + write permissions to Zeebe |
| ZEEBE_CLIENT_SECRET | client secret for ZEEBE_CLIENT_ID                                    | 
| OAUTH_TOKEN_URL     | https://login.cloud.camunda.io/oauth/token                           | 