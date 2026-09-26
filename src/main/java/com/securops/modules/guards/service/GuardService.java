package com.securops.modules.guards.service;

import com.securops.modules.guards.dto.GuardDtos.*;
import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.entity.GuardStatus;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.security.entity.RoleType;
import com.securops.modules.security.entity.User;
import com.securops.modules.security.repository.UserRepository;
import com.securops.modules.shifts.entity.ShiftSchedule;
import com.securops.modules.shifts.entity.ShiftScheduleStatus;
import com.securops.modules.shifts.repository.ShiftScheduleRepository;
import com.securops.modules.sync.service.MultisiteSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardService {

    private final GuardRepository guardRepository;
    private final UserRepository userRepository;
    private final ShiftScheduleRepository scheduleRepository;
    private final MultisiteSyncService syncService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GuardResponseDto createGuard(CreateGuardRequestDto dto) {
        if (guardRepository.findByNationalId(dto.getNationalId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un guarda registrado con la identificación: " + dto.getNationalId());
        }

        User user = null;
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso");
            }

            user = userRepository.save(User.builder()
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "guarda123"))
                    .fullName(dto.getFirstName() + " " + dto.getLastName())
                    .email(dto.getEmail() != null ? dto.getEmail() : dto.getUsername() + "@securops.com")
                    .roles(Set.of(RoleType.ROLE_GUARD))
                    .enabled(true)
                    .build());
        }

        Guard guard = Guard.builder()
                .nationalId(dto.getNationalId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .professionalLicense(dto.getProfessionalLicense())
                .certifiedFirearms(dto.isCertifiedFirearms())
                .firearmsLicenseExpiry(dto.getFirearmsLicenseExpiry())
                .status(GuardStatus.ACTIVE)
                .performanceScore(new BigDecimal("100.00"))
                .userAccount(user)
                .createdAt(LocalDateTime.now())
                .build();

        Guard saved = guardRepository.save(guard);

        // Stage for multisite synchronization
        syncService.stageEventForSync("GUARD", saved.getId().toString(), "CREATED", saved);

        log.info("Nuevo guarda registrado exitosamente: ID={}, Nombre='{}', Cédula={}",
                saved.getId(), saved.getFullName(), saved.getNationalId());

        return mapToDto(saved);
    }

    /**
     * Soft delete / Inactivación de guarda preservando todo el historial previo
     * (turnos trabajados, asistencias, nóminas liquidadas).
     * Los turnos futuros programados se desasignan o se marcan como UNCOVERED.
     */
    @Transactional
    public GuardResponseDto deactivateGuard(Long guardId, String reason) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new IllegalArgumentException("Guarda no encontrado con ID: " + guardId));

        if (guard.getStatus() == GuardStatus.INACTIVE) {
            throw new IllegalStateException("El guarda ya se encuentra inactivo / retirado.");
        }

        guard.setStatus(GuardStatus.INACTIVE);
        guard.setDeactivationReason(reason != null && !reason.trim().isEmpty() ? reason : "Retiro / Desvinculación de la empresa");
        guard.setDeactivationDate(LocalDate.now());

        // Inhabilitar cuenta de acceso
        if (guard.getUserAccount() != null) {
            guard.getUserAccount().setEnabled(false);
            userRepository.save(guard.getUserAccount());
        }

        // Marcar turnos futuros como UNCOVERED para requerir relevo inmediato
        LocalDate today = LocalDate.now();
        LocalDate futureLimit = today.plusMonths(3);
        List<ShiftSchedule> futureSchedules = scheduleRepository.findByGuardIdAndShiftDateBetween(guardId, today, futureLimit);

        for (ShiftSchedule s : futureSchedules) {
            if (s.getStatus() == ShiftScheduleStatus.SCHEDULED || s.getStatus() == ShiftScheduleStatus.CONFIRMED) {
                s.setStatus(ShiftScheduleStatus.UNCOVERED);
                s.setReplacementReason("RETIRO DE PERSONAL: " + guard.getDeactivationReason());
                scheduleRepository.save(s);
                log.warn("Turno futuro {} del puesto '{}' desasignado por retiro del guarda {}",
                        s.getId(), s.getSecurityPost().getName(), guard.getFullName());
            }
        }

        Guard saved = guardRepository.save(guard);
        syncService.stageEventForSync("GUARD", saved.getId().toString(), "DEACTIVATED", saved);

        log.info("Guarda retirado con historial preservado: ID={}, Nombre='{}', Motivo='{}'",
                saved.getId(), saved.getFullName(), saved.getDeactivationReason());

        return mapToDto(saved);
    }

    @Transactional
    public GuardResponseDto reactivateGuard(Long guardId) {
        Guard guard = guardRepository.findById(guardId)
                .orElseThrow(() -> new IllegalArgumentException("Guarda no encontrado con ID: " + guardId));

        guard.setStatus(GuardStatus.ACTIVE);
        guard.setDeactivationReason(null);
        guard.setDeactivationDate(null);

        if (guard.getUserAccount() != null) {
            guard.getUserAccount().setEnabled(true);
            userRepository.save(guard.getUserAccount());
        }

        Guard saved = guardRepository.save(guard);
        syncService.stageEventForSync("GUARD", saved.getId().toString(), "REACTIVATED", saved);

        log.info("Guarda reactivado para asignación operativa: ID={}, Nombre='{}'",
                saved.getId(), saved.getFullName());

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<GuardResponseDto> getAvailableGuards() {
        return guardRepository.findByStatus(GuardStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GuardResponseDto> getAllGuards() {
        return guardRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private GuardResponseDto mapToDto(Guard g) {
        return GuardResponseDto.builder()
                .id(g.getId())
                .nationalId(g.getNationalId())
                .fullName(g.getFullName())
                .firstName(g.getFirstName())
                .lastName(g.getLastName())
                .phone(g.getPhone())
                .email(g.getEmail())
                .professionalLicense(g.getProfessionalLicense())
                .certifiedFirearms(g.isCertifiedFirearms())
                .firearmsLicenseExpiry(g.getFirearmsLicenseExpiry())
                .status(g.getStatus())
                .performanceScore(g.getPerformanceScore())
                .deactivationReason(g.getDeactivationReason())
                .deactivationDate(g.getDeactivationDate())
                .createdAt(g.getCreatedAt())
                .availableForShifts(g.getStatus() == GuardStatus.ACTIVE)
                .build();
    }
}
