package com.example.ecommerce_inventory_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce_inventory_service.models.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	
	Optional<Inventory> findByProductId(Long productId);
	
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.productId = :productId
        AND i.availableStock > 0
    """)
    List<Inventory> findAvailableInventories(@Param("productId") Long productId);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.productId = :productId
        AND i.warehouse.id = :warehouseId
    """)
    Optional<Inventory> findForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );
}
