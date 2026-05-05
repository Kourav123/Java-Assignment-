package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {

    LOGGER.infof("Create warehouse request received -> BU Code: %s, Location: %s, Capacity: %d, Stock: %d",
        warehouse.businessUnitCode, warehouse.location, warehouse.capacity, warehouse.stock);

    // Validation 1: Check if already exists
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      LOGGER.warnf("Warehouse already exists with BU Code: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' already exists");
    }

    // Validation 2: Validate location
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      LOGGER.warnf("Invalid location provided: %s", warehouse.location);
      throw new IllegalArgumentException("Location '" + warehouse.location + "' is not valid");
    }

    // Validation 3: Capacity check
    if (warehouse.capacity > location.maxCapacity()) {
      LOGGER.warnf("Capacity exceeds limit -> requested: %d, max allowed: %d for location: %s",
          warehouse.capacity, location.maxCapacity(), warehouse.location);
      throw new IllegalArgumentException(
          "Warehouse capacity (" + warehouse.capacity +
          ") exceeds location max capacity (" + location.maxCapacity() + ")");
    }

    // Validation 4: Stock check
    if (warehouse.stock > warehouse.capacity) {
      LOGGER.warnf("Stock exceeds capacity -> stock: %d, capacity: %d",
          warehouse.stock, warehouse.capacity);
      throw new IllegalArgumentException(
          "Warehouse stock (" + warehouse.stock +
          ") exceeds warehouse capacity (" + warehouse.capacity + ")");
    }

    // Set created timestamp
    warehouse.createdAt = java.time.LocalDateTime.now();
    LOGGER.debugf("Set createdAt timestamp for BU Code: %s", warehouse.businessUnitCode);

    // Persist warehouse
    warehouseStore.create(warehouse);

    LOGGER.infof("Warehouse created successfully -> BU Code: %s", warehouse.businessUnitCode);
  }
}