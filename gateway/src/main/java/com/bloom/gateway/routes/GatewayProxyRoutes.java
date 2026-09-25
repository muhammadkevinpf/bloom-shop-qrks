package com.bloom.gateway.routes;

import io.quarkus.vertx.web.Route;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class GatewayProxyRoutes {

    private static final Logger LOG = Logger.getLogger(GatewayProxyRoutes.class);

    private static final String API_PREFIX = "/api/v1/";

    @Inject
    Vertx vertx;

    private WebClient webClient;

    @ConfigProperty(name = "bloom.services.catalog.url")
    String catalogUrl;

    @ConfigProperty(name = "bloom.services.order.url")
    String orderUrl;

    @ConfigProperty(name = "bloom.services.inventory.url")
    String inventoryUrl;

    @PostConstruct
    void init() {
        this.webClient = WebClient.create(vertx);
    }

    @Route(path = "/api/v1/*", order = 1)
    public void proxyApiRequests(RoutingContext rc) {
        String uri = rc.request().uri();
        String targetBaseUrl = resolveTargetUrl(uri);

        if (targetBaseUrl == null) {
            rc.response().setStatusCode(404).end("\"error\": \"Service not found for URI: " + uri + "\"");
            return;
        }

        String fullTargetUrl = targetBaseUrl + uri;
        forwardRequest(rc, fullTargetUrl);
    }

    private String resolveTargetUrl(String uri) {
        if (!uri.startsWith(API_PREFIX)) {
            return null;
        }

        String segment = uri.substring(API_PREFIX.length()).split("[/?]")[0];

        return switch (segment) {
            case "products", "categories" -> catalogUrl;
            case "orders", "cart" -> orderUrl;
            case "inventory" -> inventoryUrl;
            default -> null;
        };
    }

    private void forwardRequest(RoutingContext rc, String fullTargetUrl) {
        // 1. Create client request with the same HTTP method (GET, POST, PUT, DELETE,
        // etc.)
        var clientRequest = webClient.requestAbs(rc.request().method(), fullTargetUrl);

        // 2. Forward essential headers from client to downstream service
        rc.request().headers().forEach(entry -> {
            // Skip host header so downstream sees its own host
            if (!entry.getKey().equalsIgnoreCase("host")) {
                clientRequest.putHeader(entry.getKey(), entry.getValue());
            }
        });

        // 3. Send body (if POST/PUT) or send empty (GET/DELETE)
        var clientResponseFuture = (rc.body() != null && rc.body().buffer() != null)
                ? clientRequest.sendBuffer(rc.body().buffer())
                : clientRequest.send();

        // 4. Pipe downstream response back to caller
        clientResponseFuture.onSuccess(response -> {
            rc.response().setStatusCode(response.statusCode());

            // Copy downstream response headers (Content-Type, etc.)
            response.headers().forEach(entry -> {
                rc.response().putHeader(entry.getKey(), entry.getValue());
            });

            if (response.body() != null) {
                rc.response().end(response.body());
            } else {
                rc.response().end();
            }
        }).onFailure(err -> {
            LOG.errorf(err, "Failed to proxy request to: %s", fullTargetUrl);
            rc.response().setStatusCode(502).end("{\"error\": \"Bad Gateway: " + err.getMessage() + "\"}");
        });
    }
}
