/*
 * package com.fulfilment.application.monolith.stores; import static
 * io.restassured.RestAssured.given; import static
 * org.hamcrest.Matchers.equalTo; import static
 * org.junit.jupiter.api.Assertions.assertEquals; import static
 * org.junit.jupiter.api.Assertions.assertNotNull;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * io.quarkus.test.junit.QuarkusTest; import io.restassured.http.ContentType;
 * import jakarta.transaction.Transactional; import
 * jakarta.ws.rs.WebApplicationException; import jakarta.ws.rs.core.Response;
 * import java.lang.reflect.Field; import org.junit.jupiter.api.BeforeEach;
 * import org.junit.jupiter.api.Test;
 * 
 * @QuarkusTest class StoreResourceTest {
 * 
 * @BeforeEach
 * 
 * @Transactional void setup() { Store.deleteAll();
 * 
 * Store store1 = new Store(); store1.name = "TONSTAD";
 * store1.quantityProductsInStock = 10; store1.persist();
 * 
 * Store store2 = new Store(); store2.name = "KALLAX";
 * store2.quantityProductsInStock = 5; store2.persist(); }
 * 
 * private Long getStoreIdByName(String name) { Store store = (Store)
 * Store.find("name", name).firstResult(); return store.id; }
 * 
 * @Test void shouldGetAllStores() { given() .when() .get("/store") .then()
 * .statusCode(200); }
 * 
 * @Test void shouldGetSingleStore() { Long id = getStoreIdByName("TONSTAD");
 * 
 * given() .when() .get("/store/" + id) .then() .statusCode(200) .body("name",
 * equalTo("TONSTAD")) .body("quantityProductsInStock", equalTo(10)); }
 * 
 * @Test void shouldReturn404WhenStoreNotFound() { given() .when()
 * .get("/store/99999") .then() .statusCode(404); }
 * 
 * @Test void shouldCreateStore() { String requestBody = """ { "name":
 * "New Store", "quantityProductsInStock": 200 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .post("/store") .then() .statusCode(201) .body("name", equalTo("New Store"))
 * .body("quantityProductsInStock", equalTo(200)); }
 * 
 * @Test void shouldReturn422WhenCreateStoreWithId() { String requestBody = """
 * { "id": 1, "name": "Invalid Store", "quantityProductsInStock": 10 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .post("/store") .then() .statusCode(422); }
 * 
 * @Test void shouldReturn422WhenCreateStoreWithoutName() { String requestBody =
 * """ { "quantityProductsInStock": 10 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .post("/store") .then() .statusCode(422); }
 * 
 * @Test void shouldUpdateStore() { Long id = getStoreIdByName("TONSTAD");
 * 
 * String requestBody = """ { "name": "Updated Store",
 * "quantityProductsInStock": 500 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .put("/store/" + id) .then() .statusCode(200) .body("name",
 * equalTo("Updated Store")) .body("quantityProductsInStock", equalTo(500)); }
 * 
 * @Test void shouldReturn404WhenUpdateStoreNotFound() { String requestBody =
 * """ { "name": "Updated Store", "quantityProductsInStock": 500 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .put("/store/99999") .then() .statusCode(404); }
 * 
 * @Test void shouldReturn422WhenUpdateStoreNameMissing() { Long id =
 * getStoreIdByName("TONSTAD");
 * 
 * String requestBody = """ { "quantityProductsInStock": 500 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .put("/store/" + id) .then() .statusCode(422); }
 * 
 * @Test void shouldPatchStore() { Long id = getStoreIdByName("TONSTAD");
 * 
 * String requestBody = """ { "name": "Patched Store",
 * "quantityProductsInStock": 700 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .patch("/store/" + id) .then() .statusCode(200) .body("name",
 * equalTo("Patched Store")) .body("quantityProductsInStock", equalTo(700)); }
 * 
 * @Test void shouldPatchOnlyStoreName() { Long id =
 * getStoreIdByName("TONSTAD");
 * 
 * String requestBody = """ { "name": "Only Name Updated" } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .patch("/store/" + id) .then() .statusCode(200) .body("name",
 * equalTo("Only Name Updated")) .body("quantityProductsInStock", equalTo(10));
 * }
 * 
 * @Test void shouldPatchOnlyStock() { Long id = getStoreIdByName("TONSTAD");
 * 
 * String requestBody = """ { "quantityProductsInStock": 900 } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .patch("/store/" + id) .then() .statusCode(200) .body("name",
 * equalTo("TONSTAD")) .body("quantityProductsInStock", equalTo(900)); }
 * 
 * @Test void shouldReturn404WhenPatchStoreNotFound() { String requestBody = """
 * { "name": "Patched Store" } """;
 * 
 * given() .contentType(ContentType.JSON) .body(requestBody) .when()
 * .patch("/store/99999") .then() .statusCode(404); }
 * 
 * @Test void shouldDeleteStore() { Long id = getStoreIdByName("TONSTAD");
 * 
 * given() .when() .delete("/store/" + id) .then() .statusCode(204); }
 * 
 * @Test void shouldReturn404WhenDeleteStoreNotFound() { given() .when()
 * .delete("/store/99999") .then() .statusCode(404); }
 * 
 * @Test void shouldCoverErrorMapperForWebApplicationException() throws
 * Exception { StoreResource.ErrorMapper mapper = new
 * StoreResource.ErrorMapper();
 * 
 * Field field =
 * StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
 * field.setAccessible(true); field.set(mapper, new ObjectMapper());
 * 
 * Response response = mapper.toResponse( new
 * WebApplicationException("Store not found", 404));
 * 
 * assertEquals(404, response.getStatus()); assertNotNull(response.getEntity());
 * }
 * 
 * @Test void shouldCoverErrorMapperForGenericException() throws Exception {
 * StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
 * 
 * Field field =
 * StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
 * field.setAccessible(true); field.set(mapper, new ObjectMapper());
 * 
 * Response response = mapper.toResponse( new
 * RuntimeException("Something went wrong"));
 * 
 * assertEquals(500, response.getStatus()); assertNotNull(response.getEntity());
 * } }
 */