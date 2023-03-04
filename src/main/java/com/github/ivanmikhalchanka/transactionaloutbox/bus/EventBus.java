package com.github.ivanmikhalchanka.transactionaloutbox.bus;

import com.github.ivanmikhalchanka.transactionaloutbox.bus.exception.EventTopicNotConfiguredException;
import java.lang.reflect.Field;
import java.util.Optional;

public interface EventBus {

  void send(Object event);

  default String retrieveTopic(Object event) {
    if (event.getClass().isAnnotationPresent(EventTopic.class)) {
      EventTopic topic = event.getClass().getAnnotation(EventTopic.class);

      return topic.value();
    }

    throw new EventTopicNotConfiguredException(event.getClass());
  }

  default Optional<String> retrieveEventKey(Object event) {
    for (Field field : event.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(EventKey.class)) {
        field.setAccessible(true);

        try {
          return Optional.ofNullable(field.get(event)).map(Object::toString);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return Optional.empty();
  }
}
