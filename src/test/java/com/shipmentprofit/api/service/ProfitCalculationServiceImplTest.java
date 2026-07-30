package com.shipmentprofit.api.service;

import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.entity.Cost;
import com.shipmentprofit.api.entity.CostType;
import com.shipmentprofit.api.entity.Income;
import com.shipmentprofit.api.entity.ProfitLoss;
import com.shipmentprofit.api.entity.Shipment;
import com.shipmentprofit.api.repository.CostRepository;
import com.shipmentprofit.api.repository.IncomeRepository;
import com.shipmentprofit.api.repository.ProfitLossRepository;
import com.shipmentprofit.api.repository.ShipmentRepository;
import com.shipmentprofit.api.service.impl.ProfitCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitCalculationServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private CostRepository costRepository;
    @Mock
    private ProfitLossRepository profitLossRepository;

    @InjectMocks
    private ProfitCalculationServiceImpl profitCalculationService;

    private Shipment shipment;

    @BeforeEach
    void setUp() {
        shipment = new Shipment("0001");
    }

    @Test
    void calculatesProfitWhenIncomeExceedsCosts() {
        CalculateProfitRequest request = new CalculateProfitRequest("0001", new BigDecimal("1000.00"),
                new BigDecimal("200.00"), BigDecimal.ZERO);

        when(shipmentRepository.findByShipmentCode("0001")).thenReturn(Optional.of(shipment));
        when(incomeRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.empty());
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(costRepository.findByShipmentId(shipment.getId())).thenReturn(List.of());
        when(costRepository.save(any(Cost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profitLossRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.empty());
        when(profitLossRepository.save(any(ProfitLoss.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfitLossResponse response = profitCalculationService.calculateProfit(request);

        assertThat(response.income()).isEqualByComparingTo("1000.00");
        assertThat(response.totalCosts()).isEqualByComparingTo("200.00");
        assertThat(response.profitOrLoss()).isEqualByComparingTo("800.00");
        verify(incomeRepository).save(any(Income.class));
        verify(costRepository).save(any(Cost.class));
    }

    @Test
    void calculatesLossWhenCostsExceedIncome() {
        CalculateProfitRequest request = new CalculateProfitRequest("0002", new BigDecimal("500.00"),
                new BigDecimal("900.00"), BigDecimal.ZERO);
        Shipment shipment2 = new Shipment("0002");

        when(shipmentRepository.findByShipmentCode("0002")).thenReturn(Optional.of(shipment2));
        when(incomeRepository.findByShipmentId(shipment2.getId())).thenReturn(Optional.empty());
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(costRepository.findByShipmentId(shipment2.getId())).thenReturn(List.of());
        when(costRepository.save(any(Cost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profitLossRepository.findByShipmentId(shipment2.getId())).thenReturn(Optional.empty());
        when(profitLossRepository.save(any(ProfitLoss.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfitLossResponse response = profitCalculationService.calculateProfit(request);

        assertThat(response.profitOrLoss()).isEqualByComparingTo("-400.00");
    }

    @Test
    void createsNewShipmentWhenShipmentCodeNotFound() {
        CalculateProfitRequest request = new CalculateProfitRequest("9999", new BigDecimal("100.00"),
                new BigDecimal("50.00"), BigDecimal.ZERO);
        Shipment newShipment = new Shipment("9999");

        when(shipmentRepository.findByShipmentCode("9999")).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(newShipment);
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(costRepository.save(any(Cost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profitLossRepository.save(any(ProfitLoss.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profitCalculationService.calculateProfit(request);

        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void recalculatingUpdatesExistingIncomeAndCostInsteadOfInsertingNewOnes() {
        CalculateProfitRequest request = new CalculateProfitRequest("0001", new BigDecimal("230.50"),
                new BigDecimal("200.00"), BigDecimal.ZERO);
        Income existingIncome = new Income(shipment, new BigDecimal("1000.00"), "Customer payment");
        Cost existingBaseCost = new Cost(shipment, CostType.BASE, new BigDecimal("200.00"), "Service provision cost");

        when(shipmentRepository.findByShipmentCode("0001")).thenReturn(Optional.of(shipment));
        when(incomeRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.of(existingIncome));
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(costRepository.findByShipmentId(shipment.getId())).thenReturn(List.of(existingBaseCost));
        when(costRepository.save(any(Cost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profitLossRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.empty());
        when(profitLossRepository.save(any(ProfitLoss.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfitLossResponse response = profitCalculationService.calculateProfit(request);

        // the same Income/Cost instances were updated and saved, never a fresh "new Income(...)"/"new Cost(...)"
        assertThat(existingIncome.getAmount()).isEqualByComparingTo("230.50");
        assertThat(existingBaseCost.getAmount()).isEqualByComparingTo("200.00");
        assertThat(response.income()).isEqualByComparingTo("230.50");
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void clearingAdditionalCostDeletesThePreviouslyStoredRow() {
        CalculateProfitRequest request = new CalculateProfitRequest("0001", new BigDecimal("1000.00"),
                new BigDecimal("200.00"), BigDecimal.ZERO);
        Income existingIncome = new Income(shipment, new BigDecimal("1000.00"), "Customer payment");
        Cost existingBaseCost = new Cost(shipment, CostType.BASE, new BigDecimal("200.00"), "Service provision cost");
        Cost existingAdditionalCost = new Cost(shipment, CostType.ADDITIONAL, new BigDecimal("50.00"), "Additional cost");

        when(shipmentRepository.findByShipmentCode("0001")).thenReturn(Optional.of(shipment));
        when(incomeRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.of(existingIncome));
        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(costRepository.findByShipmentId(shipment.getId())).thenReturn(List.of(existingBaseCost, existingAdditionalCost));
        when(costRepository.save(any(Cost.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profitLossRepository.findByShipmentId(shipment.getId())).thenReturn(Optional.empty());
        when(profitLossRepository.save(any(ProfitLoss.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfitLossResponse response = profitCalculationService.calculateProfit(request);

        verify(costRepository).delete(existingAdditionalCost);
        assertThat(response.totalCosts()).isEqualByComparingTo("200.00");
    }
}
