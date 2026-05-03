package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldCreateWarehouseSuccessfully() {
    Warehouse warehouse = warehouse("WH-001", "AMSTERDAM-001", 50, 20);

    when(warehouseStore.findByBusinessUnitCode("WH-001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));

    useCase.create(warehouse);

    assertNotNull(warehouse.createdAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void shouldFailWhenWarehouseAlreadyExists() {
    Warehouse warehouse = warehouse("WH-001", "AMSTERDAM-001", 50, 20);

    when(warehouseStore.findByBusinessUnitCode("WH-001"))
        .thenReturn(warehouse);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(ex.getMessage().contains("already exists"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldFailWhenLocationIsInvalid() {
    Warehouse warehouse = warehouse("WH-002", "INVALID", 50, 20);

    when(warehouseStore.findByBusinessUnitCode("WH-002")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(ex.getMessage().contains("not valid"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldFailWhenCapacityExceedsLocationMaxCapacity() {
    Warehouse warehouse = warehouse("WH-003", "ZWOLLE-001", 100, 20);

    when(warehouseStore.findByBusinessUnitCode("WH-003")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(ex.getMessage().contains("exceeds location max capacity"));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void shouldFailWhenStockExceedsCapacity() {
    Warehouse warehouse = warehouse("WH-004", "AMSTERDAM-001", 50, 80);

    when(warehouseStore.findByBusinessUnitCode("WH-004")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(ex.getMessage().contains("exceeds warehouse capacity"));
    verify(warehouseStore, never()).create(any());
  }

  private Warehouse warehouse(String code, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}