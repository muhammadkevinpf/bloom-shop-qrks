package com.bloom.order.routes;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.bloom.common.dto.ApiResponse;
import com.bloom.common.dto.PageResponse;
import com.bloom.order.dto.CheckoutRequest;
import com.bloom.order.model.Order;
import com.bloom.order.service.OrderService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class OrderRoutes {

    @Inject
    JsonWebToken jwt;

    @Inject
    OrderService orderService;

    @GET
    @Operation(summary = "Get current customer order history")
    @APIResponse(responseCode = "200", description = "Order history retrieved")
    public Response getOrderHistory(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        List<Order> orders = Order.findByCustomerId(customerId, page, size);
        long totalOrder = Order.countByCustomerId(customerId);

        PageResponse<Order> response = PageResponse.of(orders, page, size, totalOrder);
        return Response.ok(ApiResponse.ok(response)).build();
    }

    @POST
    @Path("/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Checkout cart and place an order")
    @APIResponse(responseCode = "201", description = "Order placed successfully")
    public Response checkout(@Valid CheckoutRequest request) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        Order order = orderService.checkout(customerId, request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok(order))
                .build();
    }

    @GET
    @Path("/{orderNumber}")
    @Operation(summary = "Get order detail by order number")
    @APIResponse(responseCode = "200", description = "Order detail retrieved")
    public Response getOrderByNumber(@PathParam("orderNumber") String orderNumber) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        Order order = orderService.getOrderByNumber(orderNumber, customerId);
        return Response.ok(ApiResponse.ok(order)).build();
    }

    @POST
    @Path("/{orderNumber}/pay")
    @Operation(summary = "Simulate mock payment for an order", description = "Marks an order as PAID and transitions status to be SHIPPED")
    @APIResponse(responseCode = "200", description = "Payment simulated successfully")
    @APIResponse(responseCode = "400", description = "Order is already paid or cancelled")
    @APIResponse(responseCode = "404", description = "Order not found")
    public Response payOrder(@PathParam("orderNumber") String orderNumber) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        Order paidOrder = orderService.payOrder(orderNumber, customerId);
        return Response.ok(ApiResponse.ok("Payment successful", paidOrder)).build();
    }

    @POST
    @Path("/{orderNumber}/complete")
    @Operation(summary = "Simulate mock complete for an order", description = "Marks an order as DELIVERED")
    @APIResponse(responseCode = "200", description = "Order completed successfully")
    @APIResponse(responseCode = "400", description = "Order is not paid, or already cancelled or delivered")
    @APIResponse(responseCode = "404", description = "Order not found")
    public Response completeOrder(@PathParam("orderNumber") String orderNumber) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        Order deliveredOrder = orderService.completeOrder(orderNumber, customerId);
        return Response.ok(ApiResponse.ok("Order completed successfully", deliveredOrder)).build();
    }
}
