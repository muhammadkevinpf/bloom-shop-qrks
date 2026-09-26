package com.bloom.gateway.routes;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.bloom.common.dto.ApiResponse;
import com.bloom.gateway.dto.LoginRequest;
import com.bloom.gateway.dto.RegisterRequest;
import com.bloom.gateway.service.AuthService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Customer login and registration APIs")
public class AuthRoutes {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Operation(summary = "Login customer", description = "Authenticates credentials with Keycloak and returns JWT tokens")
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    public Uni<Response> login(@Valid LoginRequest req) {
        return authService.login(req).map(token -> Response.ok(ApiResponse.ok("Login successful", token)).build());
    }

    @POST
    @Path("/register")
    @Operation(summary = "Register customer", description = "Creates a new customer account with Keycloak")
    @APIResponse(responseCode = "201", description = "Registration successful")
    @APIResponse(responseCode = "409", description = "User already exists")
    public Uni<Response> register(@Valid RegisterRequest req) {
        return authService.register(req)
                .map(token -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok("Registration successful", token)).build());
    }
}
