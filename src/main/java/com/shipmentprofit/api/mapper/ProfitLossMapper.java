package com.shipmentprofit.api.mapper;

import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.entity.ProfitLoss;

/**
 * Maps between the persistence layer ({@link ProfitLoss}) and the API layer ({@link
 * ProfitLossResponse}), keeping the two representations decoupled.
 */
public interface ProfitLossMapper {

  ProfitLossResponse toResponse(ProfitLoss profitLoss);
}
