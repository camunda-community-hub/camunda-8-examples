# Kubernetes Manifests

## client-secret.yaml
This manifest is an example of how to deploy your client secret as a Kubernetes secret.

:information_desk_person: You want to change the metadata namespace and name to match your use case.

:information_desk_person: Replace **CLIENT SECRET IN BASE64** with your base64 encoded client secret.

## worker-deploy.yaml
This manifest is used to deploy your worker to the EKS cluster.

:information_desk_person: Be sure to change the metadata.namespace and metadata.name

:information_desk_person: Be sure to change selector.matchLabels.app and template.metadata.labels.app

:information_desk_person: You can edit your worker's connection settings in the env section of this manifest.