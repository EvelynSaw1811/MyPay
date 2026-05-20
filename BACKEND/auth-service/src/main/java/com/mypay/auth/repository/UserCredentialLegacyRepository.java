package com.mypay.auth.repository;

import com.mypay.auth.entity.UserCredentialLegacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialLegacyRepository extends JpaRepository<UserCredentialLegacy, String> {
}
