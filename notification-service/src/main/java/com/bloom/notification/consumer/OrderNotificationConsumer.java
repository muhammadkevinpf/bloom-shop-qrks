package com.bloom.notification.consumer;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import com.bloom.common.event.OrderCreatedEvent;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderNotificationConsumer {

        private static final Logger LOG = Logger.getLogger(OrderNotificationConsumer.class);

        @Inject
        ReactiveMailer mailer;

        @Inject
        @Location("order-confirmation.html")
        Template orderConfirmation;

        @Incoming("order-created-in")
        public Uni<Void> handleOrderCreatedEvent(OrderCreatedEvent event) {
                LOG.infof("Received OrderCreatedEvent for order: %s, customer email: %s", event.orderNumber(),
                                event.customerEmail());

                return orderConfirmation.data("event", event)
                                .createUni()
                                .chain((String htmlBody) -> {
                                        Mail mail = Mail.withHtml(
                                                        event.customerEmail(),
                                                        "Bloom Shop - Order Confirmation #" + event.orderNumber(),
                                                        htmlBody);
                                        return mailer.send(mail);
                                })
                                .onItem()
                                .invoke(() -> LOG.infof("Order confirmation email sent successfully for order: %s",
                                                event.orderNumber()))
                                .onFailure()
                                .invoke(error -> LOG.errorf(error, "Failed to send confirmation email for order: %s",
                                                event.orderNumber()))
                                .replaceWithVoid();
        }
}
