package com.mypay.reporting.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.reporting.dto.*;
import com.mypay.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<UserDashboardReport>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getDashboard(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/spending")
    public ResponseEntity<ApiResponse<SpendingSummaryReport>> getSpendingSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getSpendingSummary(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/ledger/{currency}")
    public ResponseEntity<ApiResponse<CurrencyLedgerReport>> getCurrencyLedger(
            @PathVariable String currency) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getCurrencyLedger(RequestContextHolder.currentUserId(), currency)));
    }

    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<ApiResponse<CollectionStatementReport>> getCollectionStatement(
            @PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getCollectionStatement(RequestContextHolder.currentUserId(), collectionId)));
    }

    @GetMapping("/position")
    public ResponseEntity<ApiResponse<PersonalPositionReport>> getPersonalPosition() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getPersonalPosition(RequestContextHolder.currentUserId())));
    }
}
