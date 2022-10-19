package com.github.ivanmikhalchanka.transactionaloutbox.web.rest.dto;

import com.github.ivanmikhalchanka.transactionaloutbox.entity.UserStatus;
import com.github.ivanmikhalchanka.transactionaloutbox.utils.SecureLogging;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusChangeRequest {
  private UserStatus status;
  private String modifiedBy;

  @Override
  public String toString() {
    return SecureLogging.toJsonString(this);
  }
}
