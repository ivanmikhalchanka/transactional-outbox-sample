package com.github.ivanmikhalchanka.transactionaloutbox.dao.mapper;

import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class UserRowMapper implements RowMapper<User> {

  @Override
  public User mapRow(ResultSet rs, int rowNum) throws SQLException {
    return User.builder()
        .id(rs.getLong("id"))
        .status(UserStatus.valueOf(rs.getString("status")))
        .email(rs.getString("email"))
        .build();
  }
}
