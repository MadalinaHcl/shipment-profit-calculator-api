package com.shipmentprofit.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Result of the "Calculate Profit" use case, as displayed by the UI:
 * income, total costs and the resulting profit or loss.
 */
public record ProfitLossResponse(
        String shipmentCode,
        BigDecimal income,
        BigDecimal totalCosts,
        BigDecimal profitOrLoss,
        LocalDateTime calculatedAt
) {
}
