package com.shipmentprofit.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "profit_loss", uniqueConstraints = @UniqueConstraint(columnNames = "shipment_id"))
public class ProfitLoss extends AbstractEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipment_id", nullable = false, unique = true)
  private Shipment shipment;

  @Column(name = "total_income", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalIncome;

  @Column(name = "total_costs", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalCosts;

  @Column(name = "profit_or_loss", nullable = false, precision = 12, scale = 2)
  private BigDecimal profitOrLoss;

  @Column(name = "calculated_at", nullable = false)
  private LocalDateTime calculatedAt;

  protected ProfitLoss() {}

  public ProfitLoss(
      Shipment shipment, BigDecimal totalIncome, BigDecimal totalCosts, BigDecimal profitOrLoss) {
    this.shipment = shipment;
    this.totalIncome = totalIncome;
    this.totalCosts = totalCosts;
    this.profitOrLoss = profitOrLoss;
    this.calculatedAt = LocalDateTime.now();
  }

  public Shipment getShipment() {
    return shipment;
  }

  public BigDecimal getTotalIncome() {
    return totalIncome;
  }

  public BigDecimal getTotalCosts() {
    return totalCosts;
  }

  public BigDecimal getProfitOrLoss() {
    return profitOrLoss;
  }

  public LocalDateTime getCalculatedAt() {
    return calculatedAt;
  }

  public void update(BigDecimal totalIncome, BigDecimal totalCosts, BigDecimal profitOrLoss) {
    this.totalIncome = totalIncome;
    this.totalCosts = totalCosts;
    this.profitOrLoss = profitOrLoss;
    this.calculatedAt = LocalDateTime.now();
  }
}
