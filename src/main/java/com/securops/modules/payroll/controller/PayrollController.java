package com.securops.modules.payroll.controller;

import com.securops.modules.payroll.entity.PayrollSettlement;
import com.securops.modules.payroll.service.PayrollCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollCalculatorService payrollCalculatorService;

    @PostMapping("/calculate/{guardId}/period/{periodId}")
    public ResponseEntity<PayrollSettlement> calculateSettlement(
            @PathVariable Long guardId,
            @PathVariable Long periodId) {
        PayrollSettlement settlement = payrollCalculatorService.calculateSettlementForGuard(guardId, periodId);
        return ResponseEntity.ok(settlement);
    }
}
