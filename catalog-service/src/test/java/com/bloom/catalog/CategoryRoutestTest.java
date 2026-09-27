package com.bloom.catalog;

import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class CategoryRoutestTest {

    private static final String CATEGORY_PATH = "/api/v1/categories";

    @Test
    public void testGetAllActiveCategories() {
        given()
                .when()
                .get(CATEGORY_PATH)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", greaterThan(0));
    }
}
