package com.bloom.catalog.routes;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.bloom.catalog.model.Category;
import com.bloom.common.dto.ApiResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Category", description = "Product category browsing and navigation APIs")
public class CategoryRoutes {

    @GET
    @Operation(summary = "Get all active categories", description = "Retrieves a flat list of all active categories")
    public Response getAllActiveCategories() {
        List<Category> categories = Category.findActiveCategories();
        return Response.ok(ApiResponse.ok(categories)).build();
    }

    @GET
    @Path("/tree")
    @Operation(summary = "Get category tree", description = "Retrieves top-level categories with their nested children")
    public Response getRootCategories() {
        List<Category> categories = Category.findRootCategories();
        return Response.ok(ApiResponse.ok(categories)).build();
    }

    @GET
    @Path("/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a single category by its URL slug")
    @APIResponse(responseCode = "200", description = "Category found")
    @APIResponse(responseCode = "404", description = "Category not found")
    public Response getCategoryBySlug(@PathParam("slug") String slug) {
        Category category = Category.findBySlug(slug);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Category not found with slug " + slug))
                    .build();
        }
        return Response.ok(ApiResponse.ok(category)).build();
    }
}
