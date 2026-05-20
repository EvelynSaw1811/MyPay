package com.mypay.collection.repository;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, String> {
    List<CollectionMember> findByCollection(Collection collection);
    List<CollectionMember> findByCollectionMemberUserId(String userId);
    Optional<CollectionMember> findByCollectionAndCollectionMemberUserId(Collection collection, String userId);
    boolean existsByCollectionAndCollectionMemberUserId(Collection collection, String userId);
}
