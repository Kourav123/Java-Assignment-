package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOG = Logger.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    LOG.info("Fetching all warehouses");

    return this.listAll().stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    LOG.infof("Creating warehouse with businessUnitCode: %s", warehouse.businessUnitCode);

    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);

    LOG.infof("Warehouse created successfully with businessUnitCode: %s", warehouse.businessUnitCode);
  }

  @Override
  public void update(Warehouse warehouse) {
    LOG.infof("Updating warehouse with businessUnitCode: %s", warehouse.businessUnitCode);

    DbWarehouse dbWarehouse =
        find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (dbWarehouse == null) {
      LOG.warnf("Warehouse not found for update with businessUnitCode: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse does not exist for businessUnitCode: " + warehouse.businessUnitCode);
    }

    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.archivedAt = warehouse.archivedAt;

    getEntityManager().merge(dbWarehouse);
    getEntityManager().flush();

    LOG.infof("Warehouse updated successfully with businessUnitCode: %s", warehouse.businessUnitCode);
  }

  @Override
  public void remove(Warehouse warehouse) {
    LOG.infof("Removing warehouse with businessUnitCode: %s", warehouse.businessUnitCode);

    DbWarehouse dbWarehouse =
        find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (dbWarehouse == null) {
      LOG.warnf("Warehouse not found for removal with businessUnitCode: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException(
          "Warehouse does not exist for businessUnitCode: " + warehouse.businessUnitCode);
    }

    delete(dbWarehouse);
    getEntityManager().flush();

    LOG.infof("Warehouse removed successfully with businessUnitCode: %s", warehouse.businessUnitCode);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    LOG.infof("Searching warehouse with businessUnitCode: %s", buCode);

    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();

    if (dbWarehouse == null) {
      LOG.infof("No warehouse found with businessUnitCode: %s", buCode);
      return null;
    }

    return dbWarehouse.toWarehouse();
  }
  @Override
  public List<Warehouse> findByLocation(String location) {
    LOG.infof("Finding warehouses by location: %s", location);

    return list("location = ?1", location).stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public List<Warehouse> findByCapacityBetween(Integer minCapacity, Integer maxCapacity) {
    LOG.infof(
        "Finding warehouses by capacity range -> minCapacity: %s, maxCapacity: %s",
        minCapacity, maxCapacity);

    if (minCapacity != null && maxCapacity != null) {
      return list("capacity >= ?1 and capacity <= ?2", minCapacity, maxCapacity).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    if (minCapacity != null) {
      return list("capacity >= ?1", minCapacity).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    if (maxCapacity != null) {
      return list("capacity <= ?1", maxCapacity).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    return listAll().stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public List<Warehouse> findByStockBetween(Integer minStock, Integer maxStock) {
    LOG.infof(
        "Finding warehouses by stock range -> minStock: %s, maxStock: %s",
        minStock, maxStock);

    if (minStock != null && maxStock != null) {
      return list("stock >= ?1 and stock <= ?2", minStock, maxStock).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    if (minStock != null) {
      return list("stock >= ?1", minStock).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    if (maxStock != null) {
      return list("stock <= ?1", maxStock).stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    return listAll().stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }

  @Override
  public List<Warehouse> findByArchived(Boolean archived) {
    LOG.infof("Finding warehouses by archived status: %s", archived);

    if (archived == null) {
      return listAll().stream()
          .map(DbWarehouse::toWarehouse)
          .toList();
    }

    String query = archived ? "archivedAt is not null" : "archivedAt is null";

    return list(query).stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }
  

}