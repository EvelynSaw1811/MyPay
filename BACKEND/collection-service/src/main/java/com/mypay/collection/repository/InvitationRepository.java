package com.mypay.collection.repository;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.Invitation;
import com.mypay.common.constant.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, String> {
    List<Invitation> findByInvitationInviteeAndInvitationStatus(String invitee, InvitationStatus status);
    List<Invitation> findByCollection(Collection collection);
    boolean existsByCollectionAndInvitationInviteeAndInvitationStatus(Collection collection, String invitee, InvitationStatus status);
}
