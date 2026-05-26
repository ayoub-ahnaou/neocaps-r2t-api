package com.r2p.neocaps.repository;

import com.r2p.neocaps.entity.Capsule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    long countByLotId(UUID lotId);
    boolean existsByBarcode(String barcode);
    boolean existsByTrayPosition(Integer trayPosition);
}
