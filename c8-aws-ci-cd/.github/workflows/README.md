# Github Action Workflows
This repository contains several Github Action Workflows. You will find a description of eah one below.


## build-and-push-to-aws-yml
This workflow is used to build a Docker image containing the worker jar, and deploy it to AWS ECR and EKS.
For the required secrets, refer to [configuration for AWS](../README.md).
For configuration options of each step, refer to the links provided in the table below.

It has the following steps:


| Step Name                                | Description                                                                                                                         | Links                                                    |
|------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| Checkout Repository                      | checkout entire repository                                                                                                          |                                                          |
| Set up JDK                               | set up the jdk to use                                                                                                               | https://github.com/actions/setup-java                    |
| Build with Maven                         | build via mvn package. can choose to run tests here or any maven command. copy target folder to docker folder for docker build step |                                                          |
| Configure AWS credentials                | provide AWS credentials to access ECR and EKS                                                                                       | https://github.com/aws-actions/configure-aws-credentials |
| Login to Amazon ECR                      | perform AWS login                                                                                                                   | https://github.com/aws-actions/amazon-ecr-login          |
| Build, tag, and push image to Amazon ECR | switch to docker folder. build the docker image from dockerfile, tag with the git sha, and push to ECR.                             |                                                          |
| Update kube config                       | set the kubectl context to the desired EKS cluster                                                                                  |                                                          |
| Deploy to EKS                            | use kubectl apply to update worker deployment with latest image                                                                     |                                                          |


## deploy-bpmn-resources-saas.yaml and deploy-bpmn-resources-sm.yaml
These workflows are used to locate added and changed BPMNs, DMNs, and Forms. It then deploys these resources to a Camunda SaaS cluster, or a Camunda Selfmanaged (SM) cluster. 

For the required secrets, refer to [configuration for SaaS](../README.md) and [configuration for SM](../README.md). 

For configuration options of each step, refer to the links provided in the tables below.

### 🔵 job: prepare-files

This job is responsible for collecting changed and added files.

| Step Name                                    | Description                                                                                                                                  | Links                                       |
|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|
| Select added and changed resources to deploy | outputs a json array of added and modified *.bpmn, *.dmn, and *.form files in the src/main/resources folder. deleted files are not included. | https://github.com/yumemi-inc/changed-files |

### 🔵 job: deploy-modified-files

This job is responsible for deploying the files listed in the previous job. It iterates over the list of file paths, and deploys them one at a time.
It only executes if at least one file was found in the previous job.

| Step Name                     | Description                                                                                                                            | Links                                                        |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| Check Out File for Deployment | checkout only the file to be deployed in this iteration                                                                                | https://github.com/actions/checkout#fetch-only-a-single-file |
| Echo File                     | echo the file name for reference                                                                                                       |                                                              |
| Get Bearer Token              | request authorization from oauth token url. This requires a client id and secret which has access to Zeebe. Output is stored in $token |                                                              |
| Deploy to Zeebe               | deploy the file to the cluster via the Zeebe REST API with the token from the previous step                                            |                                                              |

