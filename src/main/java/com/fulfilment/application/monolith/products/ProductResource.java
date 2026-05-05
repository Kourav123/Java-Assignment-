package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class);

  @Inject
  ProductRepository productRepository;

  @GET
  public List<Product> get() {
    LOGGER.info("Fetching all products sorted by name");

    List<Product> products = productRepository.listAll(Sort.by("name"));

    LOGGER.infof("Total products fetched: %d", products.size());
    return products;
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    LOGGER.infof("Fetching product with id: %d", id);

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Product not found with id: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    LOGGER.infof("Product fetched successfully with id: %d", id);
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    LOGGER.info("Creating new product");

    if (product.id != null) {
      LOGGER.warnf("Invalid create request. Product id should not be provided. Provided id: %d", product.id);
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    if (product.name == null || product.name.isBlank()) {
      LOGGER.warn("Invalid create request. Product name is missing");
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    productRepository.persist(product);

    LOGGER.infof("Product created successfully with id: %d", product.id);

    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    LOGGER.infof("Updating product with id: %d", id);

    if (product.name == null || product.name.isBlank()) {
      LOGGER.warnf("Invalid update request. Product name is missing for id: %d", id);
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Product not found for update with id: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);

    LOGGER.infof("Product updated successfully with id: %d", id);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infof("Deleting product with id: %d", id);

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Product not found for deletion with id: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    productRepository.delete(entity);

    LOGGER.infof("Product deleted successfully with id: %d", id);

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
          "Exception occurred while processing product request. statusCode=%d, message=%s",
          code,
          exception.getMessage()
      );

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}