package com.github.ivanmikhalchanka.transactionaloutbox.bus.event;

import com.github.ivanmikhalchanka.transactionaloutbox.bus.EventKey;
import com.github.ivanmikhalchanka.transactionaloutbox.bus.EventTopic;
import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import com.github.ivanmikhalchanka.transactionaloutbox.utils.SecureLogging;

@EventTopic("user.statusChange")
public class UserStatusChangeEvent {
  @EventKey private final Long userId;
  private final UserStatus status;
  private final String modifiedBy;

  public UserStatusChangeEvent(Long userId, UserStatus status, String modifiedBy) {
    this.userId = userId;
    this.status = status;
    this.modifiedBy = modifiedBy;
  }

  public Long getUserId() {
    return userId;
  }

  public UserStatus getStatus() {
    return status;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  @Override
  public String toString() {
    return SecureLogging.toJsonString(this);
  }
}
