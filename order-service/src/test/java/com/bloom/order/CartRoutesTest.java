package com.bloom.order;

import com.bloom.order.model.CartItem;
import com.bloom.order.model.Customer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartRoutesTest {

    private static final String CART_PATH = "/api/v1/cart";
    private static final UUID CUSTOMER_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID VARIANT_ID = UUID.fromString("d8a6e038-cb2e-4391-b9f2-16c3c1debc30");
    private static String token;
    private static String createdItemId;

    @BeforeAll
    public static void setup() {
        token = given()
                .contentType(ContentType.URLENC)
                .formParam("client_id", "bloom-app")
                .formParam("grant_type", "password")
                .formParam("username", "customer@bloom.com")
                .formParam("password", "password123")
                .when()
                .post("http://localhost:8180/realms/bloom/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }

    @Transactional
    void clearCustomerCart() {
        Customer customer = Customer.findOrCreate(
                CUSTOMER_ID, "customer@bloom.com", "Jane", "Doe", null
        );
        CartItem.deleteByCustomerId(customer.id);
    }

    @Test
    @Order(1)
    public void testAddCartItem() {
        clearCustomerCart();

        var addBody = Map.of(
                "variantId", VARIANT_ID.toString(),
                "quantity", 2
        );

        createdItemId = given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(addBody)
                .when()
                .post(CART_PATH + "/items")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", notNullValue())
                .body("data.quantity", is(2))
                .extract()
                .path("data.id");
    }

    @Test
    @Order(2)
    public void testGetCart() {
        given()
                .auth().oauth2(token)
                .when()
                .get(CART_PATH)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", greaterThan(0))
                .body("data[0].variantId", equalTo(VARIANT_ID.toString()));
    }

    @Test
    @Order(3)
    public void testUpdateCartItemQuantity() {
        var updateBody = Map.of(
                "itemId", createdItemId,
                "newQuantity", 5
        );

        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put(CART_PATH + "/items")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data", is(true));
    }

    @Test
    @Order(4)
    public void testRemoveCartItem() {
        given()
                .auth().oauth2(token)
                .when()
                .delete(CART_PATH + "/items/" + createdItemId)
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data", is(true));
    }
}
