package com.mypay.collection.mapper;

import com.mypay.collection.dto.InvitationResponse;
import com.mypay.collection.entity.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationResponse toResponse(Invitation invitation) {
        return InvitationResponse.builder()
                .invitationId(invitation.getInvitationId())
                .collectionId(invitation.getCollection().getCollectionId())
                .collectionName(invitation.getCollection().getCollectionName())
                .collectionCurrency(invitation.getCollection().getCollectionCurrency())
                .inviterId(invitation.getInvitationInviter())
                .inviteeId(invitation.getInvitationInvitee())
                .role(invitation.getInvitationRole())
                .status(invitation.getInvitationStatus())
                .createdAt(invitation.getInvitationCreated())
                .build();
    }
}
