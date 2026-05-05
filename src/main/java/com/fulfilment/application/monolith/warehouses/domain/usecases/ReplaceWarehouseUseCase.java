package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {

    LOGGER.infof(
        "Replace request received -> BU Code: %s, New Location: %s, Capacity: %d, Stock: %d",
        newWarehouse.businessUnitCode,
        newWarehouse.location,
        newWarehouse.capacity,
        newWarehouse.stock
    );

    // Validation 1: Warehouse must exist
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      LOGGER.warnf("Warehouse not found for replace with BU Code: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + newWarehouse.businessUnitCode + "' does not exist");
    }

    // Validation 2: Warehouse must not be archived
    if (existing.archivedAt != null) {
      LOGGER.warnf("Attempt to replace archived warehouse with BU Code: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + newWarehouse.businessUnitCode + "' is archived and cannot be replaced");
    }

    // Validation 3: Location must be valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      LOGGER.warnf("Invalid location provided for replace: %s", newWarehouse.location);
      throw new IllegalArgumentException(
          "Location '" + newWarehouse.location + "' is not valid");
    }

    // Validation 4: Capacity validation
    if (newWarehouse.capacity > location.maxCapacity()) {
      LOGGER.warnf(
          "Capacity exceeds location limit -> requested: %d, max allowed: %d, location: %s",
          newWarehouse.capacity,
          location.maxCapacity(),
          newWarehouse.location
      );
      throw new IllegalArgumentException(
          "Warehouse capacity (" + newWarehouse.capacity +
          ") exceeds location max capacity (" + location.maxCapacity() + ")");
    }

    // Validation 5: Stock validation
    if (newWarehouse.stock > newWarehouse.capacity) {
      LOGGER.warnf(
          "Stock exceeds capacity -> stock: %d, capacity: %d",
          newWarehouse.stock,
          newWarehouse.capacity
      );
      throw new IllegalArgumentException(
          "Warehouse stock (" + newWarehouse.stock +
          ") exceeds warehouse capacity (" + newWarehouse.capacity + ")");
    }

    // Update fields
    existing.location = newWarehouse.location;
    existing.capacity = newWarehouse.capacity;
    existing.stock = newWarehouse.stock;

    LOGGER.debugf("Warehouse fields updated for BU Code: %s", newWarehouse.businessUnitCode);

    // Persist update
    warehouseStore.update(existing);

    LOGGER.infof("Warehouse replaced successfully with BU Code: %s", newWarehouse.businessUnitCode);
  }
}