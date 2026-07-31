package com.shipmentprofit.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.service.ProfitCalculationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfitController.class)
class ProfitControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProfitCalculationService profitCalculationService;

  @Test
  void returnsCalculatedProfitForValidRequest() throws Exception {
    CalculateProfitRequest request =
        new CalculateProfitRequest(
            "0001", new BigDecimal("1000.00"), new BigDecimal("200.00"), BigDecimal.ZERO);
    ProfitLossResponse response =
        new ProfitLossResponse(
            "0001",
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            new BigDecimal("800.00"),
            LocalDateTime.now());

    when(profitCalculationService.calculateProfit(any(CalculateProfitRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/profit-calculations")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipmentCode").value("0001"))
        .andExpect(jsonPath("$.profitOrLoss").value(800.00));
  }

  @Test
  void returnsBadRequestWhenShipmentCodeMissing() throws Exception {
    CalculateProfitRequest request =
        new CalculateProfitRequest(
            "", new BigDecimal("1000.00"), new BigDecimal("200.00"), BigDecimal.ZERO);

    mockMvc
        .perform(
            post("/api/profit-calculations")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
