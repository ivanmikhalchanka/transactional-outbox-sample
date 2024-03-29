package com.github.ivanmikhalchanka.transactionaloutbox.dao;

import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;

public interface UserDao {
  User create(String email);

  User updateStatus(long id, UserStatus status);
}
