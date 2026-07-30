package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.ProfitLoss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfitLossRepository extends JpaRepository<ProfitLoss, Long> {

    Optional<ProfitLoss> findByShipmentId(Long shipmentId);
}
