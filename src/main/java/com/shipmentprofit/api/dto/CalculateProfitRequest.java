package com.shipmentprofit.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Input for the "Calculate Profit" use case: a shipment's income and the
 * costs incurred providing the service (base cost + optional additional cost).
 * Numeric precision and string length limits mirror the DECIMAL(12,2) and
 * VARCHAR(50) columns in schema.sql, so invalid input is rejected with a
 * 400 here rather than failing at the database.
 */
public record CalculateProfitRequest(
        @NotBlank(message = "shipmentCode is required")
        @Size(max = 50, message = "shipmentCode must not exceed 50 characters")
        String shipmentCode,

        @NotNull(message = "income is required")
        @PositiveOrZero(message = "income must not be negative")
        @Digits(integer = 10, fraction = 2, message = "income must have at most 10 integer digits and 2 decimal places")
        BigDecimal income,

        @NotNull(message = "cost is required")
        @PositiveOrZero(message = "cost must not be negative")
        @Digits(integer = 10, fraction = 2, message = "cost must have at most 10 integer digits and 2 decimal places")
        BigDecimal cost,

        @PositiveOrZero(message = "additionalCost must not be negative")
        @Digits(integer = 10, fraction = 2, message = "additionalCost must have at most 10 integer digits and 2 decimal places")
        BigDecimal additionalCost
) {
    public CalculateProfitRequest {
        if (shipmentCode != null) {
            shipmentCode = shipmentCode.trim();
        }
        if (additionalCost == null) {
            additionalCost = BigDecimal.ZERO;
        }
    }
}
