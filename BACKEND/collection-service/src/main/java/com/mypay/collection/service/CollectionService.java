package com.mypay.collection.service;

import com.mypay.collection.dto.*;

import java.util.List;

public interface CollectionService {
    CollectionResponse createCollection(String userId, CreateCollectionRequest request);
    List<CollectionResponse> getMyCollections(String userId);
    CollectionResponse getCollection(String collectionId, String userId);
    CollectionResponse updateCollection(String collectionId, CreateCollectionRequest request);
    void closeCollection(String collectionId, String userId);
    List<MemberResponse> getMembers(String collectionId);
    void removeMember(String collectionId, String targetUserId, String requesterId);
    List<BalanceSummaryResponse> getBalances(String collectionId, String userId);
}
