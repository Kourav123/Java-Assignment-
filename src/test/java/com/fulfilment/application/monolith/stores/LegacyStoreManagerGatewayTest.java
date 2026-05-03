package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

    private LegacyStoreManagerGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new LegacyStoreManagerGateway();
    }

    @Test
    void shouldCreateStoreOnLegacySystem() {
        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 100;

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    void shouldUpdateStoreOnLegacySystem() {
        Store store = new Store();
        store.name = "Updated Store";
        store.quantityProductsInStock = 200;

        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }

    @Test
    void shouldHandleExceptionWhenStoreNameIsNull() {
        Store store = new Store();
        store.name = null;
        store.quantityProductsInStock = 50;

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }
}