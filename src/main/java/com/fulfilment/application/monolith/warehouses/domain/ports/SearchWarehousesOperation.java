package com.fulfilment.application.monolith.warehouses.domain.ports;
import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface SearchWarehousesOperation {
	
	    List<Warehouse> searchByLocation(String location);

	    List<Warehouse> searchByCapacityRange(Integer minCapacity, Integer maxCapacity);

	    List<Warehouse> searchByStockRange(Integer minStock, Integer maxStock);

	    List<Warehouse> searchByArchivedStatus(Boolean archived);
	}

