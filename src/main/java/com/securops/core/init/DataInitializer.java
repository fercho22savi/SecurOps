package com.securops.core.init;

import com.securops.modules.guards.entity.Guard;
import com.securops.modules.guards.entity.GuardStatus;
import com.securops.modules.guards.repository.GuardRepository;
import com.securops.modules.payroll.entity.PayrollPeriod;
import com.securops.modules.payroll.repository.PayrollPeriodRepository;
import com.securops.modules.posts.entity.SecurityPost;
import com.securops.modules.posts.entity.ServiceCoverageType;
import com.securops.modules.posts.entity.Site;
import com.securops.modules.posts.repository.SecurityPostRepository;
import com.securops.modules.posts.repository.SiteRepository;
import com.securops.modules.security.entity.RoleType;
import com.securops.modules.security.entity.User;
import com.securops.modules.security.repository.UserRepository;
import com.securops.modules.shifts.entity.RotationPattern;
import com.securops.modules.shifts.entity.RotationScheme;
import com.securops.modules.shifts.entity.ShiftDefinition;
import com.securops.modules.shifts.entity.ShiftType;
import com.securops.modules.shifts.repository.RotationSchemeRepository;
import com.securops.modules.shifts.repository.ShiftDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ShiftDefinitionRepository shiftDefinitionRepository;
    private final RotationSchemeRepository rotationSchemeRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final SecurityPostRepository securityPostRepository;
    private final GuardRepository guardRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (shiftDefinitionRepository.count() > 0) {
            log.info("Master data already initialized. Skipping seeder.");
            return;
        }

        log.info("⚡ Initializing SecurOps Master and Demo Data...");

        // 1. Shift Definitions
        ShiftDefinition d12 = shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("D12")
                .name("Diurno 12 Horas")
                .type(ShiftType.DAY_12H)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(18, 0))
                .durationHours(12)
                .isNightShift(false)
                .build());

        ShiftDefinition n12 = shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("N12")
                .name("Nocturno 12 Horas")
                .type(ShiftType.NIGHT_12H)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(6, 0))
                .durationHours(12)
                .isNightShift(true)
                .build());

        shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("M8")
                .name("Mañana 8 Horas")
                .type(ShiftType.MORNING_8H)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(14, 0))
                .durationHours(8)
                .isNightShift(false)
                .build());

        shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("T8")
                .name("Tarde 8 Horas")
                .type(ShiftType.AFTERNOON_8H)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(22, 0))
                .durationHours(8)
                .isNightShift(false)
                .build());

        shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("N8")
                .name("Noche 8 Horas")
                .type(ShiftType.NIGHT_8H)
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(6, 0))
                .durationHours(8)
                .isNightShift(true)
                .build());

        ShiftDefinition off = shiftDefinitionRepository.save(ShiftDefinition.builder()
                .code("OFF")
                .name("Descanso / Libre")
                .type(ShiftType.OFF_DUTY)
                .startTime(LocalTime.MIDNIGHT)
                .endTime(LocalTime.MIDNIGHT)
                .durationHours(0)
                .isNightShift(false)
                .build());

        log.info("✓ 6 Shift definitions registered.");

        // 2. Rotation Schemes
        rotationSchemeRepository.save(RotationScheme.builder()
                .name("Rotación 2x2x2 (12 Horas)")
                .pattern(RotationPattern.ROTATION_2X2X2_12H)
                .cycleDays(6)
                .requiredGuardsPerPost(3)
                .description("2 días diurnos, 2 días nocturnos, 2 libres. Ciclo de 6 días.")
                .build());

        rotationSchemeRepository.save(RotationScheme.builder()
                .name("Rotación 3x3 (12 Horas)")
                .pattern(RotationPattern.ROTATION_3X3_12H)
                .cycleDays(9)
                .requiredGuardsPerPost(3)
                .description("3 diurnos, 3 nocturnos, 3 libres. Ciclo de 9 días.")
                .build());

        rotationSchemeRepository.save(RotationScheme.builder()
                .name("Rotación 4x4 (12 Horas)")
                .pattern(RotationPattern.ROTATION_4X4_12H)
                .cycleDays(8)
                .requiredGuardsPerPost(4)
                .description("4 días de trabajo (2D + 2N), 4 días de descanso.")
                .build());

        rotationSchemeRepository.save(RotationScheme.builder()
                .name("Rotación 6x1 (8 Horas)")
                .pattern(RotationPattern.ROTATION_6X1_8H)
                .cycleDays(7)
                .requiredGuardsPerPost(4)
                .description("6 días de trabajo continuo y 1 día libre semanal.")
                .build());

        rotationSchemeRepository.save(RotationScheme.builder()
                .name("Rotación 5x2 (8 Horas)")
                .pattern(RotationPattern.ROTATION_5X2_8H)
                .cycleDays(7)
                .requiredGuardsPerPost(1)
                .description("Lunes a Viernes 8 horas, Sábado y Domingo libres.")
                .build());

        log.info("✓ 5 Rotation schemes registered.");

        // 3. Users with Roles
        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Administrador General de Operaciones")
                .email("admin@securops.com")
                .roles(Set.of(RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERVISOR))
                .enabled(true)
                .build());

        User supervisorUser = userRepository.save(User.builder()
                .username("supervisor")
                .password(passwordEncoder.encode("supervisor123"))
                .fullName("Carlos Supervisor de Zona")
                .email("supervisor@securops.com")
                .roles(Set.of(RoleType.ROLE_SUPERVISOR))
                .enabled(true)
                .build());

        User operatorUser = userRepository.save(User.builder()
                .username("operador")
                .password(passwordEncoder.encode("operator123"))
                .fullName("Diana Central de Monitoreo")
                .email("operador@securops.com")
                .roles(Set.of(RoleType.ROLE_OPERATOR))
                .enabled(true)
                .build());

        log.info("✓ Administrative users seeded (admin, supervisor, operador).");

        // 4. Sites and Posts
        Site site = siteRepository.save(Site.builder()
                .name("Complejo Empresarial Metropolitano")
                .city("Bogotá D.C.")
                .address("Calle 100 # 15-20")
                .nodeCode("BOG-CEM-01")
                .active(true)
                .build());

        SecurityPost post247 = securityPostRepository.save(SecurityPost.builder()
                .name("Puesto Principal Acceso Vehicular 24/7")
                .code("POST-VEH-01")
                .site(site)
                .coverageType(ServiceCoverageType.CONTINUOUS_24_7)
                .requiresFirearm(true)
                .requiresLeaderGuard(true)
                .requiresCctvOperator(true)
                .weaponSerialNumber("REV-INDUMIL-77821")
                .latitude(new BigDecimal("4.6854120"))
                .longitude(new BigDecimal("-74.0532100"))
                .geofenceRadiusMeters(50)
                .active(true)
                .build());

        SecurityPost postLobby = securityPostRepository.save(SecurityPost.builder()
                .name("Recepción y Lobby Edificio A (12h Diurno)")
                .code("POST-LOB-01")
                .site(site)
                .coverageType(ServiceCoverageType.DAYTIME_12H)
                .requiresFirearm(false)
                .requiresLeaderGuard(false)
                .requiresCoordinator(false)
                .requiresCctvOperator(false)
                .latitude(new BigDecimal("4.6855000"))
                .longitude(new BigDecimal("-74.0531000"))
                .geofenceRadiusMeters(30)
                .active(true)
                .build());

        log.info("✓ Site and 2 Security Posts created (Puesto 24/7 ID: {}, Puesto Lobby ID: {}).", post247.getId(), postLobby.getId());

        // 5. Guards Squad with specialized roles (Guarda Líder, Operador CCTV, Coordinador, Vigilante Estándar)
        Guard guard1 = createGuard("1020304050", "Carlos", "Mendoza Ramos", "VIG-2024-001", "guarda1",
                com.securops.modules.guards.entity.GuardOperationalRole.POST_LEADER_SUPERVISOR,
                new BigDecimal("1900000.00"), false);
        Guard guard2 = createGuard("1030405060", "Andrés", "Gómez Quintero", "VIG-2024-002", "guarda2",
                com.securops.modules.guards.entity.GuardOperationalRole.CCTV_TECH_OPERATOR,
                new BigDecimal("1850000.00"), true);
        Guard guard3 = createGuard("1040506070", "Javier", "Rojas Beltrán", "VIG-2024-003", "guarda3",
                com.securops.modules.guards.entity.GuardOperationalRole.SECURITY_GUARD,
                new BigDecimal("1600000.00"), false);
        Guard guard4 = createGuard("1050607080", "Mauricio", "Delgado Silva", "VIG-2024-004", "coordinador1",
                com.securops.modules.guards.entity.GuardOperationalRole.POST_COORDINATOR,
                new BigDecimal("2400000.00"), false);

        log.info("✓ 4 Active Guards seeded with specialized roles (Líder: {}, OMT/CCTV: {}, Vigilante: {}, Coordinador: {}).",
                guard1.getId(), guard2.getId(), guard3.getId(), guard4.getId());

        // 6. Current Payroll Period
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        String periodCode = String.format("%d-%02d-M1", now.getYear(), now.getMonthValue());

        PayrollPeriod period = payrollPeriodRepository.save(PayrollPeriod.builder()
                .periodCode(periodCode)
                .startDate(startOfMonth)
                .endDate(endOfMonth)
                .isClosed(false)
                .build());

        log.info("✓ Payroll Period seeded: {} (ID: {}).", period.getPeriodCode(), period.getId());
        log.info("🎉 SecurOps Initialization Completed successfully!");
    }

    private Guard createGuard(String nationalId, String firstName, String lastName, String license, String username,
                              com.securops.modules.guards.entity.GuardOperationalRole role, BigDecimal salary, boolean cctv) {
        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(username + "123"))
                .fullName(firstName + " " + lastName)
                .email(username + "@securops.com")
                .roles(Set.of(RoleType.ROLE_GUARD))
                .enabled(true)
                .build());

        return guardRepository.save(Guard.builder()
                .nationalId(nationalId)
                .firstName(firstName)
                .lastName(lastName)
                .phone("310" + nationalId.substring(4))
                .email(user.getEmail())
                .professionalLicense(license)
                .certifiedFirearms(true)
                .firearmsLicenseExpiry(LocalDate.now().plusYears(1))
                .operationalRole(role)
                .baseSalary(salary)
                .cctvCertified(cctv)
                .status(GuardStatus.ACTIVE)
                .performanceScore(new BigDecimal("100.00"))
                .userAccount(user)
                .build());
    }
}
