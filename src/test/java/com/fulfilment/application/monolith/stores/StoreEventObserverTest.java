package com.fulfilment.application.monolith.stores;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreEventObserverTest {

    private StoreEventObserver storeEventObserver;
    private LegacyStoreManagerGateway legacyStoreManagerGateway;

    @BeforeEach
    void setUp() throws Exception {
        storeEventObserver = new StoreEventObserver();
        legacyStoreManagerGateway = mock(LegacyStoreManagerGateway.class);

        Field field = StoreEventObserver.class.getDeclaredField("legacyStoreManagerGateway");
        field.setAccessible(true);
        field.set(storeEventObserver, legacyStoreManagerGateway);
    }

    @Test
    void shouldCallLegacyGatewayWhenStoreCreated() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Test Store";
        store.quantityProductsInStock = 100;

        StoreCreatedEvent event = new StoreCreatedEvent(store);

        storeEventObserver.onStoreCreated(event);

        verify(legacyStoreManagerGateway, times(1))
                .createStoreOnLegacySystem(store);
    }

    @Test
    void shouldCallLegacyGatewayWhenStoreUpdated() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Test Store";
        store.quantityProductsInStock = 100;

        StoreUpdatedEvent event = new StoreUpdatedEvent(store);

        storeEventObserver.onStoreUpdated(event);

        verify(legacyStoreManagerGateway, times(1))
                .updateStoreOnLegacySystem(store);
    }
}