package com.github.ivanmikhalchanka.transactionaloutbox.service;

import com.github.ivanmikhalchanka.transactionaloutbox.bus.EventBus;
import com.github.ivanmikhalchanka.transactionaloutbox.bus.event.UserStatusChangeEvent;
import com.github.ivanmikhalchanka.transactionaloutbox.dao.UserDao;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.User;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
  private final UserDao userDao;
  private final EventBus eventBus;

  public UserServiceImpl(UserDao userDao, EventBus eventBus) {
    this.userDao = userDao;
    this.eventBus = eventBus;
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public User create(String email, String createdBy) {
    User user = userDao.create(email);

    eventBus.send(new UserStatusChangeEvent(user.getId(), user.getStatus(), createdBy));

    return user;
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public User updateStatus(long userId, UserStatus status, String modifiedBy) {
    User user = userDao.updateStatus(userId, status);

    eventBus.send(new UserStatusChangeEvent(user.getId(), user.getStatus(), modifiedBy));

    return user;
  }
}
