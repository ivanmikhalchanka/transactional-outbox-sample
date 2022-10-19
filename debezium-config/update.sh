curl -i -X PUT localhost:8083/connectors/outbox-connector/config \
  -H "Accept:application/json" \
  -H "Content-Type:application/json" \
  --data-binary "@./config-update.json"
