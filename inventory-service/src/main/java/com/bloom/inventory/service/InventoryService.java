package com.bloom.inventory.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;

import com.bloom.common.event.InventoryReservedEvent;
import com.bloom.common.event.OrderCreatedEvent.OrderItemPayload;
import com.bloom.inventory.model.Inventory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InventoryService {

    private static final Logger LOG = Logger.getLogger(InventoryService.class);

    @Transactional
    public InventoryReservedEvent reserveStock(UUID orderId, List<OrderItemPayload> items) {
        Map<Inventory, Integer> toDeduct = new HashMap<>();

        // validate that all items have suficient stocks
        for (OrderItemPayload item : items) {
            Inventory inventory = Inventory.findByVariantId(item.variantId());

            // check if variant exist in inventory
            if (inventory == null) {
                String reason = String.format("Variant not found in inventory: %s (SKU: %s)", item.variantId(),
                        item.sku());

                LOG.warnf("Reservation failed for order %s: %s", orderId, reason);
                return InventoryReservedEvent.failure(orderId, reason);
            }

            // check if variant has sufficient stock
            if (inventory.availableStock < item.quantity()) {
                String reason = String.format("Insufficient stock for SKU '%s'. Requested: %d, Available: %d",
                        item.sku(), item.quantity(), inventory.availableStock);
                LOG.warnf("Reservation failed for order %s: %s", orderId, reason);

                return InventoryReservedEvent.failure(orderId, reason);
            }

            toDeduct.put(inventory, item.quantity());
        }

        toDeduct.forEach((inventory, qty) -> {
            inventory.availableStock -= qty;
            inventory.reservedStock += qty;
        });

        return InventoryReservedEvent.success(orderId);

    }
}
