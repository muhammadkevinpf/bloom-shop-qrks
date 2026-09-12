package com.bloom.inventory.messaging;

import com.bloom.common.event.OrderCreatedEvent;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OrderCreatedEventDeserializer extends ObjectMapperDeserializer<OrderCreatedEvent>{
    public OrderCreatedEventDeserializer() {
        super(OrderCreatedEvent.class);
    }
}
