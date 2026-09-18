package com.bloom.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.bloom.common.event.OrderCreatedEvent;
import com.bloom.common.event.OrderCreatedEvent.OrderItemPayload;
import com.bloom.order.dto.CheckoutRequest;
import com.bloom.order.model.CartItem;
import com.bloom.order.model.Customer;
import com.bloom.order.model.Order;
import com.bloom.order.model.OrderItem;
import com.bloom.order.model.OrderStatus;
import com.bloom.order.model.OutboxEvent;
import com.bloom.order.model.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class OrderService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    @Inject
    ObjectMapper objectMapper;

    public String generateOrderNumber() {
        String datePrefix = DateTimeFormatter.ofPattern("yyMMdd")
                .format(LocalDateTime.now());
        String randomSuffix = random.ints(4, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
        return "BLM-" + datePrefix + "-" + randomSuffix;
    }

    public void calculateTotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : order.items) {
            BigDecimal itemPrice = item.unitPrice.multiply(BigDecimal.valueOf(item.quantity));
            subtotal = subtotal.add(itemPrice);
        }

        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("100.00")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("15.00");

        BigDecimal taxRate = new BigDecimal("0.11");
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountAmount = BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal.add(shippingFee)
                .add(taxAmount)
                .subtract(discountAmount);

        order.subtotalAmount = subtotal;
        order.shippingFee = shippingFee;
        order.taxAmount = taxAmount;
        order.discountAmount = discountAmount;
        order.totalAmount = totalAmount;
    }

    @Transactional
    public Order checkout(UUID customerId, CheckoutRequest request) {
        Customer customer = Customer.findById(customerId);
        if (customer == null) {
            throw new NotFoundException("Customer not found");
        }

        List<CartItem> cartItems = CartItem.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        Order order = new Order();
        order.orderNumber = generateOrderNumber();
        order.customer = customer;
        order.status = OrderStatus.PENDING;
        order.paymentStatus = PaymentStatus.PENDING;
        order.shippingAddress = request.shippingAddress();
        order.billingAddress = request.billingAddress() != null ? request.billingAddress() : request.shippingAddress();
        order.notes = request.notes();

        List<OrderItemPayload> eventItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.order = order;
            orderItem.variantId = cartItem.variantId;
            // still using placeholder
            orderItem.sku = "SKU-" + cartItem.variantId.toString().substring(0, 8).toUpperCase();
            orderItem.productName = "Product Item";
            orderItem.unitPrice = new BigDecimal("50.00");
            // still using placeholder
            orderItem.quantity = cartItem.quantity;
            orderItem.totalPrice = orderItem.unitPrice.multiply(BigDecimal.valueOf(cartItem.quantity));

            order.items.add(orderItem);

            eventItems.add(new OrderItemPayload(
                    orderItem.variantId,
                    orderItem.sku,
                    orderItem.productName,
                    orderItem.unitPrice,
                    orderItem.quantity,
                    orderItem.totalPrice));
        }

        calculateTotal(order);
        order.persist();

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.id,
                order.orderNumber,
                customer.id,
                customer.email,
                eventItems,
                order.shippingAddress,
                order.totalAmount,
                Instant.now());

        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    "Order",
                    order.id.toString(),
                    "OrderCreated",
                    "bloom.orders.created",
                    jsonPayload);
            outboxEvent.persist();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }

        CartItem.deleteByCustomerId(customerId);

        return order;
    }

    public Order getOrderByNumber(String orderNumber, UUID customerId) {
        Order order = Order.findByOrderNumber(orderNumber);
        if (order == null || !order.customer.id.equals(customerId)) {
            throw new NotFoundException("Order not found: " + orderNumber);
        }
        return order;
    }
}
