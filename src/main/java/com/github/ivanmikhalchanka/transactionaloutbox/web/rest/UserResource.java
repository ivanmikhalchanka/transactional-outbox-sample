package com.github.ivanmikhalchanka.transactionaloutbox.web.rest;

import com.github.ivanmikhalchanka.transactionaloutbox.dao.UserDao;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.web.rest.dto.UserRegisterRequest;
import com.github.ivanmikhalchanka.transactionaloutbox.web.rest.dto.UserStatusChangeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserResource {
  private final UserDao userDao;

  public UserResource(UserDao userDao) {
    this.userDao = userDao;
  }

  @PostMapping("/v1/register")
  public User registerUser(@RequestBody UserRegisterRequest request) {
    log.info("UserRegisterRequest: {}", request);

    return userDao.create(request.getEmail(), request.getModifiedBy());
  }

  @PostMapping("/v1/{userId}/status")
  public User changeStatus(
      @PathVariable Long userId, @RequestBody UserStatusChangeRequest request) {
    log.info("UserStatusChangeRequest: {}", request);

    return userDao.updateStatus(userId, request.getStatus(), request.getModifiedBy());
  }
}
