package com.neocaps.api.model.entity;

import com.neocaps.api.enums.CapsuleStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String displayId;

    @Column(nullable = false)
    private Integer trayPosition;

    @Column(nullable = false)
    private Integer rackNumber;

    @Column(nullable = false)
    private Integer rackPosition;

    @Column(nullable = false)
    private Double doseMci;

    @Column(nullable = false)
    private Double volumeMicroliter;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(nullable = false)
    private LocalDateTime calibrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapsuleStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;
}
