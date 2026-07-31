package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.ProfitLoss;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfitLossRepository extends JpaRepository<ProfitLoss, Long> {

  Optional<ProfitLoss> findByShipmentId(Long shipmentId);
}
