package com.r2p.neocaps.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.r2p.neocaps.entity.enums.CapsuleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "lot")
@ToString(exclude = "lot")
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String displayId;

    private Integer trayPosition;

    private Integer rackNumber;

    private Integer rackPosition;

    private Double doseMci;

    private Double volumeMicroliter;

    @Column(unique = true)
    private String barcode;

    @Enumerated(EnumType.STRING)
    private CapsuleStatus status;

    private LocalDateTime calibrationDate;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;
}
