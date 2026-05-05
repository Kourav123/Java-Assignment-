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
  public List<Warehouse> search(String location, Integer minCapacity,Integer maxCapacity,String sortBy,String sortOrder,int page,int pageSize) {

    LOG.infof("Search request received -> location: %s, minCapacity: %s, maxCapacity: %s, sortBy: %s, sortOrder: %s, page: %d, pageSize: %d",
        location, minCapacity, maxCapacity, sortBy, sortOrder, page, pageSize);

    StringBuilder query = new StringBuilder("archivedAt is null");
    Map<String, Object> params = new HashMap<>();

    if (location != null && !location.isBlank()) {
      query.append(" and location = :location");
      params.put("location", location);
    }

    if (minCapacity != null) {
      query.append(" and capacity >= :minCapacity");
      params.put("minCapacity", minCapacity);
    }

    if (maxCapacity != null) {
      query.append(" and capacity <= :maxCapacity");
      params.put("maxCapacity", maxCapacity);
    }

    if (!"capacity".equalsIgnoreCase(sortBy)) {
      sortBy = "createdAt";
    }

    if (!"desc".equalsIgnoreCase(sortOrder)) {
      sortOrder = "asc";
    }

    query.append(" order by ").append(sortBy).append(" ").append(sortOrder);

    LOG.debugf("Executing search query: %s with params: %s", query, params);

    List<Warehouse> result = find(query.toString(), params)
        .page(page, pageSize)
        .list()
        .stream()
        .map(DbWarehouse::toWarehouse)
        .toList();

    LOG.infof("Search completed. Total results fetched: %d", result.size());

    return result;
  }
}