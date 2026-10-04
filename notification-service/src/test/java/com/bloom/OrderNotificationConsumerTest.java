package com.bloom;

import static org.awaitility.Awaitility.await;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.bloom.common.event.OrderCreatedEvent;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

@QuarkusTest
public class OrderNotificationConsumerTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    MockMailbox mailbox;

    @Test
    void testOrderCreatedNotificationSendsEmail() {
        mailbox.clear();

        OrderCreatedEvent.OrderItemPayload item = new OrderCreatedEvent.OrderItemPayload(
                UUID.randomUUID(), "SKU-ROSE", "Red Rose Bouquet", new BigDecimal("29.99"), 2,
                new BigDecimal("59.98"));

        OrderCreatedEvent.AddressSnapshot address = new OrderCreatedEvent.AddressSnapshot(
                "Jane Doe", "1234567890", "123 Bloom St", null,
                "Springfield", "State", "12345", "US");

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                "ORD-TEST-9999",
                UUID.randomUUID(),
                "customer@example.com",
                List.of(item),
                address,
                new BigDecimal("59.98"),
                Instant.now());

        InMemorySource<OrderCreatedEvent> source = connector.source("order-created-in");
        source.send(event);

        await().atMost(Duration.ofSeconds(5))
                .until(() -> !mailbox.getMailsSentTo("customer@example.com").isEmpty());

        List<Mail> sent = mailbox.getMailsSentTo("customer@example.com");
        Assertions.assertFalse(sent.isEmpty(), "Email should have been sent to customer");
        Assertions.assertTrue(sent.get(0).getSubject().contains("ORD-TEST-9999"));
        Assertions.assertTrue(sent.get(0).getHtml().contains("Red Rose Bouquet"));
    }

}
