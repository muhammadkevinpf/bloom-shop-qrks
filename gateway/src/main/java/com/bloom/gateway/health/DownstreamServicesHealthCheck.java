package com.bloom.gateway.health;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

@Readiness
@ApplicationScoped
public class DownstreamServicesHealthCheck implements HealthCheck {

    @ConfigProperty(name = "bloom.services.catalog.url")
    public String catalogUrl;

    @ConfigProperty(name = "bloom.services.order.url")
    public String orderUrl;

    @ConfigProperty(name = "bloom.services.inventory.url")
    public String inventoryUrl;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static final String LIVENESS_PATH = "/q/health/live";

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Downstream Microservices");

        boolean catalogUp = checkServicesHealth(catalogUrl + LIVENESS_PATH);
        boolean orderUp = checkServicesHealth(orderUrl + LIVENESS_PATH);
        boolean inventoryUp = checkServicesHealth(inventoryUrl + LIVENESS_PATH);

        responseBuilder
                .withData("catalog-service", catalogUp ? "UP" : "DOWN")
                .withData("order-service", orderUp ? "UP" : "DOWN")
                .withData("inventory-service", inventoryUp ? "UP" : "DOWN");

        if (catalogUp && orderUp && inventoryUp) {
            return responseBuilder.up().build();
        } else {
            return responseBuilder.down().build();
        }
    }

    private boolean checkServicesHealth(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
