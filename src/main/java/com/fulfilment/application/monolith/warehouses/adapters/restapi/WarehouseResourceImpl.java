package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject
  private WarehouseRepository warehouseRepository;

  @Inject
  private CreateWarehouseOperation createWarehouseOperation;

  @Inject
  private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject
  private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    LOGGER.info("Fetching all warehouse units");

    List<Warehouse> result = warehouseRepository.getAll()
        .stream()
        .map(this::toWarehouseResponse)
        .toList();

    LOGGER.infof("Total warehouse units fetched: %d", result.size());
    return result;
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infof("Creating warehouse with BU Code: %s", data.getBusinessUnitCode());

    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      createWarehouseOperation.create(domainWarehouse);

      LOGGER.infof("Warehouse created successfully with BU Code: %s", data.getBusinessUnitCode());

      return toWarehouseResponse(domainWarehouse);

    } catch (IllegalArgumentException e) {
      LOGGER.warnf("Validation failed while creating warehouse: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (Exception ex) {
      LOGGER.errorf(ex, "Unexpected error while creating warehouse: %s", data.getBusinessUnitCode());
      throw new WebApplicationException("Internal Server Error", 500);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.infof("Fetching warehouse with BU Code: %s", id);

    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      LOGGER.warnf("Warehouse not found with BU Code: %s", id);
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    LOGGER.infof("Warehouse fetched successfully with BU Code: %s", id);
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.infof("Archiving warehouse with BU Code: %s", id);

    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      LOGGER.warnf("Warehouse not found for archive with BU Code: %s", id);
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      archiveWarehouseOperation.archive(domainWarehouse);

      LOGGER.infof("Warehouse archived successfully with BU Code: %s", id);

    } catch (IllegalArgumentException e) {
      LOGGER.warnf("Validation failed during archive: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (Exception ex) {
      LOGGER.errorf(ex, "Unexpected error while archiving warehouse: %s", id);
      throw new WebApplicationException("Internal Server Error", 500);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {

    LOGGER.infof("Replacing warehouse with BU Code: %s", businessUnitCode);

    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode;
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      replaceWarehouseOperation.replace(domainWarehouse);

      var updated = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

      LOGGER.infof("Warehouse replaced successfully with BU Code: %s", businessUnitCode);

      return toWarehouseResponse(updated);

    } catch (IllegalArgumentException e) {
      LOGGER.warnf("Validation failed during replace: %s", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (Exception ex) {
      LOGGER.errorf(ex, "Unexpected error while replacing warehouse: %s", businessUnitCode);
      throw new WebApplicationException("Internal Server Error", 500);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {

    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}