package com.bloom.notification.consumer;

import com.bloom.common.event.OrderCreatedEvent;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OrderCreatedEventDeserializer extends ObjectMapperDeserializer<OrderCreatedEvent> {
    public OrderCreatedEventDeserializer() {
        super(OrderCreatedEvent.class);
    }
}
