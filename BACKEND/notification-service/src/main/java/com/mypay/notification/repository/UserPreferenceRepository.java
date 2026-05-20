package com.mypay.notification.repository;

import com.mypay.notification.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
    Optional<UserPreference> findByUserPreferenceUserId(String userId);
}
