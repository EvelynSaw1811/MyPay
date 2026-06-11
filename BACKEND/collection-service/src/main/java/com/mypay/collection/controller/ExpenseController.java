package com.mypay.collection.controller;

import com.mypay.collection.config.RequireCollectionRole;
import com.mypay.collection.dto.CreateExpenseRequest;
import com.mypay.collection.dto.ExpenseResponse;
import com.mypay.collection.dto.ShareResponse;
import com.mypay.collection.service.ExpenseService;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections/{collectionId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @RequireCollectionRole({CollectionRole.ADMIN, CollectionRole.EDITOR})
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @PathVariable String collectionId,
            @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created", expenseService.createExpense(collectionId, RequestContextHolder.currentUserId(), request)));
    }

    @GetMapping
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(@PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenses(collectionId)));
    }

    @GetMapping("/{expenseId}")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(
            @PathVariable String collectionId,
            @PathVariable String expenseId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpense(collectionId, expenseId)));
    }

    @RequireCollectionRole({CollectionRole.ADMIN, CollectionRole.EDITOR})
    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateExpense(collectionId, expenseId, RequestContextHolder.currentUserId(), request)));
    }

    @RequireCollectionRole({CollectionRole.ADMIN, CollectionRole.EDITOR})
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @RequestParam(defaultValue = "false") boolean waiveSettlements) {
        expenseService.deleteExpense(collectionId, expenseId, RequestContextHolder.currentUserId(), waiveSettlements);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/{expenseId}/shares/{shareId}")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<ShareResponse>> getShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getShare(collectionId, expenseId, shareId)));
    }

    @PostMapping("/{expenseId}/reminders")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<Map<String, Integer>>> sendSettlementReminder(
            @PathVariable String collectionId,
            @PathVariable String expenseId) {
        int sent = expenseService.sendSettlementReminder(collectionId, expenseId, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Settlement reminder sent", Map.of("sent", sent)));
    }

    @PutMapping("/{expenseId}/shares/{shareId}/settle")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<Void>> settleShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId) {
        expenseService.settleShare(collectionId, expenseId, shareId);
        return ResponseEntity.ok(ApiResponse.success("Share settled", null));
    }

    @PutMapping("/{expenseId}/shares/{shareId}/unsettle")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<Void>> unsettleShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId) {
        expenseService.unsettleShare(collectionId, expenseId, shareId);
        return ResponseEntity.ok(ApiResponse.success("Share unsettled", null));
    }
}
