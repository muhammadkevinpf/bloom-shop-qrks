package com.bloom.order.client;

import java.util.UUID;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.bloom.common.dto.ApiResponse;
import com.bloom.order.dto.CatalogVariantDto;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "catalog-client")
@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
public interface CatalogServiceClient {

    @GET
    @Path("/variants/{variantId}")
    ApiResponse<CatalogVariantDto> getVariantById(@PathParam("variantId") UUID variantId);
}
