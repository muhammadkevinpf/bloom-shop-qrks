package com.bloom.catalog.routes;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.bloom.catalog.model.Product;
import com.bloom.common.dto.ApiResponse;
import com.bloom.common.dto.PageResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductRoutes {

    @GET
    @Operation(summary = "Get paginated products", description = "Retrieves a paginated list of active products")
    @APIResponse(responseCode = "200", description = "Products page retrieved successfully")
    public Response getProductsByPage(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        List<Product> products = Product.findByPage(page, size);
        long totalCount = Product.count("status", "ACTIVE");
        PageResponse<Product> pageResponse = PageResponse.of(products, page, size, totalCount);
        return Response.ok(ApiResponse.ok(pageResponse)).build();
    }

    @GET
    @Path("/featured")
    @Operation(summary = "Get featured products", description = "Retrieves products flagged for homepage featured showcase")
    @APIResponse(responseCode = "200", description = "List of featured products")
    public Response getFeaturedProducts() {
        List<Product> products = Product.findFeatured();
        return Response.ok(ApiResponse.ok(products)).build();
    }

    @GET
    @Path("/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieves full product details using its SEO-friendly URL slug")
    @APIResponse(responseCode = "200", description = "Product found")
    @APIResponse(responseCode = "404", description = "Product not found")
    public Response getProductsDetail(@PathParam("slug") String slug) {
        Product product = Product.findBySlug(slug);
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Product not found with slug: " + slug))
                    .build();
        }

        return Response.ok(ApiResponse.ok(product)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search products", description = "Searches active products by name or description keyword")
    @APIResponse(responseCode = "200", description = "Search results returned")
    public Response searchProducts(@QueryParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Response.ok(ApiResponse.ok(List.of())).build();
        }
        List<Product> products = Product.search(keyword.trim());
        return Response.ok(ApiResponse.ok(products)).build();
    }
}
