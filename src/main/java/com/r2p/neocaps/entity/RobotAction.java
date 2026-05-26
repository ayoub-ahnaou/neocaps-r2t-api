package com.r2p.neocaps.entity;

import com.r2p.neocaps.entity.enums.RobotActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "robot_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id", nullable = false)
    private Capsule capsule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RobotActionType actionType;

    private String fromPosition;

    private String toPosition;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private boolean success;

    @Column(length = 1000)
    private String message;
}
