package com.bloom.order.messaging;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import com.bloom.common.event.InventoryReservedEvent;
import com.bloom.order.model.Order;
import com.bloom.order.model.OrderStatus;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InventoryEventConsumer {

    private static final Logger LOG = Logger.getLogger(InventoryEventConsumer.class);

    @Incoming("inventory-in")
    @Blocking
    @Transactional
    public void consumeInventoryReserved(InventoryReservedEvent event) {
        LOG.infof("Received InventoryReservedEvent for orderId: %s (success: %s)", event.orderId(), event.success());

        OrderStatus targetStatus = event.success() ? OrderStatus.CONFIRMED : OrderStatus.CANCELLED;

        boolean updated = Order.updateStatus(event.orderId(), targetStatus);

        if (updated) {
            LOG.infof("Order %s status successfully updated to %s", event.orderId(), targetStatus);
        } else {
            LOG.warnf("Could not find order %s to update status to %s", event.orderId(), targetStatus);
        }

    }
}
