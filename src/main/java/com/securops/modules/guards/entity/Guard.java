package com.securops.modules.guards.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.securops.modules.security.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ops_guards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Guard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 25)
    private String nationalId; // Cédula de ciudadanía o identificación

    @Column(nullable = false, length = 60)
    private String firstName;

    @Column(nullable = false, length = 60)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(unique = true, length = 40)
    private String professionalLicense; // Tarjeta de control o credencial superintendencia

    @Column(nullable = false)
    @Builder.Default
    private boolean certifiedFirearms = false;

    private LocalDate firearmsLicenseExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private GuardStatus status = GuardStatus.ACTIVE;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal performanceScore = new BigDecimal("100.00"); // Scoring 0 - 100

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User userAccount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
