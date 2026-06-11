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
        // Invitation notifications mirror collection-service seed invitations:
        // - INV_PENDING_FRANK: Alice sent, Frank received, pending.
        // - INV_ACCEPTED_ALICE: Bob sent, Alice accepted, accepted.
        // - INV_DECLINED_CAROL: David sent, Carol declined, declined/rejected.
        // - INV_PENDING_EMMA_C11 / INV_PENDING_DAVID_C11: Alice sent, Emma/David received, pending.
        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Frank to Bali Trip 2025 as MEMBER.", SeedDataIds.INV_PENDING_FRANK, true);
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Bali Trip 2025",
                "Alice invited you to join Bali Trip 2025.", SeedDataIds.INV_PENDING_FRANK, false);

        notify(SeedUsers.U2, "INVITATION_SENT", "Invitation sent",
                "You invited Alice to SG Weekend Getaway as MEMBER.", SeedDataIds.INV_ACCEPTED_ALICE, true);
        notify(SeedUsers.U1, "INVITATION_RECEIVED", "Invitation to SG Weekend Getaway",
                "Bob invited you to join SG Weekend Getaway.", SeedDataIds.INV_ACCEPTED_ALICE, true);
        notify(SeedUsers.U1, "INVITATION_ACCEPTED_CONFIRMATION", "Invitation accepted",
                "You accepted Bob's invitation to SG Weekend Getaway.", SeedDataIds.INV_ACCEPTED_ALICE, true);
        notify(SeedUsers.U2, "INVITATION_ACCEPTED", "Invitation accepted",
                "Alice accepted your invitation to SG Weekend Getaway.", SeedDataIds.INV_ACCEPTED_ALICE, false);

        notify(SeedUsers.U4, "INVITATION_SENT", "Invitation sent",
                "You invited Carol to Holiday Dinner Party as MEMBER.", SeedDataIds.INV_DECLINED_CAROL, true);
        notify(SeedUsers.U3, "INVITATION_RECEIVED", "Invitation to Holiday Dinner Party",
                "David invited you to join Holiday Dinner Party.", SeedDataIds.INV_DECLINED_CAROL, true);
        notify(SeedUsers.U3, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected David's invitation to Holiday Dinner Party.", SeedDataIds.INV_DECLINED_CAROL, true);
        notify(SeedUsers.U4, "INVITATION_REJECTED", "Invitation rejected",
                "Carol rejected the Holiday Dinner Party invitation.", SeedDataIds.INV_DECLINED_CAROL, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Emma to Flat Share Planning as MEMBER.", SeedDataIds.INV_PENDING_EMMA_C11, true);
        notify(SeedUsers.U5, "INVITATION_RECEIVED", "Invitation to Flat Share Planning",
                "Alice invited you to join Flat Share Planning.", SeedDataIds.INV_PENDING_EMMA_C11, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited David to Flat Share Planning as EDITOR.", SeedDataIds.INV_PENDING_DAVID_C11, true);
        notify(SeedUsers.U4, "INVITATION_RECEIVED", "Invitation to Flat Share Planning",
                "Alice invited you to join Flat Share Planning as EDITOR.", SeedDataIds.INV_PENDING_DAVID_C11, false);

        notify(SeedUsers.U5, "INVITATION_SENT", "Invitation sent",
                "You invited Bob to Empty Planning Collection as MEMBER.", SeedDataIds.INV_PENDING_BOB_C7, true);
        notify(SeedUsers.U2, "INVITATION_RECEIVED", "Invitation to Empty Planning Collection",
                "Emma invited you to join Empty Planning Collection.", SeedDataIds.INV_PENDING_BOB_C7, false);

        notify(SeedUsers.U5, "INVITATION_SENT", "Invitation sent",
                "You invited Carol to Empty Planning Collection as EDITOR.", SeedDataIds.INV_PENDING_CAROL_C7, true);
        notify(SeedUsers.U3, "INVITATION_RECEIVED", "Invitation to Empty Planning Collection",
                "Emma invited you to join Empty Planning Collection as EDITOR.", SeedDataIds.INV_PENDING_CAROL_C7, false);

        notify(SeedUsers.U5, "INVITATION_SENT", "Invitation sent",
                "You invited David to Empty Planning Collection as MEMBER.", SeedDataIds.INV_PENDING_DAVID_C7, true);
        notify(SeedUsers.U4, "INVITATION_RECEIVED", "Invitation to Empty Planning Collection",
                "Emma invited you to join Empty Planning Collection.", SeedDataIds.INV_PENDING_DAVID_C7, false);

        notify(SeedUsers.U5, "INVITATION_SENT", "Invitation sent",
                "You invited Frank to Empty Planning Collection as MEMBER.", SeedDataIds.INV_PENDING_FRANK_C7, true);
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Empty Planning Collection",
                "Emma invited you to join Empty Planning Collection.", SeedDataIds.INV_PENDING_FRANK_C7, false);

        notify(SeedUsers.U6, "INVITATION_SENT", "Invitation sent",
                "You invited Alice to Solo Coffee Run as MEMBER.", SeedDataIds.INV_PENDING_ALICE_C8, true);
        notify(SeedUsers.U1, "INVITATION_RECEIVED", "Invitation to Solo Coffee Run",
                "Frank invited you to join Solo Coffee Run.", SeedDataIds.INV_PENDING_ALICE_C8, false);

        notify(SeedUsers.U6, "INVITATION_SENT", "Invitation sent",
                "You invited Bob to Solo Coffee Run as EDITOR.", SeedDataIds.INV_PENDING_BOB_C8, true);
        notify(SeedUsers.U2, "INVITATION_RECEIVED", "Invitation to Solo Coffee Run",
                "Frank invited you to join Solo Coffee Run as EDITOR.", SeedDataIds.INV_PENDING_BOB_C8, false);

        notify(SeedUsers.U6, "INVITATION_SENT", "Invitation sent",
                "You invited Carol to Solo Coffee Run as MEMBER.", SeedDataIds.INV_PENDING_CAROL_C8, true);
        notify(SeedUsers.U3, "INVITATION_RECEIVED", "Invitation to Solo Coffee Run",
                "Frank invited you to join Solo Coffee Run.", SeedDataIds.INV_PENDING_CAROL_C8, false);

        notify(SeedUsers.U6, "INVITATION_SENT", "Invitation sent",
                "You invited David to Solo Coffee Run as MEMBER.", SeedDataIds.INV_PENDING_DAVID_C8, true);
        notify(SeedUsers.U4, "INVITATION_RECEIVED", "Invitation to Solo Coffee Run",
                "Frank invited you to join Solo Coffee Run.", SeedDataIds.INV_PENDING_DAVID_C8, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Emma to Bali Trip 2025 as MEMBER.", SeedDataIds.INV_PENDING_EMMA_C1, true);
        notify(SeedUsers.U5, "INVITATION_RECEIVED", "Invitation to Bali Trip 2025",
                "Alice invited you to join Bali Trip 2025.", SeedDataIds.INV_PENDING_EMMA_C1, false);

        notify(SeedUsers.U2, "INVITATION_SENT", "Invitation sent",
                "You invited Frank to Office Lunch Pool as MEMBER.", SeedDataIds.INV_PENDING_FRANK_C2, true);
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Office Lunch Pool",
                "Bob invited you to join Office Lunch Pool.", SeedDataIds.INV_PENDING_FRANK_C2, false);

        notify(SeedUsers.U2, "INVITATION_SENT", "Invitation sent",
                "You invited Emma to SG Weekend Getaway as MEMBER.", SeedDataIds.INV_PENDING_EMMA_C3, true);
        notify(SeedUsers.U5, "INVITATION_RECEIVED", "Invitation to SG Weekend Getaway",
                "Bob invited you to join SG Weekend Getaway.", SeedDataIds.INV_PENDING_EMMA_C3, false);

        notify(SeedUsers.U4, "INVITATION_SENT", "Invitation sent",
                "You invited Alice to Holiday Dinner Party as MEMBER.", SeedDataIds.INV_PENDING_ALICE_C5, true);
        notify(SeedUsers.U1, "INVITATION_RECEIVED", "Invitation to Holiday Dinner Party",
                "David invited you to join Holiday Dinner Party.", SeedDataIds.INV_PENDING_ALICE_C5, false);

        notify(SeedUsers.U6, "INVITATION_SENT", "Invitation sent",
                "You invited Emma to Solo Coffee Run as MEMBER.", SeedDataIds.INV_PENDING_EMMA_C8, true);
        notify(SeedUsers.U5, "INVITATION_RECEIVED", "Invitation to Solo Coffee Run",
                "Frank invited you to join Solo Coffee Run.", SeedDataIds.INV_PENDING_EMMA_C8, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Frank to Quarterly Regional Product Operations And Settlement Reconciliation Workshop as MEMBER.", SeedDataIds.INV_PENDING_FRANK_C9, true);
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Quarterly Regional Product Operations And Settlement Reconciliation Workshop",
                "Alice invited you to join Quarterly Regional Product Operations And Settlement Reconciliation Workshop.", SeedDataIds.INV_PENDING_FRANK_C9, false);

        notify(SeedUsers.U2, "INVITATION_SENT", "Invitation sent",
                "You invited David to Office Lunch Pool as MEMBER.", SeedDataIds.INV_DECLINED_DAVID_C2, true);
        notify(SeedUsers.U4, "INVITATION_RECEIVED", "Invitation to Office Lunch Pool",
                "Bob invited you to join Office Lunch Pool.", SeedDataIds.INV_DECLINED_DAVID_C2, true);
        notify(SeedUsers.U4, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected Bob's invitation to Office Lunch Pool.", SeedDataIds.INV_DECLINED_DAVID_C2, true);
        notify(SeedUsers.U2, "INVITATION_REJECTED", "Invitation rejected",
                "David rejected the Office Lunch Pool invitation.", SeedDataIds.INV_DECLINED_DAVID_C2, false);

        notify(SeedUsers.U2, "INVITATION_SENT", "Invitation sent",
                "You invited Carol to SG Weekend Getaway as MEMBER.", SeedDataIds.INV_DECLINED_CAROL_C3, true);
        notify(SeedUsers.U3, "INVITATION_RECEIVED", "Invitation to SG Weekend Getaway",
                "Bob invited you to join SG Weekend Getaway.", SeedDataIds.INV_DECLINED_CAROL_C3, true);
        notify(SeedUsers.U3, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected Bob's invitation to SG Weekend Getaway.", SeedDataIds.INV_DECLINED_CAROL_C3, true);
        notify(SeedUsers.U2, "INVITATION_REJECTED", "Invitation rejected",
                "Carol rejected the SG Weekend Getaway invitation.", SeedDataIds.INV_DECLINED_CAROL_C3, false);

        notify(SeedUsers.U4, "INVITATION_SENT", "Invitation sent",
                "You invited Bob to Holiday Dinner Party as MEMBER.", SeedDataIds.INV_DECLINED_BOB_C5, true);
        notify(SeedUsers.U2, "INVITATION_RECEIVED", "Invitation to Holiday Dinner Party",
                "David invited you to join Holiday Dinner Party.", SeedDataIds.INV_DECLINED_BOB_C5, true);
        notify(SeedUsers.U2, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected David's invitation to Holiday Dinner Party.", SeedDataIds.INV_DECLINED_BOB_C5, true);
        notify(SeedUsers.U4, "INVITATION_REJECTED", "Invitation rejected",
                "Bob rejected the Holiday Dinner Party invitation.", SeedDataIds.INV_DECLINED_BOB_C5, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited David to Quarterly Regional Product Operations And Settlement Reconciliation Workshop as EDITOR.", SeedDataIds.INV_DECLINED_DAVID_C9, true);
        notify(SeedUsers.U4, "INVITATION_RECEIVED", "Invitation to Quarterly Regional Product Operations And Settlement Reconciliation Workshop",
                "Alice invited you to join Quarterly Regional Product Operations And Settlement Reconciliation Workshop as EDITOR.", SeedDataIds.INV_DECLINED_DAVID_C9, true);
        notify(SeedUsers.U4, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected Alice's invitation to Quarterly Regional Product Operations And Settlement Reconciliation Workshop.", SeedDataIds.INV_DECLINED_DAVID_C9, true);
        notify(SeedUsers.U1, "INVITATION_REJECTED", "Invitation rejected",
                "David rejected the Quarterly Regional Product Operations And Settlement Reconciliation Workshop invitation.", SeedDataIds.INV_DECLINED_DAVID_C9, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Emma to Budget Road Trip as MEMBER.", SeedDataIds.INV_DECLINED_EMMA_C10, true);
        notify(SeedUsers.U5, "INVITATION_RECEIVED", "Invitation to Budget Road Trip",
                "Alice invited you to join Budget Road Trip.", SeedDataIds.INV_DECLINED_EMMA_C10, true);
        notify(SeedUsers.U5, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected Alice's invitation to Budget Road Trip.", SeedDataIds.INV_DECLINED_EMMA_C10, true);
        notify(SeedUsers.U1, "INVITATION_REJECTED", "Invitation rejected",
                "Emma rejected the Budget Road Trip invitation.", SeedDataIds.INV_DECLINED_EMMA_C10, false);

        notify(SeedUsers.U1, "INVITATION_SENT", "Invitation sent",
                "You invited Frank to Flat Share Planning as MEMBER.", SeedDataIds.INV_DECLINED_FRANK_C11, true);
        notify(SeedUsers.U6, "INVITATION_RECEIVED", "Invitation to Flat Share Planning",
                "Alice invited you to join Flat Share Planning.", SeedDataIds.INV_DECLINED_FRANK_C11, true);
        notify(SeedUsers.U6, "INVITATION_DECLINED_CONFIRMATION", "Invitation rejected",
                "You rejected Alice's invitation to Flat Share Planning.", SeedDataIds.INV_DECLINED_FRANK_C11, true);
        notify(SeedUsers.U1, "INVITATION_REJECTED", "Invitation rejected",
                "Frank rejected the Flat Share Planning invitation.", SeedDataIds.INV_DECLINED_FRANK_C11, false);

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
        notify(SeedUsers.U5, "TOP_UP_COMPLETED", "Top-up successful",
                "Your MYR wallet was topped up with MYR 300.00.", SeedDataIds.TXN_TOPUP_EMMA_MYR, false);
        notify(SeedUsers.U1, "TRANSFER_REVERSED", "Transfer reversed",
                "Your MYR 150.00 transfer to Bob has been reversed.", SeedDataIds.TXN_REVERSED_ALICE_BOB, true);

        log.info("[Seed] Created or backfilled invitation, expense, settlement, top-up, reversal, read, and unread notifications.");
    }

    private void notify(String userId, String type, String title, String message, String refId, boolean read) {
        notificationRepository.findByNotificationReferenceIdAndNotificationType(refId, type).ifPresentOrElse(
                existing -> {
                    existing.setNotificationUserId(userId);
                    existing.setNotificationTitle(title);
                    existing.setNotificationMessage(message);
                    existing.setNotificationRead(read);
                    existing.setNotificationReadDateTime(read ? LocalDateTime.now().minusDays(1) : null);
                    notificationRepository.save(existing);
                },
                () -> notificationRepository.save(Notification.builder()
                        .notificationUserId(userId)
                        .notificationType(type)
                        .notificationTitle(title)
                        .notificationMessage(message)
                        .notificationReferenceId(refId)
                        .notificationRead(read)
                        .notificationReadDateTime(read ? LocalDateTime.now().minusDays(1) : null)
                        .build())
        );
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
    // ─────────────────────────────────────────────────────────────────────

    private void seedUserPreferences() {
        upsertPreference(SeedUsers.U1, true,  false, true,  false);  // defaults
        upsertPreference(SeedUsers.U2, true,  true,  true,  true);   // all on
        upsertPreference(SeedUsers.U3, false, false, false, false);  // all off
        upsertPreference(SeedUsers.U4, true,  false, true,  false);  // default verbatim
        upsertPreference(SeedUsers.U5, false, true,  false, false);  // SMS only
        upsertPreference(SeedUsers.U6, false, false, false, true);   // promo only
        upsertPreference(SeedUsers.U7, false, false, false, false);  // quarantined inactive user

        log.info("[Seed] Created or backfilled notification preferences for 7 users.");
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
