package com.mypay.collection.service.impl;

import com.mypay.collection.client.AuthClient;
import com.mypay.collection.dto.InvitationActionRequest;
import com.mypay.collection.dto.InvitationResponse;
import com.mypay.collection.dto.InviteRequest;
import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import com.mypay.collection.entity.Invitation;
import com.mypay.collection.mapper.InvitationMapper;
import com.mypay.collection.messaging.NotificationEventPublisher;
import com.mypay.collection.repository.CollectionMemberRepository;
import com.mypay.collection.repository.CollectionRepository;
import com.mypay.collection.repository.InvitationRepository;
import com.mypay.collection.service.InvitationService;
import com.mypay.common.constant.CollectionStatus;
import com.mypay.common.constant.InvitationStatus;
import com.mypay.common.event.NotificationEvent;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ConflictException;
import com.mypay.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final CollectionRepository collectionRepository;
    private final CollectionMemberRepository memberRepository;
    private final InvitationRepository invitationRepository;
    private final NotificationEventPublisher notificationPublisher;
    private final InvitationMapper invitationMapper;
    private final AuthClient authClient;

    @Override
    @Transactional
    public InvitationResponse invite(String collectionId, String inviterId, InviteRequest request) {
        Collection collection = findCollection(collectionId);
        String inviteeUserId = Optional.ofNullable(request.getInviteeUserId())
                .filter(id -> !id.isBlank())
                .orElseGet(() -> resolveUserId(request.getIdentifier()));

        if (inviteeUserId == null || inviteeUserId.isBlank()) {
            throw new BadRequestException("Invitee user ID is required");
        }

        if (collection.getCollectionStatus() != CollectionStatus.ACTIVE) {
            throw new BadRequestException("Cannot invite to a closed collection");
        }

        if (memberRepository.existsByCollectionAndCollectionMemberUserId(collection, inviteeUserId)) {
            throw new ConflictException("User is already a member of this collection");
        }

        if (invitationRepository.existsByCollectionAndInvitationInviteeAndInvitationStatus(
                collection, inviteeUserId, InvitationStatus.PENDING)) {
            throw new ConflictException("Invitation already pending for this user");
        }

        Invitation invitation = Invitation.builder()
                .collection(collection)
                .invitationInviter(inviterId)
                .invitationInvitee(inviteeUserId)
                .invitationRole(request.getRole())
                .build();
        invitation = invitationRepository.save(invitation);

        notificationPublisher.publishInvitationReceived(NotificationEvent.builder()
                .userId(inviteeUserId)
                .type("INVITATION_RECEIVED")
                .title("Collection invitation")
                .message("You've been invited to join: " + collection.getCollectionName())
                .referenceId(invitation.getInvitationId())
                .build());

        return invitationMapper.toResponse(invitation);
    }

    @Override
    public List<InvitationResponse> getCollectionInvitations(String collectionId) {
        Collection collection = findCollection(collectionId);
        return invitationRepository.findByCollection(collection).stream()
                .map(invitationMapper::toResponse)
                .toList();
    }

    @Override
    public List<InvitationResponse> getMyInvitations(String userId) {
        return invitationRepository.findByInvitationInviteeAndInvitationStatus(userId, InvitationStatus.PENDING).stream()
                .map(invitationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InvitationResponse respond(String invitationId, String userId, InvitationActionRequest request) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getInvitationInvitee().equals(userId)) {
            throw new BadRequestException("This invitation is not for you");
        }

        if (invitation.getInvitationStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Invitation already responded to");
        }

        if ("ACCEPT".equals(request.getAction())) {
            invitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            CollectionMember member = CollectionMember.builder()
                    .collection(invitation.getCollection())
                    .collectionMemberUserId(userId)
                    .collectionMemberRole(invitation.getInvitationRole())
                    .build();
            memberRepository.save(member);
        } else {
            invitation.setInvitationStatus(InvitationStatus.DECLINED);
        }

        return invitationMapper.toResponse(invitationRepository.save(invitation));
    }

    private Collection findCollection(String collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));
    }

    private String resolveUserId(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        var response = authClient.resolveUser(identifier.trim());
        if (response == null || response.getData() == null || response.getData().get("userId") == null) {
            throw new ResourceNotFoundException("User not found: " + identifier);
        }
        return response.getData().get("userId").toString();
    }
}
