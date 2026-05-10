package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.SearchWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.usecases.SearchWarehousesUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
  @Inject 
  private SearchWarehousesUseCase searchWarehousesUseCase;
  @Inject
  private SearchWarehousesOperation searchWarehousesOperation;

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
  // Search methods for bonus functionality
  private List<Warehouse> mapToWarehouseResponse(
		    List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> warehouses) {

		  return warehouses.stream()
		      .map(this::toWarehouseResponse)
		      .toList();
		}

		
	    @GET
  @Path("/search")
  @Produces("application/json")
  public List<Warehouse> searchWarehouses(
      @QueryParam("location") String location, 
      @QueryParam("minCapacity") Integer minCapacity, 
      @QueryParam("maxCapacity") Integer maxCapacity, 
      @QueryParam("minStock") Integer minStock, 
      @QueryParam("maxStock") Integer maxStock, 
      @QueryParam("archived") Boolean archived,
      @QueryParam("sortBy") String sortBy, 
      @QueryParam("sortOrder") String sortOrder, 
      @QueryParam("page") Integer page, 
      @QueryParam("pageSize") Integer pageSize) {
    
    try {
      java.util.List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> warehouses = warehouseRepository.getAll();
      
      // Apply filters
      if (location != null && !location.trim().isEmpty()) {
        warehouses = warehouses.stream()
            .filter(w -> w.location.equalsIgnoreCase(location))
            .toList();
      }
      
      if (minCapacity != null) {
        warehouses = warehouses.stream()
            .filter(w -> w.capacity >= minCapacity)
            .toList();
      }
      
      if (maxCapacity != null) {
        warehouses = warehouses.stream()
            .filter(w -> w.capacity <= maxCapacity)
            .toList();
      }
      
      if (minStock != null) {
        warehouses = warehouses.stream()
            .filter(w -> w.stock >= minStock)
            .toList();
      }
      
      if (maxStock != null) {
        warehouses = warehouses.stream()
            .filter(w -> w.stock <= maxStock)
            .toList();
      }
      
      if (archived != null) {
        warehouses = warehouses.stream()
            .filter(w -> (archived ? w.archivedAt != null : w.archivedAt == null))
            .toList();
      }
      
      // Apply sorting
      if ("capacity".equals(sortBy)) {
        warehouses = warehouses.stream()
            .sorted((a, b) -> Integer.compare(b.capacity, a.capacity))
            .toList();
      } else if ("stock".equals(sortBy)) {
        warehouses = warehouses.stream()
            .sorted((a, b) -> Integer.compare(b.stock, a.stock))
            .toList();
      } else if ("location".equals(sortBy)) {
        warehouses = warehouses.stream()
            .sorted((a, b) -> a.location.compareTo(b.location))
            .toList();
      }
      
      // Apply sort order
      if ("desc".equalsIgnoreCase(sortOrder)) {
        java.util.Collections.reverse(warehouses);
      }
      
      // Apply pagination
      if (page != null && pageSize != null && pageSize > 0) {
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, warehouses.size());
        warehouses = warehouses.subList(startIndex, endIndex);
      }
      
      return warehouses.stream()
          .map(this::toWarehouseResponse)
          .toList();
          
    } catch (Exception e) {
      throw new WebApplicationException("Error searching warehouses: " + e.getMessage(), 500);
    }
  }
    //New Api
    @GET
    @Path("/search/location")
    @Produces("application/json")
    public List<Warehouse> searchWarehousesByLocation(
        @QueryParam("location") String location) {

      LOGGER.infof(
          "REST request received for warehouse search by location: %s",
          location);

      try {

        var warehouses =
            searchWarehousesOperation.searchByLocation(location);

        LOGGER.infof(
            "Warehouse search by location completed successfully. Total result: %d",
            warehouses.size());

        return mapToWarehouseResponse(warehouses);

      } catch (IllegalArgumentException ex) {

        LOGGER.warnf(
            "Validation failed during warehouse search by location: %s",
            ex.getMessage());

        throw new WebApplicationException(
            ex.getMessage(),
            400);

      } catch (Exception ex) {

        LOGGER.error(
            "Unexpected error occurred while searching warehouses by location",
            ex);

        throw new WebApplicationException(
            "Internal Server Error",
            500);
      }
    }
   
    @GET
    @Path("/search/capacity")
    public List<Warehouse> searchByCapacityRange(
        @QueryParam("minCapacity") Integer minCapacity,
        @QueryParam("maxCapacity") Integer maxCapacity) {

      LOGGER.infof(
          "REST request received for warehouse search by capacity range. minCapacity: %s, maxCapacity: %s",
          minCapacity,
          maxCapacity);

      try {

        var result =
            searchWarehousesOperation.searchByCapacityRange(
                minCapacity,
                maxCapacity);

        LOGGER.infof(
            "Warehouse search by capacity completed successfully. Total result: %d",
            result.size());

        return mapToWarehouseResponse(result);

      } catch (IllegalArgumentException ex) {

        LOGGER.warnf(
            "Validation failed during warehouse search by capacity: %s",
            ex.getMessage());

        throw new WebApplicationException(
            ex.getMessage(),
            400);

      } catch (Exception ex) {

        LOGGER.error(
            "Unexpected error occurred while searching warehouses by capacity",
            ex);

        throw new WebApplicationException(
            "Internal Server Error",
            500);
      }
    }

    @GET
    @Path("/search/stock")
    public List<Warehouse> searchByStockRange(
        @QueryParam("minStock") Integer minStock,
        @QueryParam("maxStock") Integer maxStock) {

      LOGGER.infof(
          "REST request received for warehouse search by stock range. minStock: %s, maxStock: %s",
          minStock,
          maxStock);

      try {

        var result =
            searchWarehousesOperation.searchByStockRange(
                minStock,
                maxStock);

        LOGGER.infof(
            "Warehouse search by stock completed successfully. Total result: %d",
            result.size());

        return mapToWarehouseResponse(result);

      } catch (IllegalArgumentException ex) {

        LOGGER.warnf(
            "Validation failed during warehouse search by stock: %s",
            ex.getMessage());

        throw new WebApplicationException(
            ex.getMessage(),
            400);

      } catch (Exception ex) {

        LOGGER.error(
            "Unexpected error occurred while searching warehouses by stock",
            ex);

        throw new WebApplicationException(
            "Internal Server Error",
            500);
      }
    }

    @GET
    @Path("/search/archived")
    public List<Warehouse> searchByArchivedStatus(
        @QueryParam("archived") Boolean archived) {

      LOGGER.infof(
          "REST request received for warehouse search by archived status: %s",
          archived);

      try {

        var result =
            searchWarehousesOperation.searchByArchivedStatus(
                archived);

        LOGGER.infof(
            "Warehouse search by archived status completed successfully. Total result: %d",
            result.size());

        return mapToWarehouseResponse(result);

      } catch (IllegalArgumentException ex) {

        LOGGER.warnf(
            "Validation failed during warehouse search by archived status: %s",
            ex.getMessage());

        throw new WebApplicationException(
            ex.getMessage(),
            400);

      } catch (Exception ex) {

        LOGGER.error(
            "Unexpected error occurred while searching warehouses by archived status",
            ex);

        throw new WebApplicationException(
            "Internal Server Error",
            500);
      }
    }


  }
