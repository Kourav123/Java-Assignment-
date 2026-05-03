package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseRepositoryUnitTest {

    private WarehouseRepository repository;

    @BeforeEach
    void setup() {
        repository = spy(new WarehouseRepository());
    }

    @Test
    void shouldCoverGetAll() {

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "TEST-001";
        dbWarehouse.location = "ZWOLLE-001";
        dbWarehouse.capacity = 100;
        dbWarehouse.stock = 10;

        doReturn(List.of(dbWarehouse))
                .when(repository)
                .listAll();

        List<Warehouse> result = repository.getAll();

        assertEquals(1, result.size());
        assertEquals("TEST-001",
                result.get(0).businessUnitCode);
    }

    @Test
    void shouldCoverCreate() {

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "TEST-002";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 20;
        warehouse.createdAt = LocalDateTime.now();

        doNothing().when(repository)
                .persist(any(DbWarehouse.class));

        repository.create(warehouse);

        verify(repository, times(1))
                .persist(any(DbWarehouse.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCoverUpdate() {

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "TEST-003";
        dbWarehouse.location = "OLD";
        dbWarehouse.capacity = 100;
        dbWarehouse.stock = 10;

        Warehouse updated = new Warehouse();
        updated.businessUnitCode = "TEST-003";
        updated.location = "NEW";
        updated.capacity = 500;
        updated.stock = 50;
        updated.archivedAt = LocalDateTime.now();

        PanacheQuery<DbWarehouse> query =
                mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find("businessUnitCode",
                        "TEST-003");

        when(query.firstResult())
                .thenReturn(dbWarehouse);

        EntityManager entityManager =
                mock(EntityManager.class);

        doReturn(entityManager)
                .when(repository)
                .getEntityManager();

        repository.update(updated);

        assertEquals("NEW", dbWarehouse.location);
        assertEquals(500, dbWarehouse.capacity);
        assertEquals(50, dbWarehouse.stock);

        verify(entityManager).merge(dbWarehouse);
        verify(entityManager).flush();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowExceptionWhenUpdateWarehouseDoesNotExist() {

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "UNKNOWN";

        PanacheQuery<DbWarehouse> query =
                mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find("businessUnitCode",
                        "UNKNOWN");

        when(query.firstResult())
                .thenReturn(null);

        IllegalArgumentException ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> repository.update(warehouse));

        assertEquals(
                "Warehouse does not exist",
                ex.getMessage());
    }

    @Test
    void shouldCoverRemove() {

        Warehouse warehouse = new Warehouse();

        UnsupportedOperationException ex =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> repository.remove(warehouse));

        assertEquals(
                "Unimplemented method 'remove'",
                ex.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCoverFindByBusinessUnitCode() {

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "TEST-004";
        dbWarehouse.location = "TILBURG-001";
        dbWarehouse.capacity = 300;
        dbWarehouse.stock = 30;

        PanacheQuery<DbWarehouse> query =
                mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find("businessUnitCode",
                        "TEST-004");

        when(query.firstResult())
                .thenReturn(dbWarehouse);

        Warehouse result =
                repository.findByBusinessUnitCode("TEST-004");

        assertNotNull(result);
        assertEquals("TEST-004",
                result.businessUnitCode);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnNullWhenWarehouseNotFound() {

        PanacheQuery<DbWarehouse> query =
                mock(PanacheQuery.class);

        doReturn(query)
                .when(repository)
                .find("businessUnitCode",
                        "UNKNOWN");

        when(query.firstResult())
                .thenReturn(null);

        Warehouse result =
                repository.findByBusinessUnitCode("UNKNOWN");

        assertNull(result);
    }
}