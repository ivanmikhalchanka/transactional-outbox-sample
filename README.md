# Transactional outbox & Transaction Log Tailing sample

Sample implementation
of [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html)
and [Transaction log tailing](https://microservices.io/patterns/data/transaction-log-tailing.html)
patterns.

## Setup

1. **Install [Docker](https://www.docker.com/)**
2. **Run docker compose via:**

```
docker-compose up -d
```

3. **Verify Debezium sink init finished:**

- Open [Debezium UI](http://localhost:8084/)
- Reload page until `outbox-connector` appears

In case of sink not initialized within 5 minutes - check init container logs:

- check containers statuses:

```sh
docker ps -a
```

- find name of the container with `debezium-connect-config-init:latest` image and check logs:

```sh
docker logs -t transactional-outbox_debezium-connect-init_1
```

4. **(Optionally) add more events
   via [SpringDoc OpenApi3 UI](http://localhost:8081/swagger-ui/index.html)**

5. **Verify outbox events in kafka, e.g. via [KCat](https://github.com/edenhill/kcat):**

```shell
kcat -C -b localhost:29092 -t user.statusChange
```

In case of sink not initialized within 5 minutes - check init container logs:

- check containers statuses:

```sh
docker ps -a
```

- find name of the container with `outbox-app-init:latest` image and check logs:

```sh
docker logs -t transactional-outbox_outbox-app-init_1
```

### Useful links:

- [SpringDoc OpenApi3 UI](http://localhost:8080/swagger-ui/index.html)
- [Debezium UI](http://localhost:8084)

## Implementation details

### Problem to solve

Let's focus on user management service and find out how to implement user status change events.

### Implementation 1: publish message after DB updated in scope of transaction:

```java
// @formatter:off
@Transactional(rollbackFor = Throwable.class)
public User updateStatus(long id, UserStatus status, String modifiedBy) throws TaskNotFoundException {
    User user = userDao.updateStatus(id, status);
    kafkaTemplate.send("user.statusChanged", id, new UserStatusChangedEvent(id, status, modifiedBy));

    return user;
}
```

This approach has next issues:

- Event can be consumed before transaction committed
- In case of `kafkaTemplate` fail to send message to kafka without an exception - event will be lost

### Implementation 2: transactional outbox table + transaction log tailing

1. #### Create outbox table for status changed event:

```postgresql
CREATE TABLE app_user_status_change_outbox
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           TEXT        NOT NULL REFERENCES app_user (id),
    status            TEXT        NOT NULL,
    modified_by       TEXT        NOT NULL,
    created_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

2. #### Update status and insert record to the outbox table:

```postgresql
-- @formatter:off
PERFORM *
FROM app_user
WHERE id = _id
    FOR UPDATE;

IF NOT FOUND THEN
    RAISE 'User with id  % not found', _id;
END IF;

UPDATE app_user
SET status = _status
WHERE id = _id;

INSERT INTO app_user_status_change_outbox
    (user_id, status, modified_by)
VALUES (_id, _status, _modified_by);
```

4. #### Configure DB

- Postgres: set `wal_level=logical`, e.g. via docker compose:

```yaml
postgres:
  image: postgres:14.5
  ...
  command: [ "postgres", "-c", "wal_level=logical" ]
```

5. #### Configure CDC tool for outbox table that publish messages to Kafka

CDC - [Change Data Capture](https://en.wikipedia.org/wiki/Change_data_capture).

For example [Debezium](https://debezium.io/):

- enable logical decoding output plugin or use something enabled by default
  e.g. [pgoutput](https://access.redhat.com/documentation/en-us/red_hat_integration/2019-12/html/change_data_capture_user_guide/debezium-connector-for-postgresql#output-plugin)
- run debezium image

```yaml
debezium-connect:
  image: debezium/connect:1.9.5.Final
  container_name: outbox-debezium-connect
  depends_on:
    - kafka
    - postgres
  ports:
    - 8083:8083
  environment:
    - BOOTSTRAP_SERVERS=kafka:9092
    - GROUP_ID=dbz
    - CONFIG_STORAGE_TOPIC=my_connect_configs
    - OFFSET_STORAGE_TOPIC=my_connect_offsets
    - STATUS_STORAGE_TOPIC=my_connect_statuses
```

- create connector config connector (`connector-config.json`):

```json
{
  "name": "outbox-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "sample-app-user",
    "database.password": "password",
    "database.dbname": "sample-db",
    "database.server.name": "postgres",
    "plugin.name": "pgoutput",
    "table.whitelist": "public.app_user_status_change_outbox",
    "publication.name": "outbox_publication",
    "publication.autocreate.mode": "filtered"
  }
}

```

- post this config to the Debezium:

```shell
curl -i -X POST localhost:8083/connectors/ \
  -H "Accept:application/json" \
  -H "Content-Type:application/json" \
  --data-binary "@./outbox-connector.json"
```

## Additional configs required to use this approach in production

- Configure separate DB user for Debezium instead of granting it superuser privileges,
  e.g. [Setting up Postgres permissions](https://debezium.io/documentation/reference/1.9/connectors/postgresql.html#postgresql-permissions)

- Configure topic rerouting
  via [SMT](https://docs.confluent.io/platform/current/connect/transforms/overview.html), e.g. `connector-config.json`:

```json
{
  "name": ...
  "config": {
    ...
    "transforms": "reroute,...",
    "transforms.reroute.type": "io.debezium.transforms.ByLogicalTableRouter",
    "transforms.reroute.topic.regex": "(.*)user_status_change(.*)",
    "transforms.reroute.topic.replacement": "user.statusChange"
  }
}
```

- configure events transformations (`connector-config.json`):

```json
{
  "name": ...
  "config": {
    ...
    "transforms": "extractAfter,...",
    "transforms.extractAfter.type": "org.apache.kafka.connect.transforms.ExtractField$Value",
    "transforms.extractAfter.field": "after"
  }
}
```
