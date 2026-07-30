package com.shipmentprofit.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment")
public class Shipment extends AbstractEntity {

    @Column(name = "shipment_code", nullable = false, unique = true, length = 50)
    private String shipmentCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Shipment() {
    }

    public Shipment(String shipmentCode) {
        this.shipmentCode = shipmentCode;
        this.createdAt = LocalDateTime.now();
    }

    public String getShipmentCode() {
        return shipmentCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
