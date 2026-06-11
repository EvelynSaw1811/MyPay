package com.mypay.auth.seed;

import com.mypay.common.constant.SeedUsers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    private static final String PASSWORD = "Test@1234";

    @Override
    public void run(ApplicationArguments args) {
        boolean usersExist = existsUser(SeedUsers.U1) && existsUser(SeedUsers.U7);

        String hash = new BCryptPasswordEncoder().encode(PASSWORD);

        insertUserIfMissing(SeedUsers.U1, "alice.tan@mypay.test",      "+60111234001", "Alice", "Tan",  "Alice", "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U2, "bob.lim@mypay.test",        "+65901234002", "Bob",   "Lim",  "Bob",   "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U3, "carol.wong@mypay.test",     "+60111234003", "Carol", "Wong", "Carol", "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U4, "david.chen@mypay.test",     "+60111234004", "David", "Chen", "David", "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U5, "emma.lee@mypay.test",       "+60111234005", "Emma",  "Lee",  "Emma",  "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U6, "frank.ng@mypay.test",       "+65901234006", "Frank", "Ng",   "Frank", "ACTIVE",   hash);
        insertUserIfMissing(SeedUsers.U7, "grace.inactive@mypay.test", "+60111234007", "Grace", "Ong",  "Grace", "INACTIVE", hash);

        if (usersExist) {
            log.info("[Seed] Auth users already present — backfilled invitation codes.");
        } else {
            log.info("[Seed] Created or backfilled auth users. Email: alice.tan@mypay.test Password: {}", PASSWORD);
        }
    }

    private void insertUserIfMissing(String id, String email, String phone, String fname, String lname,
                                     String nickname, String status, String hash) {
        String invitationCode = generatedSeedInvitationCode(id);
        if (existsUser(id)) {
            // Backfill invitation code without overwriting anything the user may have edited.
            jdbc.update("""
                    UPDATE user_t
                       SET user_invitation_code = COALESCE(user_invitation_code, ?)
                     WHERE user_id = ?
                    """, invitationCode, id);
            if (!existsCredential(id)) {
                insertCred(id, hash);
            }
            return;
        }

        jdbc.update("""
                INSERT INTO user_t
                  (user_id, user_email, user_phone, user_fname, user_lname, user_nickname,
                   user_invitation_code, user_status, user_created, user_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """, id, email, phone, fname, lname, nickname, invitationCode, status);
        insertCred(id, hash);
    }

    private String generatedSeedInvitationCode(String userId) {
        String digits = userId.replace("-", "");
        return "MP-" + digits.substring(digits.length() - 8).toUpperCase();
    }

    private void insertCred(String userId, String hash) {
        String credId = userId.replaceFirst("^0000", "cred");
        jdbc.update("""
                INSERT INTO user_credential_t
                  (ucrd_id, ucrd_user_id, ucrd_pwd_hash, ucrd_created)
                VALUES (?, ?, ?, NOW())
                """, credId, userId, hash);
    }

    private boolean existsUser(String userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_t WHERE user_id = ?", Integer.class, userId);
        return count != null && count > 0;
    }

    private boolean existsCredential(String userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_credential_t WHERE ucrd_user_id = ?", Integer.class, userId);
        return count != null && count > 0;
    }
}
