package com.mypay.auth.repository;

import com.mypay.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {
    Optional<UserCredential> findByUserCredentialUserId(String userCredentialUserId);
}
