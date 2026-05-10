package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

    @Inject
    StoreResource resource;

    @BeforeEach
    @Transactional
    void setup() {
        Store.deleteAll();

        Store store = new Store();
        store.name = "TONSTAD";
        store.quantityProductsInStock = 10;
        store.persist();
    }

    private Long getStoreId() {
        Store store = Store.find("name", "TONSTAD").firstResult();
        return store.id;
    }

    @Test
    @Transactional
    void shouldGetAllStores() {
        List<Store> stores = resource.get();

        assertNotNull(stores);
        assertEquals(1, stores.size());
        assertEquals("TONSTAD", stores.get(0).name);
    }

    @Test
    @Transactional
    void shouldGetSingleStore() {
        Long id = getStoreId();

        Store store = resource.getSingle(id);

        assertNotNull(store);
        assertEquals("TONSTAD", store.name);
        assertEquals(10, store.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldThrow404WhenStoreNotFound() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.getSingle(999L));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @Transactional
    void shouldCreateStoreSuccessfully() {
        Store store = new Store();
        store.name = "New Store";
        store.quantityProductsInStock = 100;

        Response response = resource.create(store);

        assertEquals(201, response.getStatus());

        Store savedStore = (Store) response.getEntity();

        assertEquals("New Store", savedStore.name);
        assertEquals(100, savedStore.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldThrow422WhenCreateStoreWithId() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid Store";
        store.quantityProductsInStock = 100;

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.create(store));

        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    @Transactional
    void shouldThrow422WhenCreateStoreNameMissing() {
        Store store = new Store();
        store.quantityProductsInStock = 100;

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.create(store));

        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    @Transactional
    void shouldUpdateStoreSuccessfully() {
        Long id = getStoreId();

        Store updatedStore = new Store();
        updatedStore.name = "Updated Store";
        updatedStore.quantityProductsInStock = 500;

        Store result = resource.update(id, updatedStore);

        assertNotNull(result);
        assertEquals("Updated Store", result.name);
        assertEquals(500, result.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldThrow404WhenUpdateStoreNotFound() {
        Store updatedStore = new Store();
        updatedStore.name = "Updated Store";
        updatedStore.quantityProductsInStock = 500;

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.update(999L, updatedStore));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @Transactional
    void shouldPatchStoreSuccessfully() {
        Long id = getStoreId();

        Store patchRequest = new Store();
        patchRequest.name = "Patched Store";
        patchRequest.quantityProductsInStock = 300;

        Store result = resource.patch(id, patchRequest);

        assertNotNull(result);
        assertEquals("Patched Store", result.name);
        assertEquals(300, result.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldPatchOnlyStoreName() {
        Long id = getStoreId();

        Store patchRequest = new Store();
        patchRequest.name = "Only Name Updated";

        Store result = resource.patch(id, patchRequest);

        assertEquals("Only Name Updated", result.name);
        assertEquals(10, result.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldPatchOnlyStock() {
        Long id = getStoreId();

        Store patchRequest = new Store();
        patchRequest.quantityProductsInStock = 900;

        Store result = resource.patch(id, patchRequest);

        assertEquals("TONSTAD", result.name);
        assertEquals(900, result.quantityProductsInStock);
    }

    @Test
    @Transactional
    void shouldThrow404WhenPatchStoreNotFound() {
        Store patchRequest = new Store();
        patchRequest.name = "Patched Store";
        patchRequest.quantityProductsInStock = 300;

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.patch(999L, patchRequest));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    @Transactional
    void shouldDeleteStoreSuccessfully() {
        Long id = getStoreId();

        Response response = resource.delete(id);

        assertEquals(204, response.getStatus());
    }

    @Test
    @Transactional
    void shouldThrow404WhenDeleteStoreNotFound() {
        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.delete(999L));

        assertEquals(404, ex.getResponse().getStatus());
    }
}