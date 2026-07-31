package com.shipmentprofit.api.service.impl;

import com.shipmentprofit.api.dto.CalculateProfitRequest;
import com.shipmentprofit.api.dto.ProfitLossResponse;
import com.shipmentprofit.api.entity.Cost;
import com.shipmentprofit.api.entity.CostType;
import com.shipmentprofit.api.entity.Income;
import com.shipmentprofit.api.entity.ProfitLoss;
import com.shipmentprofit.api.entity.Shipment;
import com.shipmentprofit.api.mapper.ProfitLossMapper;
import com.shipmentprofit.api.repository.CostRepository;
import com.shipmentprofit.api.repository.IncomeRepository;
import com.shipmentprofit.api.repository.ProfitLossRepository;
import com.shipmentprofit.api.repository.ShipmentRepository;
import com.shipmentprofit.api.service.ProfitCalculationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recalculating a shipment does not append duplicate financial records. A shipment has at most one
 * income record, one BASE cost and one ADDITIONAL cost. Existing values are updated in place, so
 * repeated submissions with the same financial input produce the same totals.
 */
@Service
public class ProfitCalculationServiceImpl implements ProfitCalculationService {

  private final ShipmentRepository shipmentRepository;
  private final IncomeRepository incomeRepository;
  private final CostRepository costRepository;
  private final ProfitLossRepository profitLossRepository;
  private final ProfitLossMapper profitLossMapper;

  public ProfitCalculationServiceImpl(
      ShipmentRepository shipmentRepository,
      IncomeRepository incomeRepository,
      CostRepository costRepository,
      ProfitLossRepository profitLossRepository,
      ProfitLossMapper profitLossMapper) {
    this.shipmentRepository = shipmentRepository;
    this.incomeRepository = incomeRepository;
    this.costRepository = costRepository;
    this.profitLossRepository = profitLossRepository;
    this.profitLossMapper = profitLossMapper;
  }

  @Override
  @Transactional
  public ProfitLossResponse calculateProfit(CalculateProfitRequest request) {
    ShipmentResolution resolution = resolveShipment(request.shipmentCode());
    Shipment shipment = resolution.shipment();

    Optional<Income> existingIncome =
        resolution.isNew() ? Optional.empty() : incomeRepository.findByShipmentId(shipment.getId());
    List<Cost> existingCosts =
        resolution.isNew() ? List.of() : costRepository.findByShipmentId(shipment.getId());
    Optional<Cost> existingBaseCost = findByType(existingCosts, CostType.BASE);
    Optional<Cost> existingAdditionalCost = findByType(existingCosts, CostType.ADDITIONAL);

    Income income = saveOrUpdateIncome(shipment, request.income(), existingIncome);
    Cost baseCost = saveOrUpdateBaseCost(shipment, request.cost(), existingBaseCost);
    BigDecimal additionalCost =
        synchronizeAdditionalCost(shipment, request.additionalCost(), existingAdditionalCost);

    BigDecimal totalIncome = income.getAmount();
    BigDecimal totalCosts = baseCost.getAmount().add(additionalCost);
    BigDecimal profitOrLoss = totalIncome.subtract(totalCosts);

    Optional<ProfitLoss> existingResult =
        resolution.isNew()
            ? Optional.empty()
            : profitLossRepository.findByShipmentId(shipment.getId());
    ProfitLoss result =
        saveOrUpdateResult(shipment, totalIncome, totalCosts, profitOrLoss, existingResult);

    return profitLossMapper.toResponse(result);
  }

  private ShipmentResolution resolveShipment(String shipmentCode) {
    Optional<Shipment> existing = shipmentRepository.findByShipmentCode(shipmentCode);
    if (existing.isPresent()) {
      return new ShipmentResolution(existing.get(), false);
    }
    return new ShipmentResolution(shipmentRepository.save(new Shipment(shipmentCode)), true);
  }

  private Income saveOrUpdateIncome(
      Shipment shipment, BigDecimal amount, Optional<Income> existing) {
    Income income =
        findOrCreate(
            existing,
            current -> current.updateAmount(amount),
            () -> new Income(shipment, amount, "Customer payment"));
    return incomeRepository.save(income);
  }

  private Cost saveOrUpdateBaseCost(Shipment shipment, BigDecimal amount, Optional<Cost> existing) {
    Cost baseCost =
        findOrCreate(
            existing,
            current -> current.updateAmount(amount),
            () -> new Cost(shipment, CostType.BASE, amount, "Service provision cost"));
    return costRepository.save(baseCost);
  }

  /**
   * Updates the shipment's ADDITIONAL cost to match the request, deleting it when the request no
   * longer carries one. Returns the amount to add to the total (zero when there is no ADDITIONAL
   * cost).
   */
  private BigDecimal synchronizeAdditionalCost(
      Shipment shipment, BigDecimal amount, Optional<Cost> existing) {
    if (amount.signum() == 0) {
      existing.ifPresent(costRepository::delete);
      return BigDecimal.ZERO;
    }

    Cost additionalCost =
        findOrCreate(
            existing,
            current -> current.updateAmount(amount),
            () -> new Cost(shipment, CostType.ADDITIONAL, amount, "Additional cost"));
    return costRepository.save(additionalCost).getAmount();
  }

  private ProfitLoss saveOrUpdateResult(
      Shipment shipment,
      BigDecimal totalIncome,
      BigDecimal totalCosts,
      BigDecimal profitOrLoss,
      Optional<ProfitLoss> existing) {
    ProfitLoss profitLoss =
        findOrCreate(
            existing,
            current -> current.update(totalIncome, totalCosts, profitOrLoss),
            () -> new ProfitLoss(shipment, totalIncome, totalCosts, profitOrLoss));
    return profitLossRepository.save(profitLoss);
  }

  private static Optional<Cost> findByType(List<Cost> costs, CostType costType) {
    return costs.stream().filter(cost -> cost.getCostType() == costType).findFirst();
  }

  private static <T> T findOrCreate(Optional<T> existing, Consumer<T> update, Supplier<T> create) {
    return existing
        .map(
            current -> {
              update.accept(current);
              return current;
            })
        .orElseGet(create);
  }

  private record ShipmentResolution(Shipment shipment, boolean isNew) {}
}
