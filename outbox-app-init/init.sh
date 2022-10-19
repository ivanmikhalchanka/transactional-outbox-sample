#!/bin/sh

echo 'Initialization of application events started'

num_of_attempts=0
response_code=0

while [ $num_of_attempts -le 30 ] && { [ "$response_code" -lt 200 ] || [ "$response_code" -ge 300 ]; }; do
  num_of_attempts=$((num_of_attempts + 1))
  sleep 5s

  response_code=$(curl -i -s -o /dev/null -w "%{http_code}" -X GET outbox-app:8080/actuator/health)

  echo "$num_of_attempts num_of_attempts to init application events, response code: $response_code"
done

if [ "$response_code" -ge 200 ] && [ "$response_code" -lt 300 ]; then
  echo 'Application healthcheck returned 2xx, proceeding with events initialization'

  curl -X POST outbox-app:8080/api/users/v1/register -H 'accept: */*' -H 'Content-Type: application/json' -d '{ "email": "t-1@outbox.com", "modifiedBy": "admin-1" }'
  curl -X POST outbox-app:8080/api/users/v1/register -H 'accept: */*' -H 'Content-Type: application/json' -d '{ "email": "t-2@outbox.com", "modifiedBy": "admin-2" }'

  curl -X POST outbox-app:8080/api/users/v1/1/status -H 'accept: */*' -H 'Content-Type: application/json' -d '{ "status": "SUSPENDED", "modifiedBy": "admin-2" }'
  curl -X POST outbox-app:8080/api/users/v1/2/status -H 'accept: */*' -H 'Content-Type: application/json' -d '{ "status": "ACTIVE", "modifiedBy": "admin-1" }'
else
  echo "Initialization failed due to timeout, response code: $response_code"
fi
