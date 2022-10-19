#!/bin/sh

echo 'Initialization of Debezium connect started'

num_of_attempts=0
response_code=0

while [ $num_of_attempts -le 30 ] && { [ "$response_code" -lt 200 ] || [ "$response_code" -ge 300 ]; }; do
  num_of_attempts=$((num_of_attempts + 1))
  sleep 5s

  response_code=$(
    curl -i -s -o /dev/null -w "%{http_code}" -X POST debezium-connect:8083/connectors/ \
      -H "Accept:application/json" \
      -H "Content-Type:application/json" \
      --data-binary "@/usr/local/bin/connector-init.json"
  )

  echo "$num_of_attempts num_of_attempts to init Debezium connect, response code: $response_code"
done

if [ "$response_code" -ge 200 ] && [ "$response_code" -lt 300 ]; then
  echo 'Initialization successfully processed'
else
  echo "Initialization failed due to timeout, response code: $response_code"
fi
