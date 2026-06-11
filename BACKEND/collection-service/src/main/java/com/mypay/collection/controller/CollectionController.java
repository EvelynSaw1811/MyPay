package com.mypay.collection.controller;

import com.mypay.collection.config.RequireCollectionRole;
import com.mypay.collection.dto.*;
import com.mypay.collection.service.CollectionService;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponse>> create(
            @Valid @RequestBody CreateCollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collection created", collectionService.createCollection(RequestContextHolder.currentUserId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionResponse>>> getMyCollections() {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getMyCollections(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/{collectionId}")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<CollectionResponse>> getCollection(@PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(
                collectionService.getCollection(collectionId, RequestContextHolder.currentUserId())));
    }

    @PutMapping("/{collectionId}")
    @RequireCollectionRole(CollectionRole.ADMIN)
    public ResponseEntity<ApiResponse<CollectionResponse>> update(
            @PathVariable String collectionId,
            @Valid @RequestBody CreateCollectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.updateCollection(collectionId, request)));
    }

    @PostMapping("/{collectionId}/close")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable String collectionId) {
        collectionService.closeCollection(collectionId, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Collection closed", null));
    }

    @GetMapping("/{collectionId}/members")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(@PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getMembers(collectionId)));
    }

    @DeleteMapping("/{collectionId}/members/{targetUserId}")
    @RequireCollectionRole(CollectionRole.ADMIN)
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String collectionId,
            @PathVariable String targetUserId) {
        collectionService.removeMember(collectionId, targetUserId, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    @DeleteMapping("/{collectionId}/members/me")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<Void>> leaveCollection(
            @PathVariable String collectionId) {
        collectionService.leaveCollection(collectionId, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Collection removed from your list", null));
    }

    @GetMapping("/{collectionId}/balances")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<List<BalanceSummaryResponse>>> getBalances(
            @PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getBalances(collectionId, RequestContextHolder.currentUserId())));
    }
}
