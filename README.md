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

1. #### Create outbox table for events:

```sql
CREATE TABLE IF NOT EXISTS app_transactional_outbox_event
(
    id                BIGSERIAL PRIMARY KEY,
    topic             TEXT NOT NULL,
    key               TEXT,
    event             TEXT NOT NULL,
    created_timestamp TIMESTAMPTZ DEFAULT NULL
);
```

2. #### Implement EventBus that persists events into this table instead of Kafka:
 
```java
@Repository
public class PostgresTransactionalOutboxEventBus implements EventBus {
  private final JdbcTemplate jdbcTemplate;

  public PostgresTransactionalOutboxEventBus(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void send(Object event) {
    jdbcTemplate.query(
        "SELECT * FROM app_transactional_outbox_event_create(?, ?, ?)",
        DaoUtils.VOID_ROW_MAPPER,
        retrieveTopic(event),
        JsonEncoder.toJson(event),
        retrieveEventKey(event).orElse(null));
  }
}
```

3. #### Update status and persist event to DB through new event bus:

- Since new event bus simply persists event to the DB - it is possible to do both update and event publishing withing one DB transaction

```java
-- @formatter:off
@Transactional(rollbackFor = Throwable.class)
public User updateStatus(long userId, UserStatus status, String modifiedBy) {
  User user = userDao.updateStatus(userId, status);

  eventBus.send(new UserStatusChangeEvent(user.getId(), user.getStatus(), modifiedBy));

  return user;
}
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

- create connector config connector (`connector-init.json`):

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
  "name": "outbox-connector",
  "config": {
    ...
    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.route.by.field": "topic",
    "transforms.outbox.route.topic.replacement": "${routedByValue}",
    "transforms.outbox.table.field.event.id": "id",
    "transforms.outbox.table.field.event.key": "key",
    "transforms.outbox.table.field.event.payload": "event"
  }
}
```
