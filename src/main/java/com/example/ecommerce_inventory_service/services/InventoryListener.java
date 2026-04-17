package com.example.ecommerce_inventory_service.services;

import java.time.Instant;
import java.util.UUID;

import com.example.events.OrderAvroCreatedEvent;
import com.example.events.StockAvroFailedEvent;
import com.example.events.StockAvroReservedEvent;
import org.apache.avro.specific.SpecificRecord;
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

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Service
public class InventoryListener {
	
	@Autowired
	InventoryRepository inventoryRepository;
	
	@Autowired
	ProcessedEventRepository processedEventRepository;

    //private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;


    public InventoryListener(KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
  
    //InventoryListener versión PRO    
    /*@KafkaListener(topics = "order-created", groupId = "inventory-group")
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
    }*/

    @KafkaListener(
            topics = "order-created",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleWithAvro(StockAvroReservedEvent event) {

        log.info("Evento recibido: {}", event);

        String eventId = event.getEventId().toString();

        // 🔥 1️⃣ Idempotencia
        if (processedEventRepository.existsById(eventId)) {
            log.warn("Evento ya procesado: {}", eventId);
            return;
        }

        try {
            Inventory inventory = inventoryRepository
                    .findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 🔥 2️⃣ Reserva
            inventory.reserve(event.getQuantity());
            inventoryRepository.save(inventory);

            // 🔥 3️⃣ Marcar procesado
            processedEventRepository.save(new ProcessedEvent(eventId));

            // 🔥 4️⃣ Evento OK
            StockAvroReservedEvent stockEvent =
                    StockAvroReservedEvent.newBuilder()
                            .setEventId(UUID.randomUUID().toString())
                            .setUuid(UUID.randomUUID().toString())
                            .setOrderId(event.getOrderId())
                            .setProductId(event.getProductId())
                            .setQuantity(event.getQuantity())
                            .setInstant(Instant.now().toString())
                            .build();

            kafkaTemplate.send("stock-reserved", stockEvent);

            log.info("Stock reservado OK para orderId={}", event.getOrderId());

        } catch (IllegalStateException e) {

            // 🔥 5️⃣ Evento FAIL
            StockAvroFailedEvent failedEvent =
                    StockAvroFailedEvent.newBuilder()
                            .setEventId(UUID.randomUUID().toString())
                            .setOrderId(event.getOrderId())
                            .setReason("OUT_OF_STOCK")
                            .setInstant(Instant.now().toString())
                            .build();

            kafkaTemplate.send("stock-failed", failedEvent);

            log.warn("Stock insuficiente para orderId={}", event.getOrderId());
        }
    }
}
