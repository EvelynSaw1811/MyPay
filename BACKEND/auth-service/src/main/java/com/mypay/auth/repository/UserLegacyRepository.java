package com.mypay.auth.repository;

import com.mypay.auth.entity.UserLegacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLegacyRepository extends JpaRepository<UserLegacy, String> {
}
