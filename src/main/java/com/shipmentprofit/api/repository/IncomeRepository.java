package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Income;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeRepository extends JpaRepository<Income, Long> {

  Optional<Income> findByShipmentId(Long shipmentId);
}
