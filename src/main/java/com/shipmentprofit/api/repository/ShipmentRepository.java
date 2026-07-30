package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByShipmentCode(String shipmentCode);
}
