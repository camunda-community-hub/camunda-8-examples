#!/bin/bash

echo "Get the token"
TOKEN=$(curl -s --location --request POST 'http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=payment-app' \
--data-urlencode "client_secret=$CLIENT_SECRET" \
--data-urlencode 'grant_type=client_credentials' | jq -r .access_token)

echo "Get the task list"
IDLIST=$(curl -X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"state":"CREATED","sort":[{"field": "creationTime", "order": "ASC"}], "pageSize":200}' \
'http://localhost:8082/v1/tasks/search' | jq -r '.[].id')

echo "Complete the tasks"
while ID= read -r item; do 
  curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"variables": {"errorResolved": false}}' "http://localhost:8088/v2/user-tasks/$item/completion"; 
done <<< "$IDLIST"
