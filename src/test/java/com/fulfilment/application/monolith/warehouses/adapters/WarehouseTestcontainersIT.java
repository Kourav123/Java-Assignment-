package com.fulfilment.application.monolith.warehouses.adapters;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseTestcontainersIT {

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    LocationGateway locationResolver;

    @Inject
    EntityManager em;

    @Inject
    CreateWarehouseUseCase createWarehouseUseCase;

    @BeforeEach
    @Transactional
    public void setup() {
        em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
    }

    @Test
    @Transactional
    public void testDatabaseUniqueConstraintOnBusinessUnitCode() {
        Warehouse warehouse1 = new Warehouse();
        warehouse1.businessUnitCode = "DB-UNIQUE-001";
        warehouse1.location = "AMSTERDAM-001";
        warehouse1.capacity = 50;
        warehouse1.stock = 10;

        createWarehouseUseCase.create(warehouse1);

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "DB-UNIQUE-001";
        dbWarehouse.location = "ZWOLLE-001";
        dbWarehouse.capacity = 30;
        dbWarehouse.stock = 5;
        dbWarehouse.createdAt = java.time.LocalDateTime.now();

        assertThrows(Exception.class, () -> {
            em.persist(dbWarehouse);
            em.flush();
        });
    }

    @Test
    @Transactional
    public void testQueryingMultipleWarehousesAtSameLocation() {
        for (int i = 0; i < 5; i++) {
            Warehouse warehouse = new Warehouse();
            warehouse.businessUnitCode = "QUERY-TEST-" + i;
            warehouse.location = "AMSTERDAM-001";
            warehouse.capacity = 20 + (i * 10);
            warehouse.stock = 5 + i;

            createWarehouseUseCase.create(warehouse);
        }

        List<Warehouse> all = warehouseRepository.getAll();

        assertTrue(all.size() >= 5);

        long amsterdamCount = all.stream()
                .filter(w -> "AMSTERDAM-001".equals(w.location))
                .count();

        assertEquals(5, amsterdamCount);
    }

    @Test
    @Transactional
    public void testNullFieldsHandling() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "NULL-TEST-001";
        dbWarehouse.location = "ZWOLLE-001";
        dbWarehouse.capacity = 50;
        dbWarehouse.stock = 10;
        dbWarehouse.createdAt = java.time.LocalDateTime.now();
        dbWarehouse.archivedAt = null;

        em.persist(dbWarehouse);
        em.flush();

        DbWarehouse found = em.find(DbWarehouse.class, dbWarehouse.id);

        assertNotNull(found);
        assertNull(found.archivedAt);
    }

    @Test
    public void testTransactionRollbackDoesNotPersist() {
        assertThrows(RuntimeException.class, this::performFailingTransaction);

        Warehouse found = warehouseRepository.findByBusinessUnitCode("ROLLBACK-TEST-001");

        assertNull(found, "Rolled back warehouse should not exist in database");
    }

    @Transactional
    public void performFailingTransaction() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "ROLLBACK-TEST-001";
        warehouse.location = "TILBURG-001";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        createWarehouseUseCase.create(warehouse);

        throw new RuntimeException("Simulated failure");
    }

    @Test
    @Transactional
    public void testComplexQueryByLocationAndCapacity() {
        createWarehouse("COMPLEX-1", "AMSTERDAM-001", 30);
        createWarehouse("COMPLEX-2", "AMSTERDAM-001", 50);
        createWarehouse("COMPLEX-3", "AMSTERDAM-001", 70);
        createWarehouse("COMPLEX-4", "ZWOLLE-001", 40);

        List<DbWarehouse> results = em.createQuery(
                        "SELECT w FROM DbWarehouse w WHERE w.location = :location AND w.capacity BETWEEN :min AND :max",
                        DbWarehouse.class)
                .setParameter("location", "AMSTERDAM-001")
                .setParameter("min", 40)
                .setParameter("max", 70)
                .getResultList();

        assertEquals(2, results.size());
    }

    private void createWarehouse(String code, String location, int capacity) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = code;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = 10;

        createWarehouseUseCase.create(warehouse);
    }
}