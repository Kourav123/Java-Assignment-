package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehousesOperation;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WarehouseResourceImplTest {

    private WarehouseResourceImpl resource;

    private WarehouseRepository warehouseRepository;
    private CreateWarehouseOperation createWarehouseOperation;
    private ArchiveWarehouseOperation archiveWarehouseOperation;
    private ReplaceWarehouseOperation replaceWarehouseOperation;
    private SearchWarehousesOperation searchWarehousesOperation;

    @BeforeEach
    void setUp() throws Exception {
        resource = new WarehouseResourceImpl();

        warehouseRepository = Mockito.mock(WarehouseRepository.class);
        createWarehouseOperation = Mockito.mock(CreateWarehouseOperation.class);
        archiveWarehouseOperation = Mockito.mock(ArchiveWarehouseOperation.class);
        replaceWarehouseOperation = Mockito.mock(ReplaceWarehouseOperation.class);
        searchWarehousesOperation = Mockito.mock(SearchWarehousesOperation.class);

        setField(resource, "warehouseRepository", warehouseRepository);
        setField(resource, "createWarehouseOperation", createWarehouseOperation);
        setField(resource, "archiveWarehouseOperation", archiveWarehouseOperation);
        setField(resource, "replaceWarehouseOperation", replaceWarehouseOperation);
        setField(resource, "searchWarehousesOperation", searchWarehousesOperation);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse createDomainWarehouse(
            String businessUnitCode,
            String location,
            int capacity,
            int stock) {

        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = businessUnitCode;
        domain.location = location;
        domain.capacity = capacity;
        domain.stock = stock;
        return domain;
    }

    @Test
    void shouldListAllWarehousesUnits() {
        var domain = createDomainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);

        when(warehouseRepository.getAll()).thenReturn(List.of(domain));

        List<Warehouse> result = resource.listAllWarehousesUnits();

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.get(0).getLocation());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(10, result.get(0).getStock());
    }

    @Test
    void shouldCreateWarehouseSuccessfully() {
        Warehouse request = new Warehouse();
        request.setBusinessUnitCode("MWH.100");
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(50);
        request.setStock(5);

        Warehouse response = resource.createANewWarehouseUnit(request);

        assertEquals("MWH.100", response.getBusinessUnitCode());
        assertEquals("AMSTERDAM-001", response.getLocation());
        assertEquals(50, response.getCapacity());
        assertEquals(5, response.getStock());

        verify(createWarehouseOperation).create(any());
    }

    @Test
    void shouldCreateWarehouseWithDefaultStockZero() {
        Warehouse request = new Warehouse();
        request.setBusinessUnitCode("MWH.101");
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(50);
        request.setStock(null);

        Warehouse response = resource.createANewWarehouseUnit(request);

        assertEquals(0, response.getStock());
        verify(createWarehouseOperation).create(any());
    }

    @Test
    void shouldThrow400WhenCreateFails() {
        Warehouse request = new Warehouse();
        request.setBusinessUnitCode("MWH.102");
        request.setLocation("INVALID");
        request.setCapacity(50);
        request.setStock(5);

        doThrow(new IllegalArgumentException("Invalid location"))
                .when(createWarehouseOperation)
                .create(any());

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.createANewWarehouseUnit(request));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldGetWarehouseByIdSuccessfully() {
        var domain = createDomainWarehouse("MWH.001", "ZWOLLE-001", 100, 20);

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(domain);

        Warehouse response = resource.getAWarehouseUnitByID("MWH.001");

        assertEquals("MWH.001", response.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", response.getLocation());
        assertEquals(100, response.getCapacity());
        assertEquals(20, response.getStock());
    }

    @Test
    void shouldThrow404WhenWarehouseNotFound() {
        when(warehouseRepository.findByBusinessUnitCode("MWH.404")).thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.getAWarehouseUnitByID("MWH.404"));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {
        var domain = createDomainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(domain);

        resource.archiveAWarehouseUnitByID("MWH.001");

        verify(archiveWarehouseOperation).archive(domain);
    }

    @Test
    void shouldThrow404WhenArchiveWarehouseNotFound() {
        when(warehouseRepository.findByBusinessUnitCode("MWH.404")).thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("MWH.404"));

        assertEquals(404, ex.getResponse().getStatus());
        verify(archiveWarehouseOperation, never()).archive(any());
    }

    @Test
    void shouldThrow400WhenArchiveFails() {
        var domain = createDomainWarehouse("MWH.001", "ZWOLLE-001", 100, 10);

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(domain);

        doThrow(new IllegalArgumentException("Already archived"))
                .when(archiveWarehouseOperation)
                .archive(domain);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("MWH.001"));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldReplaceWarehouseSuccessfully() {
        Warehouse request = new Warehouse();
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(200);
        request.setStock(50);

        var updated = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 200, 50);

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(updated);

        Warehouse response = resource.replaceTheCurrentActiveWarehouse("MWH.001", request);

        assertEquals("MWH.001", response.getBusinessUnitCode());
        assertEquals("AMSTERDAM-001", response.getLocation());
        assertEquals(200, response.getCapacity());
        assertEquals(50, response.getStock());

        verify(replaceWarehouseOperation).replace(any());
    }

    @Test
    void shouldReplaceWarehouseWithDefaultStockZero() {
        Warehouse request = new Warehouse();
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(200);
        request.setStock(null);

        var updated = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 200, 0);

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(updated);

        Warehouse response = resource.replaceTheCurrentActiveWarehouse("MWH.001", request);

        assertEquals(0, response.getStock());
        verify(replaceWarehouseOperation).replace(any());
    }

    @Test
    void shouldThrow400WhenReplaceFails() {
        Warehouse request = new Warehouse();
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(9999);
        request.setStock(10);

        doThrow(new IllegalArgumentException("Capacity exceeded"))
                .when(replaceWarehouseOperation)
                .replace(any());

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.replaceTheCurrentActiveWarehouse("MWH.001", request));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldSearchWarehousesByLocationSuccessfully() {
        var domain = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);

        when(searchWarehousesOperation.searchByLocation("AMSTERDAM-001"))
                .thenReturn(List.of(domain));

        List<Warehouse> result = resource.searchWarehousesByLocation("AMSTERDAM-001");

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
        assertEquals("AMSTERDAM-001", result.get(0).getLocation());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(20, result.get(0).getStock());

        verify(searchWarehousesOperation).searchByLocation("AMSTERDAM-001");
    }

    @Test
    void shouldThrow400WhenSearchByLocationFails() {
        when(searchWarehousesOperation.searchByLocation(""))
                .thenThrow(new IllegalArgumentException("Location cannot be null or blank"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchWarehousesByLocation(""));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow500WhenSearchByLocationUnexpectedError() {
        when(searchWarehousesOperation.searchByLocation("PUNE"))
                .thenThrow(new RuntimeException("DB error"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchWarehousesByLocation("PUNE"));

        assertEquals(500, ex.getResponse().getStatus());
    }

    @Test
    void shouldSearchWarehousesByCapacityRangeSuccessfully() {
        var domain = createDomainWarehouse("MWH.002", "ZWOLLE-001", 500, 100);

        when(searchWarehousesOperation.searchByCapacityRange(100, 600))
                .thenReturn(List.of(domain));

        List<Warehouse> result = resource.searchByCapacityRange(100, 600);

        assertEquals(1, result.size());
        assertEquals("MWH.002", result.get(0).getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.get(0).getLocation());
        assertEquals(500, result.get(0).getCapacity());
        assertEquals(100, result.get(0).getStock());

        verify(searchWarehousesOperation).searchByCapacityRange(100, 600);
    }

    @Test
    void shouldThrow400WhenSearchByCapacityRangeFails() {
        when(searchWarehousesOperation.searchByCapacityRange(600, 100))
                .thenThrow(new IllegalArgumentException(
                        "minCapacity cannot be greater than maxCapacity"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByCapacityRange(600, 100));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow500WhenSearchByCapacityRangeUnexpectedError() {
        when(searchWarehousesOperation.searchByCapacityRange(100, 600))
                .thenThrow(new RuntimeException("DB error"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByCapacityRange(100, 600));

        assertEquals(500, ex.getResponse().getStatus());
    }

    @Test
    void shouldSearchWarehousesByStockRangeSuccessfully() {
        var domain = createDomainWarehouse("MWH.003", "AMSTERDAM-001", 300, 50);

        when(searchWarehousesOperation.searchByStockRange(10, 100))
                .thenReturn(List.of(domain));

        List<Warehouse> result = resource.searchByStockRange(10, 100);

        assertEquals(1, result.size());
        assertEquals("MWH.003", result.get(0).getBusinessUnitCode());
        assertEquals("AMSTERDAM-001", result.get(0).getLocation());
        assertEquals(300, result.get(0).getCapacity());
        assertEquals(50, result.get(0).getStock());

        verify(searchWarehousesOperation).searchByStockRange(10, 100);
    }

    @Test
    void shouldThrow400WhenSearchByStockRangeFails() {
        when(searchWarehousesOperation.searchByStockRange(100, 10))
                .thenThrow(new IllegalArgumentException(
                        "minStock cannot be greater than maxStock"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByStockRange(100, 10));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow500WhenSearchByStockRangeUnexpectedError() {
        when(searchWarehousesOperation.searchByStockRange(10, 100))
                .thenThrow(new RuntimeException("DB error"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByStockRange(10, 100));

        assertEquals(500, ex.getResponse().getStatus());
    }

    @Test
    void shouldSearchWarehousesByArchivedStatusSuccessfully() {
        var domain = createDomainWarehouse("MWH.004", "ZWOLLE-001", 100, 10);

        when(searchWarehousesOperation.searchByArchivedStatus(false))
                .thenReturn(List.of(domain));

        List<Warehouse> result = resource.searchByArchivedStatus(false);

        assertEquals(1, result.size());
        assertEquals("MWH.004", result.get(0).getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.get(0).getLocation());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(10, result.get(0).getStock());

        verify(searchWarehousesOperation).searchByArchivedStatus(false);
    }

    @Test
    void shouldThrow400WhenSearchByArchivedStatusFails() {
        when(searchWarehousesOperation.searchByArchivedStatus(null))
                .thenThrow(new IllegalArgumentException("Archived status is invalid"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByArchivedStatus(null));

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow500WhenSearchByArchivedStatusUnexpectedError() {
        when(searchWarehousesOperation.searchByArchivedStatus(true))
                .thenThrow(new RuntimeException("DB error"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchByArchivedStatus(true));

        assertEquals(500, ex.getResponse().getStatus());
    }
    @Test
    void shouldSearchWarehousesWithLocationFilter() {

        var domain1 = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);
        var domain2 = createDomainWarehouse("MWH.002", "ZWOLLE-001", 500, 80);

        when(warehouseRepository.getAll())
                .thenReturn(List.of(domain1, domain2));

        List<Warehouse> result = resource.searchWarehouses(
                "AMSTERDAM-001",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
    }

    @Test
    void shouldSearchWarehousesWithCapacityRange() {

        var domain1 = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);
        var domain2 = createDomainWarehouse("MWH.002", "ZWOLLE-001", 500, 80);

        when(warehouseRepository.getAll())
                .thenReturn(List.of(domain1, domain2));

        List<Warehouse> result = resource.searchWarehouses(
                null,
                200,
                600,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("MWH.002", result.get(0).getBusinessUnitCode());
    }

    @Test
    void shouldSearchWarehousesWithSorting() {

        var domain1 = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);
        var domain2 = createDomainWarehouse("MWH.002", "ZWOLLE-001", 500, 80);

        when(warehouseRepository.getAll())
                .thenReturn(List.of(domain1, domain2));

        List<Warehouse> result = resource.searchWarehouses(
                null,
                null,
                null,
                null,
                null,
                null,
                "capacity",
                null,
                null,
                null
        );

        assertEquals(2, result.size());
        assertEquals("MWH.002", result.get(0).getBusinessUnitCode());
    }

    @Test
    void shouldSearchWarehousesWithPagination() {

        var domain1 = createDomainWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);
        var domain2 = createDomainWarehouse("MWH.002", "ZWOLLE-001", 500, 80);

        when(warehouseRepository.getAll())
                .thenReturn(List.of(domain1, domain2));

        List<Warehouse> result = resource.searchWarehouses(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                1
        );

        assertEquals(1, result.size());
        assertEquals("MWH.002", result.get(0).getBusinessUnitCode());
    }

    @Test
    void shouldThrow500WhenSearchFails() {

        when(warehouseRepository.getAll())
                .thenThrow(new RuntimeException("DB error"));

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.searchWarehouses(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        assertEquals(500, ex.getResponse().getStatus());
    }
}