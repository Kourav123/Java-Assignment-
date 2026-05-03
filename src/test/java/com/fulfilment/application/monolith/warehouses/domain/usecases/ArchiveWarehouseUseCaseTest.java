package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseUnitTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldArchiveWarehouseSuccessfully() {
    Warehouse request = new Warehouse();
    request.businessUnitCode = "ARCH-001";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "ARCH-001";
    existing.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("ARCH-001")).thenReturn(existing);

    useCase.archive(request);

    assertNotNull(existing.archivedAt);
    verify(warehouseStore).update(existing);
  }

  @Test
  void shouldFailWhenWarehouseDoesNotExist() {
    Warehouse request = new Warehouse();
    request.businessUnitCode = "MISSING";

    when(warehouseStore.findByBusinessUnitCode("MISSING")).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.archive(request));

    assertTrue(ex.getMessage().contains("does not exist"));
    verify(warehouseStore, never()).update(any());
  }

  @Test
  void shouldFailWhenWarehouseAlreadyArchived() {
    Warehouse request = new Warehouse();
    request.businessUnitCode = "ARCH-002";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "ARCH-002";
    existing.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("ARCH-002")).thenReturn(existing);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.archive(request));

    assertTrue(ex.getMessage().contains("already archived"));
    verify(warehouseStore, never()).update(any());
  }
}