package com.neocaps.api.model.entity;

import com.neocaps.api.enums.RobotActionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "robot_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "capsule_id")
    private Capsule capsule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RobotActionType actionType;

    private String fromPosition;
    private String toPosition;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 1000)
    private String message;
}
