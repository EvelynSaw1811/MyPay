package com.mypay.collection.controller;

import com.mypay.collection.dto.InvitationActionRequest;
import com.mypay.collection.dto.InvitationResponse;
import com.mypay.collection.dto.InviteRequest;
import com.mypay.collection.service.InvitationService;
import com.mypay.collection.config.RequireCollectionRole;
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
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/api/collections/{collectionId}/invitations")
    @RequireCollectionRole({CollectionRole.ADMIN, CollectionRole.EDITOR})
    public ResponseEntity<ApiResponse<InvitationResponse>> invite(
            @PathVariable String collectionId,
            @Valid @RequestBody InviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent", invitationService.invite(collectionId, RequestContextHolder.currentUserId(), request)));
    }

    @GetMapping("/api/collections/{collectionId}/invitations")
    @RequireCollectionRole(CollectionRole.MEMBER)
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getCollectionInvitations(
            @PathVariable String collectionId) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getCollectionInvitations(collectionId)));
    }

    @GetMapping("/api/invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getMyInvitations(RequestContextHolder.currentUserId())));
    }

    @PostMapping("/api/invitations/{invitationId}/respond")
    public ResponseEntity<ApiResponse<InvitationResponse>> respond(
            @PathVariable String invitationId,
            @Valid @RequestBody InvitationActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.respond(invitationId, RequestContextHolder.currentUserId(), request)));
    }
}
