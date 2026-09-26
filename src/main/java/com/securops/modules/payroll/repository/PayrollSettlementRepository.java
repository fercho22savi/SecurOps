package com.securops.modules.payroll.repository;

import com.securops.modules.payroll.entity.PayrollSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollSettlementRepository extends JpaRepository<PayrollSettlement, Long> {
    Optional<PayrollSettlement> findByGuardIdAndPayrollPeriodId(Long guardId, Long periodId);
    List<PayrollSettlement> findByPayrollPeriodId(Long periodId);
}
