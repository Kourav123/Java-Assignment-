package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SearchWarehousesUseCase implements SearchWarehousesOperation {

  private static final Logger LOGGER = Logger.getLogger(SearchWarehousesUseCase.class);

  private final WarehouseStore warehouseStore;

  public SearchWarehousesUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public List<Warehouse> searchByLocation(String location) {
    LOGGER.infof("Searching warehouses by location: %s", location);

    if (location == null || location.isBlank()) {
      LOGGER.warn("Location is null or blank");
      throw new IllegalArgumentException("Location cannot be null or blank");
    }

    List<Warehouse> result = warehouseStore.findByLocation(location);

    LOGGER.infof("Search by location completed. Total result: %d", result.size());
    return result;
  }

  @Override
  public List<Warehouse> searchByCapacityRange(Integer minCapacity, Integer maxCapacity) {
    LOGGER.infof(
        "Searching warehouses by capacity range -> minCapacity: %s, maxCapacity: %s",
        minCapacity, maxCapacity);

    if (minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
      LOGGER.warnf(
          "Invalid capacity range -> minCapacity: %d, maxCapacity: %d",
          minCapacity, maxCapacity);
      throw new IllegalArgumentException("minCapacity cannot be greater than maxCapacity");
    }

    List<Warehouse> result = warehouseStore.findByCapacityBetween(minCapacity, maxCapacity);

    LOGGER.infof("Search by capacity range completed. Total result: %d", result.size());
    return result;
  }

  @Override
  public List<Warehouse> searchByStockRange(Integer minStock, Integer maxStock) {
    LOGGER.infof(
        "Searching warehouses by stock range -> minStock: %s, maxStock: %s",
        minStock, maxStock);

    if (minStock != null && maxStock != null && minStock > maxStock) {
      LOGGER.warnf("Invalid stock range -> minStock: %d, maxStock: %d", minStock, maxStock);
      throw new IllegalArgumentException("minStock cannot be greater than maxStock");
    }

    List<Warehouse> result = warehouseStore.findByStockBetween(minStock, maxStock);

    LOGGER.infof("Search by stock range completed. Total result: %d", result.size());
    return result;
  }

  @Override
  public List<Warehouse> searchByArchivedStatus(Boolean archived) {
    LOGGER.infof("Searching warehouses by archived status: %s", archived);

    List<Warehouse> result = warehouseStore.findByArchived(archived);

    LOGGER.infof("Search by archived status completed. Total result: %d", result.size());
    return result;
  }
}