package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProductResourceTest {

    private ProductResource productResource;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() throws Exception {
        productResource = new ProductResource();
        productRepository = Mockito.mock(ProductRepository.class);

        Field field = ProductResource.class.getDeclaredField("productRepository");
        field.setAccessible(true);
        field.set(productResource, productRepository);
    }

    private Product createProduct(Long id, String name) {
        Product product = new Product();
        product.id = id;
        product.name = name;
        product.description = "Test Description";
        product.price = BigDecimal.valueOf(100.50);
        product.stock = 10;
        return product;
    }

    @Test
    void shouldGetAllProducts() {
        Product product = createProduct(1L, "Apple");

        when(productRepository.listAll(any(Sort.class)))
                .thenReturn(List.of(product));

        List<Product> result = productResource.get();

        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).name);

        verify(productRepository).listAll(any(Sort.class));
    }

    @Test
    void shouldGetSingleProductSuccessfully() {
        Product product = createProduct(1L, "Apple");

        when(productRepository.findById(1L))
                .thenReturn(product);

        Product result = productResource.getSingle(1L);

        assertNotNull(result);
        assertEquals(1L, result.id);
        assertEquals("Apple", result.name);
    }

    @Test
    void shouldThrow404WhenProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.getSingle(99L));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldCreateProductSuccessfully() {
        Product product = createProduct(null, "Apple");

        Response response = productResource.create(product);

        assertEquals(201, response.getStatus());
        verify(productRepository).persist(product);
    }

    @Test
    void shouldThrow422WhenCreateProductIdAlreadyPresent() {
        Product product = createProduct(1L, "Apple");

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.create(product));

        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow422WhenCreateProductNameMissing() {
        Product product = createProduct(null, "");

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.create(product));

        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Product existingProduct = createProduct(1L, "Old Product");
        Product updateRequest = createProduct(null, "New Product");
        updateRequest.description = "Updated Description";
        updateRequest.price = BigDecimal.valueOf(200.75);
        updateRequest.stock = 20;

        when(productRepository.findById(1L))
                .thenReturn(existingProduct);

        Product result = productResource.update(1L, updateRequest);

        assertEquals("New Product", result.name);
        assertEquals("Updated Description", result.description);
        assertEquals(BigDecimal.valueOf(200.75), result.price);
        assertEquals(20, result.stock);

        verify(productRepository).persist(existingProduct);
    }

    @Test
    void shouldThrow422WhenUpdateProductNameMissing() {
        Product updateRequest = createProduct(null, "");

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.update(1L, updateRequest));

        assertEquals(422, ex.getResponse().getStatus());
    }

    @Test
    void shouldThrow404WhenUpdateProductNotFound() {
        Product updateRequest = createProduct(null, "New Product");

        when(productRepository.findById(99L))
                .thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.update(99L, updateRequest));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        Product product = createProduct(1L, "Apple");

        when(productRepository.findById(1L))
                .thenReturn(product);

        Response response = productResource.delete(1L);

        assertEquals(204, response.getStatus());
        verify(productRepository).delete(product);
    }

    @Test
    void shouldThrow404WhenDeleteProductNotFound() {
        when(productRepository.findById(99L))
                .thenReturn(null);

        WebApplicationException ex = assertThrows(
                WebApplicationException.class,
                () -> productResource.delete(99L));

        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void shouldMapWebApplicationExceptionUsingErrorMapper() throws Exception {
        ProductResource.ErrorMapper errorMapper = new ProductResource.ErrorMapper();

        Field field = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(errorMapper, new com.fasterxml.jackson.databind.ObjectMapper());

        WebApplicationException exception =
                new WebApplicationException("Product not found", 404);

        Response response = errorMapper.toResponse(exception);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void shouldMapGenericExceptionUsingErrorMapper() throws Exception {
        ProductResource.ErrorMapper errorMapper = new ProductResource.ErrorMapper();

        Field field = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(errorMapper, new com.fasterxml.jackson.databind.ObjectMapper());

        RuntimeException exception = new RuntimeException("Unexpected error");

        Response response = errorMapper.toResponse(exception);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getEntity());
    }
}