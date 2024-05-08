[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Use a Camunda Process to Backup Camunda 8 SM
 
Creating a successful backup of Camunda 8 Self Managed involves the steps described [here](https://docs.camunda.io/docs/self-managed/operational-guides/backup-restore/backup-and-restore/).

This project contains several `bpmn` process definitions that automate these steps. 

> [!NOTE]  
> This solution should not be used in Camunda SaaS environments. This only works in Camunda 8 Self Managed. See [this documentation](https://docs.camunda.io/docs/components/concepts/backups/) for information about managing Backups in SaaS.  

Here are the steps to create a Camunda 8 SM backup using this project: 

1. Make sure to configure the prerequisites below.
2. Deploy all the artifacts found inside the [models](src/main/resources/models) directory. 
3. Start an instance of the [backup.bpmn](src/main/resources/models/backup.bpmn) process to create a backup with the following payload:

```json
{
  "s3BucketName": "YOUR_BUCKET_NAME" 
}
```

If convenient, this project also contains a Spring Boot App which will auto deploy the resources. Configure [application.properties](src/main/resources/application.properties) to point to your zeebe gateway, and start the app. 

Then send a `POST` to `https://localhost:8080/process/start`

If you'd like to edit the bpmn files, you'll need to configure the Rest Element Template

If you're using Web Modeler, then publish the [http-json-connector.json](src/main/resources/http-json-connector.json) connector template.

If you're using Desktop Modeler, save the [http-json-connector.json](src/main/resources/http-json-connector.json) into `resources/element-templates` as [described here](https://docs.camunda.io/docs/next/components/modeler/desktop-modeler/element-templates/configuring-templates/)

# Prerequisites for S3

## Setup an S3 Bucket

1. Create a S3 bucket
2. Grant access to s3 bucket and create credentials

There are many ways to configure access to an S3 bucket and covering all the ways to configure AWS authorization is definitely out of scope for this guide. But, I'll share one technique here for convenience. One technique is to grant access to an IAM user using an inline policy like this:
```json
{
	"Version": "2012-10-17",
	"Statement": [
	    {
	        "Action": [
	            "s3:*"
	   ],
	   "Effect": "Allow",
	   "Resource": [
        "arn:aws:s3:::bucket-name",
        "arn:aws:s3:::bucket-name/*"
      ] 
    }
	]
}
```
Then, create an [Access Key](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html) for your IAM user.

Create a Kubernetes Secret to store your AWS key and secret from the access key like so: 

```shell
   kubectl create secret generic aws-credentials --from-literal=key=YOUR_AWS_KEY --from-literal=secret=YOUR_AWS_SECRET
```

# Configure Elasticsearch

> [!NOTE]  
> This step is only necessary for Camunda versions <= 8.2.x. Camunda 8.2.x includes Elasticsearch version 7.17.x. By default, these versions of Elasticsearch don't include the S3 snapshot repository by default.

Find the version of Elasticsearch that is currently installed in your environment. [This version matrix](https://helm.camunda.io/camunda-platform/version-matrix) lists the version of Elasticsearch installed by default for each version of Camunda. 

Then, add the following `initContainer` definition to your Camunda `values.yaml` file under the [elasticsearch](https://github.com/camunda/camunda-platform-helm/tree/main/charts/camunda-platform#elasticsearch-parameters) section: 

> [!INFO]  
> Remember to change the version (`YOUR_VERSION` below) of the elasticsearch image to match the current version of your existing elasticsearch 

```shell
elasticsearch:
  extraInitContainers:
    - name: s3
      image: elasticsearch:YOUR_VERSION
      securityContext:
        privileged: true
      command:
        - sh
      args:
        - -c
        - |
          ./bin/elasticsearch-plugin install --batch repository-s3
          cp -a /usr/share/elasticsearch/plugins /usr/share
          ls -altr /usr/share/plugins
          echo $AWS_KEY | ./bin/elasticsearch-keystore add -f --stdin s3.client.default.access_key
          echo $AWS_SECRET | ./bin/elasticsearch-keystore add -f --stdin s3.client.default.secret_key
          cp -a /usr/share/elasticsearch/config/elasticsearch.keystore /usr/share/config
          ls -altr /usr/share/config
          echo "s3 plugin is ready!"
      volumeMounts:
        - name: plugins
          mountPath: /usr/share/plugins
        - name: keystore
          mountPath: /usr/share/config
      env:
        - name: AWS_KEY
          valueFrom:
            secretKeyRef:
              name: aws-credentials
              key: key
        - name: AWS_SECRET
          valueFrom:
            secretKeyRef:
              name: aws-credentials
              key: secret
  extraVolumes:
    - name: plugins
      emptyDir: {}
    - name: keystore
      emptyDir: {}
  extraVolumeMounts:
    - name: plugins
      mountPath: /usr/share/elasticsearch/plugins
      readOnly: false
    - name: keystore
      mountPath: /usr/share/elasticsearch/config/elasticsearch.keystore
      subPath: elasticsearch.keystore
```

# Configure Camunda Apps

## Configure Tasklist

```shell
tasklist:
  env:
    - name: CAMUNDA_TASKLIST_BACKUP_REPOSITORY_NAME
      value: "tasklist-backup"
```

## Configure Operate

```shell
operate:
  env:
    - name: CAMUNDA_OPERATE_BACKUP_REPOSITORY_NAME
      value: "operate-backup"
```

## Configure Optimize

```shell
optimize:
  env:
    - name: CAMUNDA_OPTIMIZE_BACKUP_REPOSITORY_NAME
      value: "optimize-backup"
```

## Configure Zeebe

```shell
zeebe: 
  env:
    - name: ZEEBE_BROKER_EXECUTION_METRICS_EXPORTER_ENABLED
      value: "true"
    - name: ZEEBE_BROKER_DATA_BACKUP_STORE
      value: "S3"
    - name: ZEEBE_BROKER_DATA_BACKUP_S3_BUCKETNAME
      value: "YOUR_BUCKET_NAME"
    - name: ZEEBE_BROKER_DATA_BACKUP_S3_BASEPATH
      value: "zeebe-backup"
    - name: ZEEBE_BROKER_DATA_BACKUP_S3_ACCESSKEY
      valueFrom:
        secretKeyRef:
          name: aws-credentials
          key: key
    - name: ZEEBE_BROKER_DATA_BACKUP_S3_SECRETKEY
      valueFrom:
        secretKeyRef:
          name: aws-credentials
          key: secret
```






