package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SearchWarehousesUseCaseTest {

    private SearchWarehousesUseCase searchWarehousesUseCase;
    private WarehouseStore warehouseStore;

    @BeforeEach
    void setUp() {
        warehouseStore = Mockito.mock(WarehouseStore.class);
        searchWarehousesUseCase = new SearchWarehousesUseCase(warehouseStore);
    }

    private Warehouse createWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 100;
        warehouse.stock = 20;
        return warehouse;
    }

    @Test
    void testSearchWarehousesUseCase() {
        assertNotNull(searchWarehousesUseCase);
    }

    @Test
    void testSearchByLocation() {
        Warehouse warehouse = createWarehouse();

        when(warehouseStore.findByLocation("AMSTERDAM-001"))
                .thenReturn(List.of(warehouse));

        List<Warehouse> result =
                searchWarehousesUseCase.searchByLocation("AMSTERDAM-001");

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).businessUnitCode);
        assertEquals("AMSTERDAM-001", result.get(0).location);

        verify(warehouseStore).findByLocation("AMSTERDAM-001");
    }

    @Test
    void testSearchByLocationThrowsExceptionWhenLocationIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> searchWarehousesUseCase.searchByLocation("")
        );

        assertEquals("Location cannot be null or blank", ex.getMessage());
        verify(warehouseStore, never()).findByLocation(anyString());
    }

    @Test
    void testSearchByCapacityRange() {
        Warehouse warehouse = createWarehouse();

        when(warehouseStore.findByCapacityBetween(50, 150))
                .thenReturn(List.of(warehouse));

        List<Warehouse> result =
                searchWarehousesUseCase.searchByCapacityRange(50, 150);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).capacity);

        verify(warehouseStore).findByCapacityBetween(50, 150);
    }

    @Test
    void testSearchByCapacityRangeThrowsExceptionWhenMinGreaterThanMax() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> searchWarehousesUseCase.searchByCapacityRange(200, 100)
        );

        assertEquals("minCapacity cannot be greater than maxCapacity", ex.getMessage());
        verify(warehouseStore, never()).findByCapacityBetween(any(), any());
    }

    @Test
    void testSearchByStockRange() {
        Warehouse warehouse = createWarehouse();

        when(warehouseStore.findByStockBetween(10, 50))
                .thenReturn(List.of(warehouse));

        List<Warehouse> result =
                searchWarehousesUseCase.searchByStockRange(10, 50);

        assertEquals(1, result.size());
        assertEquals(20, result.get(0).stock);

        verify(warehouseStore).findByStockBetween(10, 50);
    }

    @Test
    void testSearchByStockRangeThrowsExceptionWhenMinGreaterThanMax() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> searchWarehousesUseCase.searchByStockRange(100, 10)
        );

        assertEquals("minStock cannot be greater than maxStock", ex.getMessage());
        verify(warehouseStore, never()).findByStockBetween(any(), any());
    }

    @Test
    void testSearchByArchivedStatus() {
        Warehouse warehouse = createWarehouse();

        when(warehouseStore.findByArchived(false))
                .thenReturn(List.of(warehouse));

        List<Warehouse> result =
                searchWarehousesUseCase.searchByArchivedStatus(false);

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).businessUnitCode);

        verify(warehouseStore).findByArchived(false);
    }
}