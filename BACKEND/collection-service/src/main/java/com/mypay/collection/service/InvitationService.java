package com.mypay.collection.service;

import com.mypay.collection.dto.InvitationActionRequest;
import com.mypay.collection.dto.InvitationResponse;
import com.mypay.collection.dto.InviteRequest;

import java.util.List;

public interface InvitationService {
    InvitationResponse invite(String collectionId, String inviterId, InviteRequest request);
    List<InvitationResponse> getCollectionInvitations(String collectionId);
    List<InvitationResponse> getMyInvitations(String userId);
    InvitationResponse respond(String invitationId, String userId, InvitationActionRequest request);
}
