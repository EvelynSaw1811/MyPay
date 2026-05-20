package com.mypay.notification.seed;

import com.mypay.common.constant.SeedDataIds;
import com.mypay.common.constant.SeedUsers;
import com.mypay.notification.entity.Notification;
import com.mypay.notification.entity.UserPreference;
import com.mypay.notification.repository.NotificationRepository;
import com.mypay.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedNotifications();
        seedUserPreferences();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Notifications
    // ─────────────────────────────────────────────────────────────────────

    private void seedNotifications() {
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Bali Trip 2025",
                "Alice invited you to join Bali Trip 2025.", SeedDataIds.INV_PENDING_FRANK, false);
        notify(SeedUsers.U1, "INVITATION_ACCEPTED", "Invitation accepted",
                "Your invitation to SG Weekend Getaway was accepted.", SeedDataIds.INV_ACCEPTED_ALICE, true);
        notify(SeedUsers.U4, "INVITATION_DECLINED", "Invitation declined",
                "Carol declined the Holiday Dinner Party invitation.", SeedDataIds.INV_DECLINED_CAROL, false);
        notify(SeedUsers.U2, "EXPENSE_CREATED", "New expense in Bali Trip 2025",
                "Flight Tickets was added to Bali Trip 2025.", SeedDataIds.E2_FLIGHT_TICKETS, false);
        notify(SeedUsers.U3, "SETTLEMENT_SENT", "Settlement sent",
                "You paid Alice MYR 300.00 for Hotel Booking.", SeedDataIds.TXN_SETTLE_CAROL_ALICE, true);
        notify(SeedUsers.U1, "SETTLEMENT_RECEIVED", "Settlement received",
                "Carol paid you MYR 300.00 for Hotel Booking.", SeedDataIds.TXN_SETTLE_CAROL_ALICE, false);
        notify(SeedUsers.U4, "SETTLEMENT_FAILED", "Settlement failed",
                "Frank's USD 40.00 settlement could not be completed.", SeedDataIds.TXN_FAILED_FRANK_DAVID, false);
        notify(SeedUsers.U2, "SETTLEMENT_RECEIVED", "Settlement received",
                "Emma paid you MYR 100.00 for Team Lunch.", SeedDataIds.TXN_SETTLE_EMMA_BOB, false);
        notify(SeedUsers.U5, "SETTLEMENT_SENT", "Settlement sent",
                "You paid Bob MYR 100.00 for Team Lunch.", SeedDataIds.TXN_SETTLE_EMMA_BOB, true);
        notify(SeedUsers.U9, "TOP_UP_COMPLETED", "Top-up successful",
                "Your MYR wallet was topped up with MYR 300.00.", SeedDataIds.TXN_TOPUP_IVY_MYR, false);
        notify(SeedUsers.U1, "TRANSFER_REVERSED", "Transfer reversed",
                "Your MYR 150.00 transfer to Bob has been reversed.", SeedDataIds.TXN_REVERSED_ALICE_BOB, true);

        log.info("[Seed] Created invitation, expense, settlement, top-up, reversal, read, and unread notifications.");
    }

    private void notify(String userId, String type, String title, String message, String refId, boolean read) {
        if (notificationRepository.existsByNotificationReferenceIdAndNotificationType(refId, type)) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .notificationUserId(userId)
                .notificationType(type)
                .notificationTitle(title)
                .notificationMessage(message)
                .notificationReferenceId(refId)
                .notificationRead(read)
                .notificationReadDateTime(read ? LocalDateTime.now().minusDays(1) : null)
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────
    //  User notification preferences
    //
    //  Distribution chosen to exercise every channel combination the Profile
    //  page can render and every code path the service touches:
    //
    //    U1 Alice  — defaults             (email=on,  sms=off, push=on,  promo=off)
    //    U2 Bob    — all on               (email=on,  sms=on,  push=on,  promo=on)   max opt-in
    //    U3 Carol  — all off              (email=off, sms=off, push=off, promo=off)  max opt-out (quiet hours)
    //    U4 David  — email + push only    (email=on,  sms=off, push=on,  promo=off)  default verbatim
    //    U5 Emma   — SMS only             (email=off, sms=on,  push=off, promo=off)  uncommon channel mix
    //    U6 Frank  — promo only           (email=off, sms=off, push=off, promo=on )  marketing-only edge case
    //    U7 Grace  — all off              (security-quarantined inactive user)
    //    U8 Henry  — NOT seeded           tests UserPreferenceService.getOrCreate() lazy-creation path
    //    U9 Ivy    — defaults             pairs null-nickname user with default preferences
    // ─────────────────────────────────────────────────────────────────────

    private void seedUserPreferences() {
        upsertPreference(SeedUsers.U1, true,  false, true,  false);  // defaults
        upsertPreference(SeedUsers.U2, true,  true,  true,  true);   // all on
        upsertPreference(SeedUsers.U3, false, false, false, false);  // all off
        upsertPreference(SeedUsers.U4, true,  false, true,  false);  // default verbatim
        upsertPreference(SeedUsers.U5, false, true,  false, false);  // SMS only
        upsertPreference(SeedUsers.U6, false, false, false, true);   // promo only
        upsertPreference(SeedUsers.U7, false, false, false, false);  // quarantined inactive user
        // U8 intentionally NOT seeded — verifies the lazy-create code path.
        upsertPreference(SeedUsers.U9, true,  false, true,  false);  // defaults

        log.info("[Seed] Created or backfilled notification preferences for 8 users (Henry U8 omitted to test lazy creation).");
    }

    private void upsertPreference(String userId, boolean email, boolean sms, boolean push, boolean promo) {
        userPreferenceRepository.findByUserPreferenceUserId(userId).ifPresentOrElse(
                existing -> { /* keep whatever the user has already chosen — never overwrite */ },
                () -> userPreferenceRepository.save(UserPreference.builder()
                        .userPreferenceUserId(userId)
                        .userPreferenceEmailEnabled(email)
                        .userPreferenceSmsEnabled(sms)
                        .userPreferencePushEnabled(push)
                        .userPreferencePromoEnabled(promo)
                        .build())
        );
    }
}
