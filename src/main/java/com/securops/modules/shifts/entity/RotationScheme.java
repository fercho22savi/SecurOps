package com.securops.modules.shifts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ops_rotation_schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RotationPattern pattern;

    @Column(nullable = false)
    private Integer cycleDays; // E.g., 6 for 2x2x2, 7 for 6x1 or 5x2, 8 for 4x4

    @Column(nullable = false)
    private Integer requiredGuardsPerPost; // E.g., 3 guards for 2x2x2 to cover 24/7 post continuously

    @Column(length = 250)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
