package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class);

  @Inject
  Event<StoreCreatedEvent> storeCreatedEvent;

  @Inject
  Event<StoreUpdatedEvent> storeUpdatedEvent;

  @GET
  public List<Store> get() {
    LOGGER.info("Fetching all stores sorted by name");

    List<Store> stores = Store.listAll(Sort.by("name"));

    LOGGER.infof("Total stores fetched: %d", stores.size());
    return stores;
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    LOGGER.infof("Fetching store with id: %d", id);

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found with id: %d", id);
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    LOGGER.infof("Store fetched successfully with id: %d", id);
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    LOGGER.info("Creating new store");

    if (store.id != null) {
      LOGGER.warnf("Invalid create request. Store id should not be provided. Provided id: %d", store.id);
      throw new WebApplicationException("Id should not be provided.", 422);
    }

    if (store.name == null || store.name.isBlank()) {
      LOGGER.warn("Invalid create request. Store name is missing");
      throw new WebApplicationException("Store name is required.", 422);
    }

    try {
      store.persist();

      LOGGER.infof("Store created successfully with id: %d", store.id);

      storeCreatedEvent.fire(new StoreCreatedEvent(store));

      LOGGER.infof("StoreCreatedEvent fired successfully for store id: %d", store.id);

    } catch (Exception exception) {
      LOGGER.errorf(
          exception,
          "Error while creating store. name=%s",
          store.name
      );
      throw new WebApplicationException("Error while creating store", 500);
    }

    return Response.status(201).entity(store).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(@PathParam("id") Long id, Store updatedStore) {
    LOGGER.infof("Updating store with id: %d", id);

    if (updatedStore.name == null || updatedStore.name.isBlank()) {
      LOGGER.warnf("Invalid update request. Store name is missing for id: %d", id);
      throw new WebApplicationException("Store name is required.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for update with id: %d", id);
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    LOGGER.infof("Store updated successfully with id: %d", id);

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    LOGGER.infof("StoreUpdatedEvent fired successfully for store id: %d", id);

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(@PathParam("id") Long id, Store updatedStore) {
    LOGGER.infof("Patching store with id: %d", id);

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for patch with id: %d", id);
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    boolean updated = false;

    if (updatedStore.name != null && !updatedStore.name.isBlank()) {
      entity.name = updatedStore.name;
      updated = true;
      LOGGER.debugf("Store name updated for id: %d", id);
    }

    if (updatedStore.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
      updated = true;
      LOGGER.debugf("Store quantityProductsInStock updated for id: %d", id);
    }

    if (!updated) {
      LOGGER.warnf("Patch request received with no valid fields for store id: %d", id);
    }

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    LOGGER.infof("Store patched successfully and StoreUpdatedEvent fired for id: %d", id);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    LOGGER.infof("Deleting store with id: %d", id);

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for deletion with id: %d", id);
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    entity.delete();

    LOGGER.infof("Store deleted successfully with id: %d", id);

    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      int code = 500;

      if (exception instanceof WebApplicationException webApplicationException) {
        code = webApplicationException.getResponse().getStatus();
      }

      LOGGER.errorf(
          exception,
          "Exception occurred while processing store request. statusCode=%d, message=%s",
          code,
          exception.getMessage()
      );

      ObjectNode json = objectMapper.createObjectNode();
      json.put("exceptionType", exception.getClass().getName());
      json.put("code", code);

      if (exception.getMessage() != null) {
        json.put("error", exception.getMessage());
      }

      return Response.status(code).entity(json).build();
    }
  }
}