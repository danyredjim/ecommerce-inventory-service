package com.example.ecommerce_inventory_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce_inventory_service.models.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
