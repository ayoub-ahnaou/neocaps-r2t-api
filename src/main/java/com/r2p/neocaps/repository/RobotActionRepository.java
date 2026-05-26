package com.r2p.neocaps.repository;

import com.r2p.neocaps.entity.RobotAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RobotActionRepository extends JpaRepository<RobotAction, UUID> {
    List<RobotAction> findByCapsuleIdOrderByTimestampDesc(UUID capsuleId);
}
