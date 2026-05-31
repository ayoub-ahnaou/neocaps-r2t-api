package com.neocaps.api.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String supplierLotNumber;

    @Column(nullable = false)
    private Double totalActivityMci;

    @Column(nullable = false)
    private Double radioactiveConcentration; // mCi/µL

    @Column(nullable = false)
    private Double reservoirVolumeMicroliter;

    @Column(nullable = false)
    private LocalDateTime manufacturingDate;

    @Column(nullable = false)
    private LocalDateTime calibrationDate;
}
