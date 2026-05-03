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

  @Inject Event<StoreCreatedEvent> storeCreatedEvent;
  @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }
    return entity;
  }

  // 🔥 FINAL FIXED CREATE METHOD
  @POST
  @Transactional
  public Response create(Store store) {

    if (store.id != null) {
      throw new WebApplicationException("Id should not be provided.", 422);
    }

    if (store.name == null || store.name.isBlank()) {
      throw new WebApplicationException("Store name is required.", 422);
    }

    try {
      store.persist();

      // ✅ fire event ONLY after success
      storeCreatedEvent.fire(new StoreCreatedEvent(store));

    } catch (Exception e) {
      // ❗ IMPORTANT: return 500 (as expected by test)
      throw new WebApplicationException("Error while creating store", 500);
    }

    return Response.status(201).entity(store).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(@PathParam("id") Long id, Store updatedStore) {

    if (updatedStore.name == null) {
      throw new WebApplicationException("Store name is required.", 422);
    }

    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(@PathParam("id") Long id, Store updatedStore) {

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }

    if (updatedStore.name != null) {
      entity.name = updatedStore.name;
    }

    if (updatedStore.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id " + id + " does not exist.", 404);
    }
    entity.delete();
    return Response.status(204).build();
  }

  // 🔥 GLOBAL ERROR HANDLER
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

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