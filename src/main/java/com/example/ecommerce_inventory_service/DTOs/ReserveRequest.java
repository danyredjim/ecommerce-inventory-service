package com.example.ecommerce_inventory_service.DTOs;

public class ReserveRequest {

    private Long productId;
    private int quantity;

    private Double clientLatitude;
    private Double clientLongitude;
    
    
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Double getClientLatitude() {
		return clientLatitude;
	}
	public void setClientLatitude(Double clientLatitude) {
		this.clientLatitude = clientLatitude;
	}
	public Double getClientLongitude() {
		return clientLongitude;
	}
	public void setClientLongitude(Double clientLongitude) {
		this.clientLongitude = clientLongitude;
	}

    // getters & setters
    
}
