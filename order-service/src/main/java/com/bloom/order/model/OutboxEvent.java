package com.bloom.order.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "aggregate_type", nullable = false)
    public String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    public String aggregateId;

    @Column(name = "event_type", nullable = false)
    public String eventType;

    @Column(name = "destination_topic", nullable = false)
    public String destinationTopic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    public String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "processed", nullable = false)
    public boolean processed = false;

    @Column(name = "processed_at")
    public Instant processedAt;

    // helper query
    public static List<OutboxEvent> findUnprocessedEvent(int batchSize) {
        return find("processed = false", Sort.ascending("createdAt"))
                .page(Page.of(0, batchSize))
                .list();
    }

    public static boolean markAsProcessed(UUID id) {
        return update("processed = true, processedAt = ?1 where id = ?2 and processed = false", Instant.now(), id) > 0;
    }

    public static OutboxEvent of(String aggregateType, String aggregateId, String eventType, String topic,
            String jsonPayload) {
        OutboxEvent event = new OutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.destinationTopic = topic;
        event.payload = jsonPayload;
        event.processed = false;
        return event;
    }
}
