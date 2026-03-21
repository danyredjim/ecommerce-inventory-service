package com.example.ecommerce_inventory_service.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.ecommerce_common_events.OrderCreatedEvent;
import com.example.ecommerce_common_events.StockFailedEvent;
import com.example.ecommerce_common_events.StockReservedEvent;
import com.example.ecommerce_inventory_service.models.Inventory;
import com.example.ecommerce_inventory_service.models.ProcessedEvent;
import com.example.ecommerce_inventory_service.repositories.InventoryRepository;
import com.example.ecommerce_inventory_service.repositories.ProcessedEventRepository;

import jakarta.transaction.Transactional;

@Service
public class InventoryListener {
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	@Autowired
	ProcessedEventRepository processedEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
  
    //InventoryListener versión PRO    
    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    @Transactional
    public void handle2(OrderCreatedEvent event) {

        // 🔥 1️⃣ Idempotencia
        if (processedEventRepository.existsById(event.getEventId())) {
            return;
        }

        try {
            Inventory inventory = inventoryRepository
                    .findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 🔥 2️⃣ Reserva dentro del dominio
            inventory.reserve(event.getQuantity());
            inventoryRepository.save(inventory);

            // 🔥 3️⃣ Marcar evento como procesado
            processedEventRepository.save(new ProcessedEvent(event.getEventId()));

            // 🔥 4️⃣ Publicar evento de éxito
            kafkaTemplate.send("stock-reserved", new StockReservedEvent(
                            UUID.randomUUID().toString(),
                            event.getOrderId(),
                            event.getProductId(),
                            event.getQuantity(),
                            Instant.now()
                    )
            );

        } catch (IllegalStateException e) {

            // 🔥 5️⃣ Publicar fallo de stock
            kafkaTemplate.send(
                    "stock-failed",
                    new StockFailedEvent(
                            UUID.randomUUID().toString(),
                            event.getOrderId(),
                            Instant.now()
                    )
            );
        }
    }
}
