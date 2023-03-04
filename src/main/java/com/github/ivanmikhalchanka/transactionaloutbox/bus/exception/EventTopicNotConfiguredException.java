package com.github.ivanmikhalchanka.transactionaloutbox.bus.exception;

public class EventTopicNotConfiguredException extends RuntimeException {

  public EventTopicNotConfiguredException(Class<?> eventType) {
    super(String.format("EventTopic annotation not presented for event type: %s",
        eventType.getName()));
  }
}
