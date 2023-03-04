package com.github.ivanmikhalchanka.transactionaloutbox.utils;

import org.springframework.jdbc.core.RowMapper;

public class DaoUtils {
  public static final RowMapper<Void> VOID_ROW_MAPPER = (rs, i) -> null;
}
