package com.github.ivanmikhalchanka.transactionaloutbox.dao.postgres;

import com.github.ivanmikhalchanka.transactionaloutbox.dao.UserDao;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDaoPostgres implements UserDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<User> mapper;

  public UserDaoPostgres(JdbcTemplate jdbcTemplate, RowMapper<User> mapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.mapper = mapper;
  }

  @Override
  public User create(String email) {
    return jdbcTemplate.queryForObject("SELECT * FROM app_user_create(?)", mapper, email);
  }

  @Override
  public User updateStatus(long id, UserStatus status) {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM app_user_update_status(?, ?)", mapper, id, status.name());
  }
}
