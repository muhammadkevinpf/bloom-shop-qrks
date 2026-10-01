package com.bloom.gateway;

import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthRoutesTest {

    @ConfigProperty(name = "bloom.keycloak.admin-username")
    private static String adminUsername;
    @ConfigProperty(name = "bloom.keycloak.admin-password")
    private static String adminPassword;

    private static final String authPath = "/api/v1/auth";
    private static String refreshToken;

    private static final String TEST_USER_EMAIL = "temp.testuser@bloom.com";
    private static final String TEST_USER_PASSWORD = "SecurePass123!";

    @Test
    @Order(1)
    public void testRegisterNewCustomer() {

        var registerBody = Map.of(
                "email", TEST_USER_EMAIL,
                "password", TEST_USER_PASSWORD,
                "firstName", "Test",
                "lastName", "User");

        given()
                .contentType(ContentType.JSON)
                .body(registerBody)
                .when()
                .post(authPath + "/register")
                .then()
                .statusCode(201)
                .body("success", is(true))
                .body("data.access_token", notNullValue());
    }

    @Test
    @Order(2)
    public void testLoginSuccess() {
        var loginBody = Map.of(
                "email", TEST_USER_EMAIL,
                "password", TEST_USER_PASSWORD);

        refreshToken = given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post(authPath + "/login")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("message", notNullValue())
                .body("data.access_token", notNullValue())
                .body("data.refresh_token", notNullValue())
                .extract()
                .path("data.refresh_token");
    }

    @Test
    @Order(3)
    public void testLoginInvalidPassword() {
        var loginBody = Map.of(
                "email", TEST_USER_EMAIL,
                "password", "dummy");

        given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post(authPath + "/login")
                .then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", notNullValue());
    }

    @Test
    @Order(4)
    public void testRefreshTokenSuccess() {
        var refreshBody = Map.of("refreshToken", refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(refreshBody)
                .when()
                .post(authPath + "/refresh")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data.access_token", notNullValue());
    }

    @AfterAll
    public static void cleanupTestUser() {
        Config config = ConfigProvider.getConfig();
        String keycloakUrl = config.getValue("bloom.keycloak.base-url", String.class);
        String adminUsername = config.getValue("bloom.keycloak.admin-username", String.class);
        String adminPassword = config.getValue("bloom.keycloak.admin-password", String.class);
        try {
            String adminToken = given()
                    .formParam("client_id", "admin-cli")
                    .formParam("grant_type", "password")
                    .formParam("username", adminUsername)
                    .formParam("password", adminPassword)
                    .when()
                    .post(keycloakUrl + "/realms/master/protocol/openid-connect/token")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("access_token");

            String userId = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .queryParam("email", TEST_USER_EMAIL)
                    .when()
                    .get(keycloakUrl + "/admin/realms/bloom/users")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("[0].id");

            if (userId != null) {
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .when()
                        .delete(keycloakUrl + "/admin/realms/bloom/users/" + userId)
                        .then()
                        .statusCode(204);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not clean up test user: " + e.getMessage());
        }
    }
}
