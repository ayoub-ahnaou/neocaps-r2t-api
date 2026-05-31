package com.neocaps.api.repository;

import com.neocaps.api.enums.CapsuleStatus;
import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    Optional<Capsule> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    boolean existsByDisplayId(String displayId);
    long countByLot(Lot lot);
    
    // Checks if a tray position is occupied by any capsule that is not stored or in error status
    boolean existsByTrayPositionAndStatusNotIn(Integer trayPosition, Collection<CapsuleStatus> statuses);
    
    // Checks if a rack position is occupied by any capsule
    boolean existsByRackNumberAndRackPosition(Integer rackNumber, Integer rackPosition);
    
    List<Capsule> findByLot(Lot lot);
}
