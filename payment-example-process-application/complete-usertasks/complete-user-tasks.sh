#!/bin/bash

echo "Get the token"
TOKEN=$(curl -s --location --request POST 'http://localhost:18080/realms/ingo-keycloak-as-oidc/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=payment-app' \
--data-urlencode "client_secret=$CLIENT_SECRET" \
--data-urlencode 'grant_type=client_credentials' | jq -r .access_token)

echo "Token:" $TOKEN
echo "Get the task list"
IDLIST=$(curl -s -X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"filter":{"state":"CREATED","processDefinitionId":"paymentProcess"},"sort":[{"field": "creationDate", "order": "ASC"}], "page":{"from":0, "limit":200}}' \
'http://localhost:8088/v2/user-tasks/search' | jq -r '.items[].userTaskKey')

echo -n "Number of tasks to complete: "
printf '%s' "$IDLIST" | jq -Rsc 'split("\n") | map(select(length>0)) | length'

echo "Complete the tasks"
while ID= read -r item; do 
  curl -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"variables": {"errorResolved": false}}' "http://localhost:8088/v2/user-tasks/$item/completion"; 
done <<< "$IDLIST"
