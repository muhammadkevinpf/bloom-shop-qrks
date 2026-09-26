package com.bloom.gateway.service;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.bloom.gateway.dto.LoginRequest;
import com.bloom.gateway.dto.RegisterRequest;
import com.bloom.gateway.dto.TokenResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthService {

    @Inject
    Vertx vertx;

    private WebClient webClient;

    @ConfigProperty(name = "bloom.keycloak.base-url")
    String keycloakBaseUrl;

    @ConfigProperty(name = "bloom.keycloak.realm")
    String keycloakRealm;

    @ConfigProperty(name = "bloom.keycloak.client-id")
    String keycloakClientId;

    @ConfigProperty(name = "bloom.keycloak.admin-username")
    String keycloakAdminUsername;

    @ConfigProperty(name = "bloom.keycloak.admin-password")
    String keycloakAdminPassword;

    @PostConstruct
    void init() {
        this.webClient = WebClient.create(vertx);
    }

    public Uni<TokenResponse> login(LoginRequest request) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakBaseUrl, keycloakRealm);

        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.set("client_id", keycloakClientId);
        form.set("grant_type", "password");
        form.set("username", request.email());
        form.set("password", request.password());

        return Uni.createFrom().emitter(emitter -> {
            webClient.postAbs(tokenUrl)
                    .sendForm(form)
                    .onSuccess(response -> {
                        if (response.statusCode() == 200) {
                            JsonObject json = response.bodyAsJsonObject();
                            TokenResponse tokenResponse = new TokenResponse(
                                    json.getString("access_token"),
                                    json.getString("refresh_token"),
                                    json.getLong("expires_in"),
                                    json.getString("token_type"));
                            emitter.complete(tokenResponse);
                        } else {
                            emitter.fail(new WebApplicationException(
                                    "Invalid credentials or login failed", Response.Status.UNAUTHORIZED));
                        }
                    })
                    .onFailure(err -> emitter
                            .fail(new WebApplicationException("Keycloak connection error: " + err.getMessage(), 502)));
        });
    }

    public Uni<TokenResponse> register(RegisterRequest request) {
        return getAdminToken().chain(adminToken -> createUser(adminToken, request))
                .chain(() -> login(new LoginRequest(request.email(), request.password())));
    }

    private Uni<String> getAdminToken() {
        String adminTokenUrl = String.format("%s/realms/master/protocol/openid-connect/token", keycloakBaseUrl);
        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.set("client_id", "admin-cli");
        form.set("grant_type", "password");
        form.set("username", keycloakAdminUsername);
        form.set("password", keycloakAdminPassword);

        return Uni.createFrom().emitter(emitter -> {
            webClient.postAbs(adminTokenUrl)
                    .sendForm(form)
                    .onSuccess(response -> {
                        if (response.statusCode() == 200) {
                            JsonObject json = response.bodyAsJsonObject();
                            emitter.complete(json.getString("access_token"));
                        } else {
                            emitter.fail(new WebApplicationException(
                                    "Invalid credentials or login failed", Response.Status.UNAUTHORIZED));
                        }
                    })
                    .onFailure(err -> emitter
                            .fail(new WebApplicationException("Keycloak connection error: " + err.getMessage(), 502)));
        });
    }

    private Uni<Void> createUser(String adminToken, RegisterRequest request) {
        String userUrl = String.format("%s/admin/realms/%s/users", keycloakBaseUrl, keycloakRealm);

        JsonObject userJson = new JsonObject()
                .put("username", request.email())
                .put("email", request.email())
                .put("firstName", request.firstName())
                .put("lastName", request.lastName())
                .put("enabled", true)
                .put("emailVerified", true)
                .put("requiredActions", List.of())
                .put("credentials", List.of(
                        Map.of("type", "password", "value", request.password(), "temporary", false)));

        return Uni.createFrom().emitter(emitter -> {
            webClient.postAbs(userUrl)
                    .bearerTokenAuthentication(adminToken)
                    .sendJsonObject(userJson)
                    .onSuccess(response -> {
                        if (response.statusCode() == 201) {
                            emitter.complete(null);
                        } else if (response.statusCode() == 409) {
                            emitter.fail(new WebApplicationException("User already exists", Response.Status.CONFLICT));
                        } else {
                            emitter.fail(new WebApplicationException("Failed to create user", response.statusCode()));
                        }
                    })
                    .onFailure(err -> emitter.fail(err));
        });

    }

}
