package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseUnitTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    Warehouse existing = warehouse("REP-001", "AMSTERDAM-001", 80, 40);
    Warehouse replacement = warehouse("REP-001", "ZWOLLE-001", 30, 15);

    when(warehouseStore.findByBusinessUnitCode("REP-001")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));

    useCase.replace(replacement);

    assertEquals("ZWOLLE-001", existing.location);
    assertEquals(30, existing.capacity);
    assertEquals(15, existing.stock);
    verify(warehouseStore).update(existing);
  }

  @Test
  void shouldFailWhenWarehouseDoesNotExist() {
    Warehouse replacement = warehouse("MISSING", "ZWOLLE-001", 30, 15);

    when(warehouseStore.findByBusinessUnitCode("MISSING")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));

    assertTrue(ex.getMessage().contains("does not exist"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldFailWhenWarehouseIsArchived() {
    Warehouse existing = warehouse("REP-002", "AMSTERDAM-001", 80, 40);
    existing.archivedAt = LocalDateTime.now();

    Warehouse replacement = warehouse("REP-002", "ZWOLLE-001", 30, 15);

    when(warehouseStore.findByBusinessUnitCode("REP-002")).thenReturn(existing);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));

    assertTrue(ex.getMessage().contains("archived"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldFailWhenNewLocationIsInvalid() {
    Warehouse existing = warehouse("REP-003", "AMSTERDAM-001", 80, 40);
    Warehouse replacement = warehouse("REP-003", "INVALID", 30, 15);

    when(warehouseStore.findByBusinessUnitCode("REP-003")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("INVALID")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));

    assertTrue(ex.getMessage().contains("not valid"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldFailWhenCapacityExceedsLocationCapacity() {
    Warehouse existing = warehouse("REP-004", "AMSTERDAM-001", 80, 40);
    Warehouse replacement = warehouse("REP-004", "ZWOLLE-001", 100, 15);

    when(warehouseStore.findByBusinessUnitCode("REP-004")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));

    assertTrue(ex.getMessage().contains("exceeds location max capacity"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldFailWhenStockExceedsCapacity() {
    Warehouse existing = warehouse("REP-005", "AMSTERDAM-001", 80, 40);
    Warehouse replacement = warehouse("REP-005", "ZWOLLE-001", 30, 50);

    when(warehouseStore.findByBusinessUnitCode("REP-005")).thenReturn(existing);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));

    assertTrue(ex.getMessage().contains("exceeds warehouse capacity"));
    verify(warehouseStore, never()).update(any());
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