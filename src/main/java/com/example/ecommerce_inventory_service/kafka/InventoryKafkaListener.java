package com.example.ecommerce_inventory_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce_common_events.OrderCreatedEvent;
import com.example.ecommerce_common_events.StockFailedEvent;
import com.example.ecommerce_common_events.StockReservedEvent;
import com.example.ecommerce_inventory_service.services.InventoryService;

@Component
public class InventoryKafkaListener {

   /* private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryKafkaListener(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        try {

            inventoryService.reserveSplitShipment(
                    event.getProductId(),
                    event.getQuantity(),
                    event.getClientLat(),
                    event.getClientLon());

            kafkaTemplate.send("stock-reserved", new StockReservedEvent(event.getOrderId()));

        } catch (Exception e) {

            kafkaTemplate.send("stock-failed", new StockFailedEvent(event.getOrderId()));
        }
    }*/
}