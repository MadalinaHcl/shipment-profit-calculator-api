package com.shipmentprofit.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income")
public class Income extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipment_id", nullable = false, unique = true)
  private Shipment shipment;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "description")
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected Income() {}

  public Income(Shipment shipment, BigDecimal amount, String description) {
    this.shipment = shipment;
    this.amount = amount;
    this.description = description;
    this.createdAt = LocalDateTime.now();
  }

  public Shipment getShipment() {
    return shipment;
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
