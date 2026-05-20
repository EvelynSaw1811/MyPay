package com.mypay.auth.repository;

import com.mypay.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserPhone(String userPhone);
    Optional<User> findByUserNickname(String userNickname);
    Optional<User> findByUserInvitationCode(String userInvitationCode);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserInvitationCode(String userInvitationCode);
}
