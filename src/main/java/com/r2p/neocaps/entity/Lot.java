package com.r2p.neocaps.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "capsules")
@ToString(exclude = "capsules")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String supplierLotNumber;

    private Double totalActivityMci;
    
    private Double radioactiveConcentration;
    
    private Double reservoirVolumeMicroliter;

    private LocalDateTime manufacturingDate;

    private LocalDateTime calibrationDate;

    @JsonManagedReference
    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Capsule> capsules = new ArrayList<>();
}
