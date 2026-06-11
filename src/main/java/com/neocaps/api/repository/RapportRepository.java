package com.neocaps.api.repository;

import com.neocaps.api.model.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RapportRepository extends JpaRepository<Rapport, UUID> {
}
