package com.neocaps.api.repository;

import com.neocaps.api.model.entity.Capsule;
import com.neocaps.api.model.entity.RobotAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RobotActionRepository extends JpaRepository<RobotAction, UUID> {
    List<RobotAction> findByCapsule(Capsule capsule);
    List<RobotAction> findAllByOrderByTimestampDesc();
}
