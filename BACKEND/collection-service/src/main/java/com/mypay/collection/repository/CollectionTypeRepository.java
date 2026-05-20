package com.mypay.collection.repository;

import com.mypay.collection.entity.CollectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionTypeRepository extends JpaRepository<CollectionType, String> {
    List<CollectionType> findByCollectionTypeUserId(String userId);
    boolean existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(String userId, String name);
}
