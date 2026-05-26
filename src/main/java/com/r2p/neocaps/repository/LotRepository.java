package com.r2p.neocaps.repository;

import com.r2p.neocaps.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LotRepository extends JpaRepository<Lot, UUID> {
}
