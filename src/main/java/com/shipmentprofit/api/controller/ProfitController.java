package com.shipmentprofit.api.controller;

import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.service.ProfitCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profit-calculations")
public class ProfitController {

    private final ProfitCalculationService profitCalculationService;

    public ProfitController(ProfitCalculationService profitCalculationService) {
        this.profitCalculationService = profitCalculationService;
    }

    @PostMapping
    public ResponseEntity<ProfitLossResponse> calculateProfit(@Valid @RequestBody CalculateProfitRequest request) {
        return ResponseEntity.ok(profitCalculationService.calculateProfit(request));
    }
}
