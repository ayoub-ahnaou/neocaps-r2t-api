package com.neocaps.api.repository;

import com.neocaps.api.model.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, UUID> {
    Optional<Lot> findBySupplierLotNumber(String supplierLotNumber);
    boolean existsBySupplierLotNumber(String supplierLotNumber);
}
