package com.example.ecommerce_inventory_service.services;

import org.springframework.stereotype.Service;

import com.example.ecommerce_inventory_service.models.Inventory;
import com.example.ecommerce_inventory_service.repositories.InventoryRepository;

import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // ===============================
    // 🟢 OPCIÓN A - MÁS STOCK
    // ===============================
    @Transactional
    public void reserveMostStock(Long productId, int qty) {

        List<Inventory> inventories =
                inventoryRepository.findAvailableInventories(productId);

        Inventory best = inventories.stream()
                .filter(i -> i.getAvailableStock() >= qty)
                .max(Comparator.comparingInt(Inventory::getAvailableStock))
                .orElseThrow(() -> new RuntimeException("Stock insuficiente"));

        lockAndReserve(best, qty);
    }

    // ===============================
    // 🟢 OPCIÓN B - MÁS CERCANO
    // ===============================
    @Transactional
    public void reserveNearest(Long productId, int qty,
                               double clientLat, double clientLon) {

        List<Inventory> inventories =
                inventoryRepository.findAvailableInventories(productId);

        Inventory best = inventories.stream()
                .filter(i -> i.getAvailableStock() >= qty)
                .min(Comparator.comparingDouble(i ->
                        distance(clientLat, clientLon,
                                i.getWarehouse().getLatitude(),
                                i.getWarehouse().getLongitude())))
                .orElseThrow(() -> new RuntimeException("Stock insuficiente"));

        lockAndReserve(best, qty);
    }

    // ===============================
    // 🟢 OPCIÓN C - SUFICIENTE + CERCANO
    // ===============================
    @Transactional
    public void reserveSmart(Long productId, int qty,
                             double clientLat, double clientLon) {

        List<Inventory> inventories =
                inventoryRepository.findAvailableInventories(productId);

        Inventory best = inventories.stream()
                .filter(i -> i.getAvailableStock() >= qty)
                .min(Comparator.comparingDouble(i ->
                        distance(clientLat, clientLon,
                                i.getWarehouse().getLatitude(),
                                i.getWarehouse().getLongitude())))
                .orElseThrow(() -> new RuntimeException("Stock insuficiente"));

        lockAndReserve(best, qty);
    }

    // ===============================
    // 🔒 BLOQUEO Y RESERVA REAL
    // ===============================
    private void lockAndReserve(Inventory selected, int qty) {

        Inventory locked = inventoryRepository
                .findForUpdate(selected.getProductId(),
                        selected.getWarehouse().getId())
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        reserve(locked, qty);
    }

    // ===============================
    // 🧠 MÉTODO RESERVE REAL
    // ===============================
    private void reserve(Inventory inventory, int qty) {

        if (inventory.getAvailableStock() < qty) {
            throw new RuntimeException("Stock insuficiente");
        }

        inventory.setAvailableStock(
                inventory.getAvailableStock() - qty);

        inventory.setReservedStock(
                inventory.getReservedStock() + qty);
    }

    // ===============================
    // 📍 CÁLCULO DISTANCIA (Haversine simple)
    // ===============================
    private double distance(double lat1, double lon1,
                            double lat2, double lon2) {

        double R = 6371; // km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
    
    
    @Transactional
    public void reserveSplitShipment(Long productId, int qty,
                                     double clientLat, double clientLon) {

        List<Inventory> inventories =
                inventoryRepository.findAvailableInventories(productId);

        // Ordenamos por cercanía
        inventories.sort(Comparator.comparingDouble(i ->
                distance(clientLat, clientLon,
                        i.getWarehouse().getLatitude(),
                        i.getWarehouse().getLongitude())
        ));

        int remaining = qty;

        for (Inventory inv : inventories) {

            if (remaining <= 0) break;

            int canTake = Math.min(inv.getAvailableStock(), remaining);

            if (canTake > 0) {

                // 🔒 Lock real
                Inventory locked = inventoryRepository
                        .findForUpdate(inv.getProductId(),
                                inv.getWarehouse().getId())
                        .orElseThrow();

                reserve(locked, canTake);

                remaining -= canTake;
            }
        }

        if (remaining > 0) {
            throw new RuntimeException("Stock total insuficiente");
        }
    }
}
