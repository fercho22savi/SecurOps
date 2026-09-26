package com.securops.modules.shifts.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "ops_shift_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String code; // D12, N12, M8, T8, N8, OFF

    @Column(nullable = false, length = 50)
    private String name; // Diurno 12h, Nocturno 12h, Descanso

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftType type;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer durationHours;

    @Column(nullable = false)
    @Builder.Default
    private boolean isNightShift = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
