package com.shipmentprofit.api.service;

import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;

/**
 * Business operations for the "Calculate Profit" use case (FR1-FR3):
 * records income and costs for a shipment, then computes and persists
 * the resulting profit or loss.
 */
public interface ProfitCalculationService {

    ProfitLossResponse calculateProfit(CalculateProfitRequest request);
}
