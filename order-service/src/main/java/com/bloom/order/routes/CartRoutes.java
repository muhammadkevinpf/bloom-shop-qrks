package com.bloom.order.routes;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.bloom.common.dto.ApiResponse;
import com.bloom.order.dto.AddCartItemRequest;
import com.bloom.order.dto.UpdateItemQuantityRequest;
import com.bloom.order.model.CartItem;
import com.bloom.order.model.Customer;

import io.quarkus.security.Authenticated;

@Path("/api/v1/cart")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Cart", description = "Shopping cart operations for authenticated customers")
public class CartRoutes {

    @Inject
    JsonWebToken jwt;

    @GET
    @Operation(summary = "Get current cart", description = "Returns all items in the authenticated customer's shopping cart")
    @APIResponse(responseCode = "200", description = "Cart items retrieved")
    public Response getCart() {
        UUID customerId = UUID.fromString(jwt.getSubject());

        List<CartItem> items = CartItem.findByCustomerId(customerId);
        return Response.ok(ApiResponse.ok(items)).build();
    }

    @POST
    @Path("/items")
    @Transactional
    @Operation(summary = "Add item to cart", description = "Adds a new item to the customer's cart or increments the quantity if the item already exists")
    @APIResponse(responseCode = "200", description = "Item added successfully")
    public Response addCartItem(@RequestBody @Valid AddCartItemRequest request) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        Customer customer = Customer.findOrCreate(customerId, email, firstName, lastName, null);

        CartItem item = CartItem.addOrIncrement(customer, request.variantId(), request.quantity());
        return Response.ok(ApiResponse.ok("Item added to cart", item)).build();
    }

    @PUT
    @Path("/items")
    @Transactional
    @Operation(summary = "Update item quantity", description = "Updates quantity of a specific cart item (deletes if quantity <= 0)")
    @APIResponse(responseCode = "200", description = "Quantity updated")
    @APIResponse(responseCode = "404", description = "Cart item not found")
    public Response updateCartItemQuantity(@RequestBody @Valid UpdateItemQuantityRequest request) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        boolean updated = CartItem.updateQuantity(request.itemId(), customerId, request.newQuantity());

        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Cart item not found or does not belong to user"))
                    .build();
        }

        return Response.ok(ApiResponse.ok("Cart quantity updated", true)).build();
    }

    @DELETE
    @Path("/items/{id}")
    @Transactional
    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the cart")
    @APIResponse(responseCode = "200", description = "Item removed successfully")
    @APIResponse(responseCode = "404", description = "Cart item not found")
    public Response removeCartItem(@PathParam("id") UUID itemId) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        boolean deleted = CartItem.deleteByIdAndCustomerId(itemId, customerId);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Cart item not found or does not belong to user"))
                    .build();
        }
        return Response.ok(ApiResponse.ok("Item removed from cart", true)).build();
    }
}
