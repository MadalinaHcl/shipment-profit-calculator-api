package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Cost;
import com.shipmentprofit.api.entity.CostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CostRepository extends JpaRepository<Cost, Long> {

    Optional<Cost> findByShipmentIdAndCostType(Long shipmentId, CostType costType);

    List<Cost> findByShipmentId(Long shipmentId);
}
