package com.shipmentprofit.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost", uniqueConstraints = @UniqueConstraint(columnNames = {"shipment_id", "cost_type"}))
public class Cost extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false, length = 20)
    private CostType costType;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Cost() {
    }

    public Cost(Shipment shipment, CostType costType, BigDecimal amount, String description) {
        this.shipment = shipment;
        this.costType = costType;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Shipment getShipment() {
        return shipment;
    }

    public CostType getCostType() {
        return costType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
