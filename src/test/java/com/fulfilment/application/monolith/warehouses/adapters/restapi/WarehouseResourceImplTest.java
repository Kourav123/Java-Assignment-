package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
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

    @BeforeEach
    void setUp() throws Exception {
        resource = new WarehouseResourceImpl();

        warehouseRepository = Mockito.mock(WarehouseRepository.class);
        createWarehouseOperation = Mockito.mock(CreateWarehouseOperation.class);
        archiveWarehouseOperation = Mockito.mock(ArchiveWarehouseOperation.class);
        replaceWarehouseOperation = Mockito.mock(ReplaceWarehouseOperation.class);

        setField(resource, "warehouseRepository", warehouseRepository);
        setField(resource, "createWarehouseOperation", createWarehouseOperation);
        setField(resource, "archiveWarehouseOperation", archiveWarehouseOperation);
        setField(resource, "replaceWarehouseOperation", replaceWarehouseOperation);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void shouldListAllWarehousesUnits() {
        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = "MWH.001";
        domain.location = "ZWOLLE-001";
        domain.capacity = 100;
        domain.stock = 10;

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
                () -> resource.createANewWarehouseUnit(request)
        );

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldGetWarehouseByIdSuccessfully() {
        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = "MWH.001";
        domain.location = "ZWOLLE-001";
        domain.capacity = 100;
        domain.stock = 20;

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
                () -> resource.getAWarehouseUnitByID("MWH.404")
        );

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldArchiveWarehouseSuccessfully() {
        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = "MWH.001";

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(domain);

        resource.archiveAWarehouseUnitByID("MWH.001");

        verify(archiveWarehouseOperation).archive(domain);
    }

    @Test
    void shouldThrow404WhenArchiveWarehouseNotFound() {
        when(warehouseRepository.findByBusinessUnitCode("MWH.404")).thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("MWH.404")
        );

        assertEquals(404, ex.getResponse().getStatus());
        verify(archiveWarehouseOperation, never()).archive(any());
    }

    @Test
    void shouldThrow400WhenArchiveFails() {
        var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domain.businessUnitCode = "MWH.001";

        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(domain);

        doThrow(new IllegalArgumentException("Already archived"))
                .when(archiveWarehouseOperation)
                .archive(domain);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("MWH.001")
        );

        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void shouldReplaceWarehouseSuccessfully() {
        Warehouse request = new Warehouse();
        request.setLocation("AMSTERDAM-001");
        request.setCapacity(200);
        request.setStock(50);

        var updated = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        updated.businessUnitCode = "MWH.001";
        updated.location = "AMSTERDAM-001";
        updated.capacity = 200;
        updated.stock = 50;

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

        var updated = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        updated.businessUnitCode = "MWH.001";
        updated.location = "AMSTERDAM-001";
        updated.capacity = 200;
        updated.stock = 0;

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
                () -> resource.replaceTheCurrentActiveWarehouse("MWH.001", request)
        );

        assertEquals(400, ex.getResponse().getStatus());
    }
}