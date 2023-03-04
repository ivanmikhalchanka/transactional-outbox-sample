package com.github.ivanmikhalchanka.transactionaloutbox.bus.postgres;

import com.github.ivanmikhalchanka.transactionaloutbox.bus.EventBus;
import com.github.ivanmikhalchanka.transactionaloutbox.utils.DaoUtils;
import com.github.ivanmikhalchanka.transactionaloutbox.utils.JsonEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
