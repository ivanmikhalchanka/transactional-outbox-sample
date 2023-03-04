package com.github.ivanmikhalchanka.transactionaloutbox.service;

import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;

public interface UserService {
  User create(String email, String createdBy);

  User updateStatus(long id, UserStatus status, String modifiedBy);
}
