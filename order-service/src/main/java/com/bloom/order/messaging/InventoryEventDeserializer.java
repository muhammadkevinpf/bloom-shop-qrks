package com.bloom.order.messaging;

import com.bloom.common.event.InventoryReservedEvent;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class InventoryEventDeserializer extends ObjectMapperDeserializer<InventoryReservedEvent> {
    public InventoryEventDeserializer() {
        super(InventoryReservedEvent.class);
    }
}
