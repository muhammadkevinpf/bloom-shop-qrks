package com.bloom.order.messaging;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import com.bloom.order.model.OutboxEvent;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OutboxPublisher {

    private static final Logger LOG = Logger.getLogger(OutboxPublisher.class);

    @Inject
    @Channel("orders-out")
    Emitter<Record<String, String>> orderEventsEmitter;

    @Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> outboxEvents = OutboxEvent.findUnprocessedEvent(20);

        if (outboxEvents.isEmpty()) {
            return;
        }

        LOG.infof("Publishing %d outbox events to kafka", outboxEvents.size());

        for (OutboxEvent event : outboxEvents) {
            try {
                orderEventsEmitter.send(Record.of(event.aggregateId, event.payload));
                OutboxEvent.markAsProcessed(event.id);
                LOG.infof("Published outbox event id: %s for order: %s", event.id, event.aggregateId);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to publish outbox event id: %s", event.id);
            }
        }
    }
}
