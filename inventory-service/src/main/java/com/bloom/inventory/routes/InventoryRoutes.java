package com.bloom.inventory.routes;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.bloom.common.dto.ApiResponse;
import com.bloom.inventory.dto.UpdateStockRequest;
import com.bloom.inventory.model.Inventory;

import jakarta.transaction.Transactional;
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

@Path("/api/v1/inventory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory", description = "Stock level management and availability check APIs")
public class InventoryRoutes {

    @GET
    @Path("/{variantId}")
    @Operation(summary = "Check available stock", description = "Retrieves current available and reserved stock for a specific product variant")
    @APIResponse(responseCode = "200", description = "Inventory record found")
    @APIResponse(responseCode = "404", description = "Inventory record not found for this variant")
    public Response checkAvailableStockByVariantId(@PathParam("variantId") UUID variantId) {
        Inventory inventory = Inventory.findByVariantId(variantId);
        if (inventory == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Inventory not found for variantId: " + variantId))
                    .build();
        }
        return Response.ok(ApiResponse.ok(inventory)).build();
    }

    @GET
    @Path("/low-stock")
    @Operation(summary = "Get low stock variants", description = "Retrieves all variants whose available stock is less than or equal to the specified threshold")
    @APIResponse(responseCode = "200", description = "List of low stock inventory records")
    public Response checkLowStockProducts(@QueryParam("threshold") @DefaultValue("5") int threshold) {
        List<Inventory> inventory = Inventory.findLowStock(threshold);
        return Response.ok(ApiResponse.ok(inventory)).build();
    }

    @POST
    @Path("/stock")
    @Transactional
    @Operation(summary = "Add or restock inventory", description = "Adds stock to an existing variant or creates a new inventory record if none exists")
    @APIResponse(responseCode = "200", description = "Stock updated successfully")
    @APIResponse(responseCode = "201", description = "Inventory initialized successfully")
    public Response addStock(@RequestBody(required = true) @Valid UpdateStockRequest request) {
        Inventory inventory = Inventory.findByVariantId(request.variantId());

        if (inventory != null) {
            inventory.availableStock += request.quantity();
            return Response.ok(ApiResponse.ok("Stock updated successfully", inventory)).build();
        } else {
            Inventory newInventory = new Inventory();
            newInventory.variantId = request.variantId();
            newInventory.availableStock = request.quantity();
            newInventory.reservedStock = 0;
            newInventory.persist();

            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.created("New inventory stock created successfully", newInventory))
                    .build();
        }
    }
}
