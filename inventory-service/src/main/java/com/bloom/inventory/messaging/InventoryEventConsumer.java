package com.bloom.inventory.messaging;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import com.bloom.common.event.InventoryReservedEvent;
import com.bloom.common.event.OrderCreatedEvent;
import com.bloom.inventory.service.InventoryService;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InventoryEventConsumer {

    private static final Logger LOG = Logger.getLogger(InventoryEventConsumer.class);

    @Inject
    InventoryService inventoryService;

    @Incoming("orders-in")
    @Outgoing("inventory-out")
    @Blocking
    public InventoryReservedEvent processOrderCreated(OrderCreatedEvent event) {
        LOG.infof("Received OrderCreatedEvent for order: %s", event.orderId());

        InventoryReservedEvent result = inventoryService.reserveStock(event.orderId(), event.items());

        if (result.success()) {
            LOG.infof("Stock successfully reserved for order: %s", event.orderId());
        } else {
            LOG.warnf("Stock reservation failed for order: %s", event.orderId());
        }

        return result;
    }
}
