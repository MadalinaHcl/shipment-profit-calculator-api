package com.shipmentprofit.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.entity.CostType;
import com.shipmentprofit.api.entity.Shipment;
import com.shipmentprofit.api.repository.CostRepository;
import com.shipmentprofit.api.repository.IncomeRepository;
import com.shipmentprofit.api.repository.ShipmentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Recalculating the same shipment must be idempotent: no duplicate income/cost rows, and each
 * recalculation reflects only the latest submitted values - never the sum of everything ever
 * submitted.
 */
@SpringBootTest
class ProfitCalculationServiceIntegrationTest {

  @Autowired private ProfitCalculationService profitCalculationService;
  @Autowired private ShipmentRepository shipmentRepository;
  @Autowired private IncomeRepository incomeRepository;
  @Autowired private CostRepository costRepository;

  @Test
  void sameRequestTwiceProducesSameTotalsAndNoDuplicateRows() {
    CalculateProfitRequest request =
        new CalculateProfitRequest(
            "IT-SAME-TWICE", new BigDecimal("230.50"), new BigDecimal("200.00"), BigDecimal.ZERO);

    ProfitLossResponse first = profitCalculationService.calculateProfit(request);
    ProfitLossResponse second = profitCalculationService.calculateProfit(request);

    assertThat(second.income()).isEqualByComparingTo(first.income());
    assertThat(second.totalCosts()).isEqualByComparingTo(first.totalCosts());
    assertThat(second.profitOrLoss()).isEqualByComparingTo(first.profitOrLoss());
    assertThat(second.profitOrLoss()).isEqualByComparingTo("30.50");

    Shipment shipment = shipmentRepository.findByShipmentCode("IT-SAME-TWICE").orElseThrow();
    assertThat(incomeRepository.findByShipmentId(shipment.getId())).isPresent();
    assertThat(costRepository.findByShipmentId(shipment.getId()))
        .as("only the BASE cost should exist - no duplicate rows from the second call")
        .hasSize(1);
    assertThat(costRepository.findByShipmentIdAndCostType(shipment.getId(), CostType.BASE))
        .isPresent();
  }

  @Test
  void incomeAndBaseCostAreReplacedNotAccumulatedOnRecalculation() {
    CalculateProfitRequest firstRequest =
        new CalculateProfitRequest(
            "IT-INCOME-COST-REPLACE",
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            BigDecimal.ZERO);
    CalculateProfitRequest secondRequest =
        new CalculateProfitRequest(
            "IT-INCOME-COST-REPLACE",
            new BigDecimal("800.00"),
            new BigDecimal("300.00"),
            BigDecimal.ZERO);

    profitCalculationService.calculateProfit(firstRequest);
    ProfitLossResponse second = profitCalculationService.calculateProfit(secondRequest);

    assertThat(second.income())
        .as("800 must replace 1000, not add to it")
        .isEqualByComparingTo("800.00");
    assertThat(second.totalCosts())
        .as("300 must replace 200, not add to it")
        .isEqualByComparingTo("300.00");
    assertThat(second.profitOrLoss()).isEqualByComparingTo("500.00");

    Shipment shipment =
        shipmentRepository.findByShipmentCode("IT-INCOME-COST-REPLACE").orElseThrow();
    assertThat(incomeRepository.findByShipmentId(shipment.getId()))
        .get()
        .extracting(i -> i.getAmount())
        .isEqualTo(new BigDecimal("800.00"));
    assertThat(costRepository.findByShipmentId(shipment.getId()))
        .as("still exactly one row - the BASE cost was updated in place, not appended")
        .hasSize(1);
  }

  @Test
  void secondAdditionalCostReplacesTheFirstRatherThanAddingToIt() {
    CalculateProfitRequest fiftyAdditional =
        new CalculateProfitRequest(
            "IT-ADD-REPLACE",
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            new BigDecimal("50.00"));
    CalculateProfitRequest twentyAdditional =
        new CalculateProfitRequest(
            "IT-ADD-REPLACE",
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            new BigDecimal("20.00"));

    ProfitLossResponse first = profitCalculationService.calculateProfit(fiftyAdditional);
    ProfitLossResponse second = profitCalculationService.calculateProfit(twentyAdditional);

    assertThat(first.totalCosts()).isEqualByComparingTo("250.00");
    assertThat(second.totalCosts())
        .as("20 must replace 50, not add to it (200 + 20, not 200 + 50 + 20)")
        .isEqualByComparingTo("220.00");

    Shipment shipment = shipmentRepository.findByShipmentCode("IT-ADD-REPLACE").orElseThrow();
    assertThat(costRepository.findByShipmentId(shipment.getId()))
        .as("still exactly one BASE row and one ADDITIONAL row - the 50 was updated, not appended")
        .hasSize(2);
    assertThat(costRepository.findByShipmentIdAndCostType(shipment.getId(), CostType.ADDITIONAL))
        .get()
        .extracting(c -> c.getAmount())
        .isEqualTo(new BigDecimal("20.00"));
  }

  @Test
  void clearingAdditionalCostToZeroRemovesItFromTheTotal() {
    CalculateProfitRequest withAdditional =
        new CalculateProfitRequest(
            "IT-ADD-CLEAR",
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            new BigDecimal("50.00"));
    CalculateProfitRequest withoutAdditional =
        new CalculateProfitRequest(
            "IT-ADD-CLEAR", new BigDecimal("1000.00"), new BigDecimal("200.00"), BigDecimal.ZERO);

    ProfitLossResponse first = profitCalculationService.calculateProfit(withAdditional);
    ProfitLossResponse second = profitCalculationService.calculateProfit(withoutAdditional);

    assertThat(first.totalCosts()).isEqualByComparingTo("250.00");
    assertThat(second.totalCosts())
        .as("the old 50 must no longer be counted once additionalCost is 0")
        .isEqualByComparingTo("200.00");

    Shipment shipment = shipmentRepository.findByShipmentCode("IT-ADD-CLEAR").orElseThrow();
    assertThat(costRepository.findByShipmentIdAndCostType(shipment.getId(), CostType.ADDITIONAL))
        .as("the ADDITIONAL row must be deleted, not just zeroed out or left stale")
        .isEmpty();
    assertThat(costRepository.findByShipmentId(shipment.getId()))
        .as("only the BASE row should remain")
        .hasSize(1);
  }
}
