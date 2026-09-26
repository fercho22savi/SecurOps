package com.securops.modules.shifts.engine;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.shifts.entity.*;
import com.securops.modules.shifts.repository.ShiftDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftMeshGeneratorEngine {

    private final List<RotationStrategy> strategies;
    private final ShiftDefinitionRepository shiftDefinitionRepository;

    /**
     * Resolves the strategy for the given pattern.
     */
    public RotationStrategy resolveStrategy(RotationPattern pattern) {
        return strategies.stream()
                .filter(s -> s.getSupportedPattern() == pattern)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy implemented for rotation pattern: " + pattern));
    }

    /**
     * Generates a complete monthly shift schedule mesh for a security post and guard squad.
     */
    public ShiftMeshGenerationResult generateMonthlyMesh(ShiftMeshGenerationRequest request) {
        RotationPattern pattern = request.getRotationScheme().getPattern();
        RotationStrategy strategy = resolveStrategy(pattern);

        SecurityPost post = request.getSecurityPost();
        ServiceCoverageType coverageType = post.getCoverageType();
        List<Guard> guards = request.getAssignedGuards();
        int requiredGuards = strategy.getRequiredGuards(coverageType);

        List<String> alerts = new ArrayList<>();
        if (guards.size() < requiredGuards) {
            String alert = String.format(
                "ADVERTENCIA DE COBERTURA: El puesto '%s' requiere %d guardas para el esquema %s, pero solo hay %d asignados.",
                post.getName(), requiredGuards, pattern, guards.size()
            );
            alerts.add(alert);
            log.warn(alert);
        }

        LocalDate startDate = request.getTargetMonth().atDay(1);
        LocalDate endDate = request.getTargetMonth().atEndOfMonth();
        LocalDate anchorDate = request.getCycleAnchorDate() != null ? request.getCycleAnchorDate() : startDate;

        int cycleLength = strategy.getCycleLengthDays();
        List<ShiftSchedule> generatedSchedules = new ArrayList<>();
        int workingShiftsCount = 0;
        int restDaysCount = 0;
        int uncoveredCount = 0;

        // Cache shift definitions
        Map<ShiftType, ShiftDefinition> definitionCache = getShiftDefinitionCache();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            long daysFromAnchor = ChronoUnit.DAYS.between(anchorDate, currentDate);
            int dayOfCycle = (int) ((daysFromAnchor % cycleLength + cycleLength) % cycleLength);

            Set<ShiftType> coveredShiftsToday = new HashSet<>();

            // Assign shifts to squad members
            for (int guardIndex = 0; guardIndex < guards.size(); guardIndex++) {
                Guard guard = guards.get(guardIndex);
                ShiftType assignedType = strategy.calculateShiftForDay(dayOfCycle, guardIndex, coverageType);
                ShiftDefinition definition = definitionCache.get(assignedType);

                if (assignedType == ShiftType.OFF_DUTY) {
                    restDaysCount++;
                } else {
                    workingShiftsCount++;
                    coveredShiftsToday.add(assignedType);
                }

                ShiftSchedule schedule = ShiftSchedule.builder()
                        .securityPost(post)
                        .guard(guard)
                        .shiftDefinition(definition)
                        .shiftDate(currentDate)
                        .status(ShiftScheduleStatus.SCHEDULED)
                        .operationalNotes("Generado automáticamente por Motor SecurOps (" + pattern + ")")
                        .build();

                generatedSchedules.add(schedule);
            }

            // Coverage validation: check if required 24/7 or continuous shifts are covered
            if (coverageType == ServiceCoverageType.CONTINUOUS_24_7) {
                if (!coveredShiftsToday.contains(ShiftType.DAY_12H)) {
                    uncoveredCount++;
                    alerts.add(String.format("PUESTO DESCUBIERTO: '%s' sin guarda Diurno 12h el %s", post.getName(), currentDate));
                }
                if (!coveredShiftsToday.contains(ShiftType.NIGHT_12H)) {
                    uncoveredCount++;
                    alerts.add(String.format("PUESTO DESCUBIERTO: '%s' sin guarda Nocturno 12h el %s", post.getName(), currentDate));
                }
            } else if (coverageType == ServiceCoverageType.DAYTIME_12H && !coveredShiftsToday.contains(ShiftType.DAY_12H)) {
                uncoveredCount++;
                alerts.add(String.format("PUESTO DESCUBIERTO: '%s' sin guarda Diurno 12h el %s", post.getName(), currentDate));
            }

            currentDate = currentDate.plusDays(1);
        }

        return ShiftMeshGenerationResult.builder()
                .totalSchedulesGenerated(generatedSchedules.size())
                .totalWorkingShifts(workingShiftsCount)
                .totalRestDays(restDaysCount)
                .uncoveredSlotsCount(uncoveredCount)
                .schedules(generatedSchedules)
                .operationalAlerts(alerts)
                .build();
    }

    private Map<ShiftType, ShiftDefinition> getShiftDefinitionCache() {
        Map<ShiftType, ShiftDefinition> map = new EnumMap<>(ShiftType.class);
        for (ShiftType type : ShiftType.values()) {
            ShiftDefinition def = shiftDefinitionRepository.findByType(type)
                    .orElseGet(() -> ShiftDefinition.builder()
                            .code(type.name())
                            .name(type.name())
                            .type(type)
                            .durationHours(type == ShiftType.OFF_DUTY ? 0 : (type.name().contains("12H") ? 12 : 8))
                            .startTime(java.time.LocalTime.of(6, 0))
                            .endTime(java.time.LocalTime.of(18, 0))
                            .isNightShift(type.name().contains("NIGHT"))
                            .build()
                    );
            map.put(type, def);
        }
        return map;
    }
}
