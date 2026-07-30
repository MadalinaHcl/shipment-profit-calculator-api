package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByShipmentId(Long shipmentId);
}
