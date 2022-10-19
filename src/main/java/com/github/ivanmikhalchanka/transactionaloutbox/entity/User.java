package com.github.ivanmikhalchanka.transactionaloutbox.entity;

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
public class User {
  private Long id;
  private String email;
  private UserStatus status;

  @Override
  public String toString() {
    return SecureLogging.toJsonString(this);
  }
}
