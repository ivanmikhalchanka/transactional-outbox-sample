package com.github.ivanmikhalchanka.transactionaloutbox.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;

public class SecureLogging {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private static final Set<String> SECURE_FIELDS = new HashSet<>(Arrays.asList("email"));

  @SneakyThrows
  public static String toJsonString(final Object object) {
    Map<String, Object> maskedFields = new HashMap<>();

    for (Field field : object.getClass().getDeclaredFields()) {
      Object value = retrieveMaskedValue(field, object);

      maskedFields.put(field.getName(), value);
    }

    return MAPPER.writeValueAsString(maskedFields);
  }

  private static Object retrieveMaskedValue(Field field, Object object) {
    field.setAccessible(true);

    try {
      Object value = field.get(object);

      if (value instanceof String && SECURE_FIELDS.contains(field.getName())) {
        value = ((String) value).charAt(0) + "***";
      }

      return value;
    } catch (IllegalAccessException ignored) {
    }

    return null;
  }
}
