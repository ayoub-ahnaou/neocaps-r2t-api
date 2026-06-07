package com.neocaps.api.model.entity;

import com.neocaps.api.enums.CapsuleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Capsule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String displayId;

    private Integer trayPosition;

    // private Integer rackNumber;
    // private Integer rackPosition;

    @Column(nullable = false)
    private Double doseMci;

    @Column(nullable = false)
    private String clientReference;

    @Column(nullable = false)
    private Double volumeMicroliter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CapsuleStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Lot lot;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.setCreatedAt(LocalDateTime.now());
        this.setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(LocalDateTime.now());
    }
}
