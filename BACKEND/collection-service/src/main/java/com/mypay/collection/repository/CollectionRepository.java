package com.mypay.collection.repository;

import com.mypay.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, String> {
    List<Collection> findByCollectionOwnerId(String ownerId);
}
