package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Cost;
import com.shipmentprofit.api.entity.CostType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostRepository extends JpaRepository<Cost, Long> {

  Optional<Cost> findByShipmentIdAndCostType(Long shipmentId, CostType costType);

  List<Cost> findByShipmentId(Long shipmentId);
}
