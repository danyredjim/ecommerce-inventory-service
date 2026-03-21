package com.example.ecommerce_inventory_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce_inventory_service.DTOs.ReserveRequest;
import com.example.ecommerce_inventory_service.services.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Opción A
    @PostMapping("/reserve/most-stock")
    public ResponseEntity<Void> reserveMostStock(
            @RequestBody ReserveRequest request) {

        inventoryService.reserveMostStock(
                request.getProductId(),
                request.getQuantity());

        return ResponseEntity.ok().build();
    }

    // Opción B
    @PostMapping("/reserve/nearest")
    public ResponseEntity<Void> reserveNearest(
            @RequestBody ReserveRequest request) {

        inventoryService.reserveNearest(
                request.getProductId(),
                request.getQuantity(),
                request.getClientLatitude(),
                request.getClientLongitude());

        return ResponseEntity.ok().build();
    }

    // Opción C
    @PostMapping("/reserve/smart")
    public ResponseEntity<Void> reserveSmart(
            @RequestBody ReserveRequest request) {

        inventoryService.reserveSmart(
                request.getProductId(),
                request.getQuantity(),
                request.getClientLatitude(),
                request.getClientLongitude());

        return ResponseEntity.ok().build();
    }
}
