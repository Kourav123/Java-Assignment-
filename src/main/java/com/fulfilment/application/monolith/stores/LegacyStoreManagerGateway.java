package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);

  public void createStoreOnLegacySystem(Store store) {
    LOGGER.infof("Sending create request to legacy system for store: %s", store.name);
    writeToFile(store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    LOGGER.infof("Sending update request to legacy system for store: %s", store.name);
    writeToFile(store);
  }

  private void writeToFile(Store store) {
    Path tempFile = null;

    try {
      // Step 1: Create temp file
      tempFile = Files.createTempFile(store.name, ".txt");
      LOGGER.infof("Temporary file created at: %s", tempFile.toString());

      // Step 2: Write data
      String content =
          "Store created. [ name =" + store.name +
          " ] [ items on stock =" + store.quantityProductsInStock + "]";

      Files.write(tempFile, content.getBytes());
      LOGGER.debug("Data written to temporary file");

      // Step 3: Read back for verification
      String readContent = new String(Files.readAllBytes(tempFile));
      LOGGER.debugf("Data read from file: %s", readContent);

    } catch (Exception exception) {
      LOGGER.errorf(
          exception,
          "Error while processing legacy store operation for store: %s",
          store.name
      );
      throw new RuntimeException("Failed to process legacy store operation", exception);

    } finally {
      // Step 4: Delete temp file safely
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
          LOGGER.infof("Temporary file deleted: %s", tempFile.toString());
        } catch (Exception ex) {
          LOGGER.warnf("Failed to delete temporary file: %s", tempFile.toString());
        }
      }
    }
  }
}