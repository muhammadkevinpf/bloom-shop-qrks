package com.bloom.order;

import com.bloom.order.dto.CheckoutRequest;
import com.bloom.order.model.Order;
import com.bloom.order.model.OrderStatus;
import com.bloom.order.service.OrderService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderRoutesTest {

        private static final String ORDERS_PATH = "/api/v1/orders";
        private static final UUID CUSTOMER_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        private static final String TEST_ORDER_NUMBER = "BLM-261001-TEST";
        private static String token;

        @InjectMock
        OrderService orderService;

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

        @Test
        @org.junit.jupiter.api.Order(1)
        public void testCheckoutWithEmptyCartFails() {
                Mockito.when(orderService.checkout(ArgumentMatchers.eq(CUSTOMER_ID),
                                ArgumentMatchers.any(CheckoutRequest.class)))
                                .thenThrow(new BadRequestException("Cannot checkout with an empty cart"));

                var checkoutBody = Map.of(
                                "shippingAddress", Map.of(
                                                "recipientName", "Jane Doe",
                                                "phoneNumber", "+628123456789",
                                                "streetLine1", "Jl. Sudirman No. 45",
                                                "city", "Jakarta Selatan",
                                                "stateProvince", "DKI Jakarta",
                                                "postalCode", "12190",
                                                "countryCode", "ID"));

                given()
                                .auth().oauth2(token)
                                .contentType(ContentType.JSON)
                                .body(checkoutBody)
                                .when()
                                .post(ORDERS_PATH + "/checkout")
                                .then()
                                .statusCode(400);
        }

        @Test
        @org.junit.jupiter.api.Order(2)
        public void testCheckoutSuccessCreatesOrder() {
                Order mockOrder = new Order();
                mockOrder.id = UUID.randomUUID();
                mockOrder.orderNumber = TEST_ORDER_NUMBER;
                mockOrder.status = OrderStatus.PENDING;
                mockOrder.totalAmount = new BigDecimal("90.00");

                Mockito.when(orderService.checkout(ArgumentMatchers.eq(CUSTOMER_ID),
                                ArgumentMatchers.any(CheckoutRequest.class)))
                                .thenReturn(mockOrder);

                var checkoutBody = Map.of(
                                "shippingAddress", Map.of(
                                                "recipientName", "Jane Doe",
                                                "phoneNumber", "+628123456789",
                                                "streetLine1", "Jl. Sudirman No. 45",
                                                "city", "Jakarta Selatan",
                                                "stateProvince", "DKI Jakarta",
                                                "postalCode", "12190",
                                                "countryCode", "ID"));

                given()
                                .auth().oauth2(token)
                                .contentType(ContentType.JSON)
                                .body(checkoutBody)
                                .when()
                                .post(ORDERS_PATH + "/checkout")
                                .then()
                                .statusCode(201)
                                .body("success", is(true))
                                .body("data.orderNumber", equalTo(TEST_ORDER_NUMBER))
                                .body("data.status", equalTo("PENDING"))
                                .body("data.totalAmount", notNullValue());
        }

        @Test
        @org.junit.jupiter.api.Order(3)
        public void testGetOrderByNumber() {
                Order mockOrder = new Order();
                mockOrder.id = UUID.randomUUID();
                mockOrder.orderNumber = TEST_ORDER_NUMBER;
                mockOrder.status = OrderStatus.PENDING;
                mockOrder.totalAmount = new BigDecimal("90.00");

                Mockito.when(orderService.getOrderByNumber(ArgumentMatchers.eq(TEST_ORDER_NUMBER),
                                ArgumentMatchers.eq(CUSTOMER_ID)))
                                .thenReturn(mockOrder);

                given()
                                .auth().oauth2(token)
                                .when()
                                .get(ORDERS_PATH + "/" + TEST_ORDER_NUMBER)
                                .then()
                                .statusCode(200)
                                .body("success", is(true))
                                .body("data.orderNumber", equalTo(TEST_ORDER_NUMBER));
        }

        @Test
        @org.junit.jupiter.api.Order(4)
        public void testGetOrderHistory() {
                given()
                                .auth().oauth2(token)
                                .when()
                                .get(ORDERS_PATH + "?page=0&size=10")
                                .then()
                                .statusCode(200)
                                .body("success", is(true))
                                .body("data.content", notNullValue())
                                .body("data.page", is(0))
                                .body("data.size", is(10));
        }
}
