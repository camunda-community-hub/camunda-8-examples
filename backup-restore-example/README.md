# backup and restore

## Overview
This is an example application, that shows how Camunda 8 Backup and Restore capabilities can be implemented.  
<b>It is supposed to serve as an inspiration, and is not intended to be used in production!</b>  

Adjust application.yaml and the values.yamls files under ./values to match your setup.

## backup and restore run

### setup platform with some sample data
1. helm install
```bash
helm install --namespace camunda camunda camunda/camunda-platform -f values/step1_values.yaml --skip-crds --version 8.3.1
```
2. port-forward (so urls configured in application.yaml can be reached)
3. start java application
4. prepare backup
```bash
curl --location 'http://localhost:8080/backup/prepare-backup' \
--header 'Content-Type: application/json' \
--data '{}'
```
5. create sample data
```bash
curl --location 'http://localhost:8080/sample-data/createData' \
--header 'Content-Type: application/json' \
--data '{
"startNumber":1,
"iterations":20
}'
```

### backup
6. backup
```bash
curl --location 'http://localhost:8080/backup/backup' \
--header 'Content-Type: application/json' \
--data '{
"backupId": 91
}'
```

### delete sample data
8. scale down WebApps and Zeebe via helm upgrade
```bash
helm upgrade --namespace camunda camunda camunda/camunda-platform -f values/step2_values.yaml --skip-crds \
--set global.identity.auth.connectors.existingSecret=test \
--set global.identity.auth.console.existingSecret=test --version 8.3.1
```
9. delete ES indices to have a clean ES
```bash
curl --location 'http://localhost:8080/delete/indices' \
--header 'Content-Type: application/json' \
--data '{}'
```

10. delete zeebe pvc
```bash
kubectl delete pvc data-camunda-zeebe-0 data-camunda-zeebe-1 data-camunda-zeebe-2
```

### restore
11. restore ES snapshots
```bash
curl --location 'http://localhost:8080/restore/restore' \
--header 'Content-Type: application/json' \
--data '{
    "backupId": 91
}'
```
12. adapt values.yaml (<b>important:</b> Update ZEEBE_RESTORE_FROM_BACKUP_ID)
13. helm upgrade
```bash
helm upgrade --namespace camunda camunda camunda/camunda-platform -f values/step3_values.yaml --skip-crds \
--set global.identity.auth.connectors.existingSecret=test \
--set global.identity.auth.console.existingSecret=test --version 8.3.1
```