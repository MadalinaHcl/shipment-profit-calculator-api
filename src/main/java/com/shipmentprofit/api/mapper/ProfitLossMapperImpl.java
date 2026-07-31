package com.shipmentprofit.api.mapper;

import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.entity.ProfitLoss;
import org.springframework.stereotype.Component;

@Component
public class ProfitLossMapperImpl implements ProfitLossMapper {

  @Override
  public ProfitLossResponse toResponse(ProfitLoss profitLoss) {
    return new ProfitLossResponse(
        profitLoss.getShipment().getShipmentCode(),
        profitLoss.getTotalIncome(),
        profitLoss.getTotalCosts(),
        profitLoss.getProfitOrLoss(),
        profitLoss.getCalculatedAt());
  }
}
