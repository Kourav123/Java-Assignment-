package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {

    private static final String PATH = "/product";

    @Test
    void shouldListProducts() {
        given()
            .when()
            .get(PATH)
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldGetSingleProductById() {
        given()
            .when()
            .get(PATH + "/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("TONSTAD"));
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() {
        given()
            .when()
            .get(PATH + "/99999")
            .then()
            .statusCode(404)
            .body("code", equalTo(404))
            .body("error", containsString("Product with id of 99999 does not exist"));
    }

    @Test
    void shouldCreateProductSuccessfully() {
        String requestBody = """
            {
              "name": "NEW_PRODUCT",
              "description": "New product description",
              "price": 99.99,
              "stock": 20
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post(PATH)
            .then()
            .statusCode(201)
            .body("name", equalTo("NEW_PRODUCT"))
            .body("description", equalTo("New product description"))
            .body("stock", equalTo(20));
    }

    @Test
    void shouldReturn422WhenIdIsSetDuringCreate() {
        String requestBody = """
            {
              "id": 100,
              "name": "INVALID_PRODUCT",
              "description": "Invalid product",
              "price": 50.00,
              "stock": 5
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post(PATH)
            .then()
            .statusCode(422)
            .body("code", equalTo(422))
            .body("error", containsString("Id was invalidly set on request"));
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        String requestBody = """
            {
              "name": "TONSTAD_UPDATED",
              "description": "Updated description",
              "price": 199.99,
              "stock": 30
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .put(PATH + "/1")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", equalTo("TONSTAD_UPDATED"))
            .body("description", equalTo("Updated description"))
            .body("stock", equalTo(30));
    }

    @Test
    void shouldReturn422WhenProductNameMissingDuringUpdate() {
        String requestBody = """
            {
              "description": "No name product",
              "price": 99.99,
              "stock": 10
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .put(PATH + "/1")
            .then()
            .statusCode(422)
            .body("code", equalTo(422))
            .body("error", containsString("Product Name was not set on request"));
    }

    @Test
    void shouldReturn404WhenUpdatingUnknownProduct() {
        String requestBody = """
            {
              "name": "UNKNOWN_PRODUCT",
              "description": "Unknown product",
              "price": 99.99,
              "stock": 10
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .put(PATH + "/99999")
            .then()
            .statusCode(404)
            .body("code", equalTo(404))
            .body("error", containsString("Product with id of 99999 does not exist"));
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        given()
            .when()
            .delete(PATH + "/2")
            .then()
            .statusCode(204);

        given()
            .when()
            .get(PATH)
            .then()
            .statusCode(200)
            .body(not(containsString("KALLAX")));
    }

    @Test
    void shouldReturn404WhenDeletingUnknownProduct() {
        given()
            .when()
            .delete(PATH + "/99999")
            .then()
            .statusCode(404)
            .body("code", equalTo(404))
            .body("error", containsString("Product with id of 99999 does not exist"));
    }
}