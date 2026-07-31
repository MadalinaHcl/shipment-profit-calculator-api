package com.shipmentprofit.api.repository;

import com.shipmentprofit.api.entity.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

  Optional<Shipment> findByShipmentCode(String shipmentCode);
}
