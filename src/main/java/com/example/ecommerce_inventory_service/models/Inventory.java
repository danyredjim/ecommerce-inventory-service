package com.example.ecommerce_inventory_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private int availableStock;
    private int reservedStock;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @Version
    private Long version; // 🔥 Optimistic Locking
    
    
	public Inventory() {}

	public Inventory(Long id, Long productId, int availableStock, int reservedStock, Warehouse warehouse) {
		
		this.id = id;
		this.productId = productId;
		this.availableStock = availableStock;
		this.reservedStock = reservedStock;
		this.warehouse = warehouse;
	}

    public void reserve(int quantity) {
        if (availableStock < quantity) {
            throw new IllegalStateException("Not enough stock");
        }

        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    public void release(int quantity) {
        this.availableStock += quantity;
        this.reservedStock -= quantity;
    }
    



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public int getAvailableStock() {
		return availableStock;
	}

	public void setAvailableStock(int availableStock) {
		this.availableStock = availableStock;
	}

	public int getReservedStock() {
		return reservedStock;
	}

	public void setReservedStock(int reservedStock) {
		this.reservedStock = reservedStock;
	}

	public Warehouse getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}

    // getters & setters
    
}
