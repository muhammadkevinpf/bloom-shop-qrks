package com.bloom.catalog;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProductRoutestTest {

    private static final String PRODUCT_PATH = "/api/v1/products";

    @Test
    public void testGetProductsPaginated() {
        given()
                .when()
                .get(PRODUCT_PATH + "?page=0&size=10")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.content.size()", greaterThan(0))
                .body("data.page", is(0))
                .body("data.size", is(10));
    }

    @Test
    public void testGetFeaturedProducts() {
        given()
                .when()
                .get(PRODUCT_PATH + "/featured")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.content.size()", greaterThan(0));
    }

    @Test
    public void testGetProductBySlugSuccess() {
        String slug = "airflex-runner";

        given()
                .when()
                .get(PRODUCT_PATH + "/" + slug)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .body("data.slug", equalTo(slug));
    }

    @Test
    public void testGetProductBySlugNotFound() {
        given()
                .when()
                .get(PRODUCT_PATH + "/slug12345")
                .then()
                .statusCode(404)
                .body("success", is(false));
    }

    @Test
    public void testGetVariantById() {
        given()
                .when()
                .get(PRODUCT_PATH + "/variants/d8a6e038-cb2e-4391-b9f2-16c3c1debc30")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue());
    }

}
