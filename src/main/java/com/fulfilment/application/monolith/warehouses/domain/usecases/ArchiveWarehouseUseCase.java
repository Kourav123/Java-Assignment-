package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    LOGGER.infof("Archive request received for warehouse BU Code: %s", warehouse.businessUnitCode);

    // Validation 1: Warehouse must exist
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

    if (existing == null) {
      LOGGER.warnf("Warehouse not found for archive with BU Code: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }

    // Validation 2: Warehouse must not already be archived
    if (existing.archivedAt != null) {
      LOGGER.warnf("Warehouse already archived with BU Code: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' is already archived");
    }

    // Set archive timestamp
    existing.archivedAt = java.time.LocalDateTime.now();
    LOGGER.debugf("Archive timestamp set for warehouse BU Code: %s", warehouse.businessUnitCode);

    // Update the warehouse
    warehouseStore.update(existing);

    LOGGER.infof("Warehouse archived successfully with BU Code: %s", warehouse.businessUnitCode);
  }
}