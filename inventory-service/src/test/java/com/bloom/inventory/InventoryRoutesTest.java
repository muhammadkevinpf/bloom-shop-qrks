package com.bloom.inventory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InventoryRoutesTest {

    private static final String INVENTORY_PATH = "/api/v1/inventory";

    @Test
    public void testGetStockByVariantIdSuccess() {

        given()
                .when()
                .get(INVENTORY_PATH + "/{variantId}", "d8a6e038-cb2e-4391-b9f2-16c3c1debc30")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue());

    }

    @Test
    public void testGetStockByVariantIdNotFound() {

        given()
                .when()
                .get(INVENTORY_PATH + "/{variantId}", "d8a6e038-cb2e-4391-b9f2-16c3c1debc3")
                .then()
                .statusCode(404)
                .body("success", is(false))
                .body("message", notNullValue());

    }

}
