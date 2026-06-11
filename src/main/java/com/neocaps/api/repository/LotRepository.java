package com.neocaps.api.repository;

import com.neocaps.api.model.entity.Lot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, UUID> {
    Optional<Lot> findBySupplierLotNumber(String supplierLotNumber);
    boolean existsBySupplierLotNumber(String supplierLotNumber);
    boolean existsByProductName(String productName);

    @Query("SELECT DISTINCT l FROM Lot l LEFT JOIN FETCH l.capsules WHERE l.id = :id")
    Optional<Lot> findByIdWithCapsules(@Param("id") Long id);

    @Query("SELECT DISTINCT l FROM Lot l " +
            "LEFT JOIN FETCH l.capsules " +
            "WHERE l.id = :id")
    Optional<Lot> findByIdWithAllDetails(@Param("id") Long id);
}
