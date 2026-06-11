package com.mypay.collection.seed;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import com.mypay.collection.entity.Expense;
import com.mypay.collection.entity.ExpenseShare;
import com.mypay.collection.entity.Invitation;
import com.mypay.collection.entity.SplitRule;
import com.mypay.collection.entity.CollectionType;
import com.mypay.collection.repository.CollectionMemberRepository;
import com.mypay.collection.repository.CollectionRepository;
import com.mypay.collection.repository.CollectionTypeRepository;
import com.mypay.collection.repository.ExpenseRepository;
import com.mypay.collection.repository.ExpenseShareRepository;
import com.mypay.collection.repository.InvitationRepository;
import com.mypay.collection.repository.SplitRuleRepository;
import com.mypay.common.constant.CollectionCategory;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.constant.CollectionStatus;
import com.mypay.common.constant.InvitationStatus;
import com.mypay.common.constant.SeedDataIds;
import com.mypay.common.constant.SeedUsers;
import com.mypay.common.constant.SplitType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final CollectionRepository collectionRepo;
    private final CollectionMemberRepository memberRepo;
    private final ExpenseRepository expenseRepo;
    private final ExpenseShareRepository shareRepo;
    private final SplitRuleRepository splitRuleRepo;
    private final InvitationRepository invitationRepo;
    private final CollectionTypeRepository collectionTypeRepo;

    private static final String U1 = SeedUsers.U1;
    private static final String U2 = SeedUsers.U2;
    private static final String U3 = SeedUsers.U3;
    private static final String U4 = SeedUsers.U4;
    private static final String U5 = SeedUsers.U5;
    private static final String U6 = SeedUsers.U6;

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Override
    public void run(ApplicationArguments args) {
        seedCollectionTypes();
        if (!collectionRepo.existsById(SeedDataIds.C1_BALI_TRIP)) seedBaliTrip();
        if (!collectionRepo.existsById(SeedDataIds.C2_OFFICE_LUNCH)) seedOfficeLunch();
        if (!collectionRepo.existsById(SeedDataIds.C3_SG_WEEKEND)) seedSgWeekend();
        if (!collectionRepo.existsById(SeedDataIds.C4_TEAM_BUILDING)) seedTeamBuilding();
        if (!collectionRepo.existsById(SeedDataIds.C5_HOLIDAY_DINNER)) seedHolidayDinner();
        if (!collectionRepo.existsById(SeedDataIds.C6_ARCHIVED_MOVIE_NIGHT)) seedClosedMovieNight();
        if (!collectionRepo.existsById(SeedDataIds.C7_EMPTY_COLLECTION)) seedEmptyCollection();
        if (!collectionRepo.existsById(SeedDataIds.C8_SINGLE_MEMBER)) seedSingleMemberCollection();
        if (!collectionRepo.existsById(SeedDataIds.C9_LONG_NAME)) seedLongNameCollection();
        if (!collectionRepo.existsById(SeedDataIds.C10_ALL_SETTLED)) seedAllSettledCollection();
        if (!collectionRepo.existsById(SeedDataIds.C11_MULTI_INVITE)) seedMultiInviteCollection();
        backfillExistingSeedDrift();
        backfillClosedCollectionSettlements();
        seedInvitations();

        log.info("[Seed] Created or backfilled deterministic collections, expenses, invitations, shares, and split rules.");
    }

    private void seedBaliTrip() {
        Collection c = saveCollection(SeedDataIds.C1_BALI_TRIP, "Bali Trip 2025",
                "Group holiday to Bali - flights, hotel, activities", CollectionCategory.TRIP, "MYR", U1, CollectionStatus.ACTIVE);

        saveMember(c, U1, CollectionRole.ADMIN);
        saveMember(c, U2, CollectionRole.EDITOR);
        saveMember(c, U3, CollectionRole.MEMBER);
        saveMember(c, U4, CollectionRole.MEMBER);

        Expense e1 = saveExpense(SeedDataIds.E1_HOTEL_BOOKING, c, "Hotel Booking", bd(1200), "MYR", U1, SplitType.EQUAL, null, null);
        saveShare(e1, U1, bd(300), bd(300), true, null);
        saveShare(e1, U2, bd(300), bd(300), false, null);
        saveShare(SeedDataIds.S_E1_CAROL, e1, U3, bd(300), bd(300), true, NOW.minusDays(8));
        saveShare(SeedDataIds.S_E1_DAVID, e1, U4, bd(300), bd(300), false, null);

        Expense e2 = saveExpense(SeedDataIds.E2_FLIGHT_TICKETS, c, "Flight Tickets", bd(2400), "MYR", U2, SplitType.PERCENTAGE, null, null);
        saveSplitRule(e2, U1, bd(30), null, null);
        saveSplitRule(e2, U2, bd(20), null, null);
        saveSplitRule(e2, U3, bd(30), null, null);
        saveSplitRule(e2, U4, bd(20), null, null);
        saveShare(e2, U1, bd(720), bd(720), false, null);
        saveShare(e2, U2, bd(480), bd(480), true, null);
        saveShare(SeedDataIds.S_E2_CAROL, e2, U3, bd(720), bd(720), true, NOW.minusDays(7));
        saveShare(e2, U4, bd(480), bd(480), false, null);

        Expense e7 = saveExpense(SeedDataIds.E7_ACTIVITY_PASSES, c, "Activity Passes", bd(600), "MYR", U4, SplitType.PERCENTAGE, null, null);
        saveSplitRule(e7, U1, bd(25), null, null);
        saveSplitRule(e7, U2, bd(25), null, null);
        saveSplitRule(e7, U3, bd(25), null, null);
        saveSplitRule(e7, U4, bd(25), null, null);
        saveShare(SeedDataIds.S_E7_ALICE, e7, U1, bd(150), bd(150), false, null);
        saveShare(e7, U2, bd(150), bd(150), false, null);
        saveShare(e7, U3, bd(150), bd(150), false, null);
        saveShare(e7, U4, bd(150), bd(150), true, null);
    }

    private void seedOfficeLunch() {
        Collection c = saveCollection(SeedDataIds.C2_OFFICE_LUNCH, "Office Lunch Pool",
                "Shared office lunch expenses", CollectionCategory.EXPENSE, "MYR", U2, CollectionStatus.ACTIVE);

        saveMember(c, U2, CollectionRole.ADMIN);
        saveMember(c, U3, CollectionRole.MEMBER);
        saveMember(c, U5, CollectionRole.MEMBER);

        Expense e3 = saveExpense(SeedDataIds.E3_TEAM_LUNCH, c, "Team Lunch", bd(300), "MYR", U2, SplitType.EQUAL, null, null);
        saveShare(e3, U2, bd(100), bd(100), true, null);
        saveShare(e3, U3, bd(100), bd(100), false, null);
        saveShare(SeedDataIds.S_E3_EMMA, e3, U5, bd(100), bd(100), true, NOW.minusDays(3));
    }

    private void seedSgWeekend() {
        Collection c = saveCollection(SeedDataIds.C3_SG_WEEKEND, "SG Weekend Getaway",
                "Singapore trip - Sentosa and city sights", CollectionCategory.TRIP, "SGD", U2, CollectionStatus.ACTIVE);

        saveMember(c, U2, CollectionRole.ADMIN);
        saveMember(c, U4, CollectionRole.EDITOR);
        saveMember(c, U6, CollectionRole.MEMBER);
        saveMember(c, U1, CollectionRole.MEMBER);

        Expense e4 = saveExpense(SeedDataIds.E4_SENTOSA_ENTRY, c, "Sentosa Island Entry", bd(120), "SGD", U6, SplitType.EXACT, null, null);
        saveSplitRule(e4, U2, null, bd(40), null);
        saveSplitRule(e4, U4, null, bd(50), null);
        saveSplitRule(e4, U6, null, bd(30), null);
        saveShare(e4, U2, bd(40), bd(40), false, null);
        saveShare(SeedDataIds.S_E4_DAVID, e4, U4, bd(50), bd(50), true, NOW.minusDays(2));
        saveShare(e4, U6, bd(30), bd(30), true, null);
    }

    private void seedTeamBuilding() {
        Collection c = saveCollection(SeedDataIds.C4_TEAM_BUILDING, "Team Building Retreat",
                "Company team building event - equipment and catering", CollectionCategory.OTHER, "MYR", U3, CollectionStatus.ACTIVE);

        saveMember(c, U3, CollectionRole.ADMIN);
        saveMember(c, U1, CollectionRole.EDITOR);
        saveMember(c, U2, CollectionRole.MEMBER);
        saveMember(c, U4, CollectionRole.MEMBER);
        saveMember(c, U5, CollectionRole.MEMBER);
        saveMember(c, U6, CollectionRole.MEMBER);

        Expense e5 = saveExpense(SeedDataIds.E5_EQUIPMENT_RENTAL, c, "Equipment Rental", bd(450), "MYR", U3, SplitType.HYBRID, null, null);
        saveSplitRule(e5, U3, null, bd(200), null);
        saveSplitRule(e5, U1, null, bd(100), null);
        saveSplitRule(e5, U2, null, null, 1);
        saveSplitRule(e5, U4, null, null, 1);
        saveShare(e5, U3, bd(200), bd(200), true, null);
        saveShare(e5, U1, bd(100), bd(100), false, null);
        saveShare(e5, U2, bd(75), bd(75), false, null);
        saveShare(e5, U4, bd(75), bd(75), false, null);

        Expense e8 = saveExpense(SeedDataIds.E8_TAXED_CATERING, c, "Catered Dinner", bd(318), "MYR", U1, SplitType.EQUAL, bd(0.06), "SIMPLE");
        saveShare(e8, U1, bd(50), bd(53), true, null);
        saveShare(e8, U2, bd(50), bd(53), false, null);
        saveShare(e8, U3, bd(50), bd(53), false, null);
        saveShare(e8, U4, bd(50), bd(53), false, null);
        saveShare(e8, U5, bd(50), bd(53), false, null);
        saveShare(e8, U6, bd(50), bd(53), false, null);

        Expense e9 = saveExpense(SeedDataIds.E9_ROUNDING_SNACKS, c, "Snacks and Drinks", bd(100), "MYR", U2, SplitType.EQUAL, null, null);
        saveShare(e9, U1, bd(33.34), bd(33.34), false, null);
        saveShare(e9, U2, bd(33.33), bd(33.33), true, null);
        saveShare(e9, U3, bd(33.33), bd(33.33), false, null);
    }

    private void seedHolidayDinner() {
        Collection c = saveCollection(SeedDataIds.C5_HOLIDAY_DINNER, "Holiday Dinner Party",
                "Christmas dinner - hierarchical split by seniority", CollectionCategory.EXPENSE, "USD", U4, CollectionStatus.ACTIVE);

        saveMember(c, U4, CollectionRole.ADMIN);
        saveMember(c, U5, CollectionRole.MEMBER);
        saveMember(c, U6, CollectionRole.MEMBER);

        Expense e6 = saveExpense(SeedDataIds.E6_CHRISTMAS_DINNER, c, "Christmas Dinner", bd(180), "USD", U4, SplitType.HIERARCHICAL, null, null);
        saveSplitRule(e6, U4, null, null, 4);
        saveSplitRule(e6, U5, null, null, 3);
        saveSplitRule(e6, U6, null, null, 2);
        saveShare(e6, U4, bd(80), bd(80), true, null);
        saveShare(e6, U5, bd(60), bd(60), false, null);
        saveShare(SeedDataIds.S_E6_FRANK, e6, U6, bd(40), bd(40), false, null);
    }

    private void seedClosedMovieNight() {
        Collection c = saveCollection(SeedDataIds.C6_ARCHIVED_MOVIE_NIGHT, "Archived Movie Night",
                "Closed collection for historical and read-only state testing", CollectionCategory.OTHER, "MYR", U5, CollectionStatus.CLOSED);

        saveMember(c, U5, CollectionRole.ADMIN);
        saveMember(c, U1, CollectionRole.MEMBER);
        saveMember(c, U6, CollectionRole.MEMBER);

        Expense e10 = saveExpense(SeedDataIds.E10_CLOSED_MOVIE_TICKETS, c, "Movie Tickets", bd(90), "MYR", U5, SplitType.EQUAL, null, null);
        saveShare(e10, U5, bd(30), bd(30), true, null);
        saveShare(e10, U1, bd(30), bd(30), true, NOW.minusDays(24));
        saveShare(e10, U6, bd(30), bd(30), true, NOW.minusDays(23));
    }

    private void seedEmptyCollection() {
        Collection c = saveCollection(SeedDataIds.C7_EMPTY_COLLECTION, "Empty Planning Collection",
                "Active monthly collection with members but no expenses for empty-state testing", CollectionCategory.MONTHLY, "MYR", U5, CollectionStatus.ACTIVE);
        saveMember(c, U5, CollectionRole.ADMIN);
        saveMember(c, U1, CollectionRole.MEMBER);
    }

    private void seedSingleMemberCollection() {
        Collection c = saveCollection(SeedDataIds.C8_SINGLE_MEMBER, "Solo Coffee Run",
                "Single-member collection for no-debt split behavior", CollectionCategory.EXPENSE, "MYR", U6, CollectionStatus.ACTIVE);
        saveMember(c, U6, CollectionRole.ADMIN);

        Expense e11 = saveExpense(SeedDataIds.E11_SINGLE_MEMBER_COFFEE, c, "Coffee", bd(12.50), "MYR", U6, SplitType.EQUAL, null, null);
        saveShare(e11, U6, bd(12.50), bd(12.50), true, null);
    }

    private void seedLongNameCollection() {
        Collection c = saveCollection(SeedDataIds.C9_LONG_NAME,
                "Quarterly Regional Product Operations And Settlement Reconciliation Workshop",
                "Long description boundary case for frontend wrapping and reporting layout checks across collection cards, detail headers, statements, and export previews.",
                CollectionCategory.OTHER, "MYR", U1, CollectionStatus.ACTIVE);
        saveMember(c, U1, CollectionRole.ADMIN);
        saveMember(c, U2, CollectionRole.EDITOR);
        saveMember(c, U3, CollectionRole.MEMBER);

        Expense e12 = saveExpense(SeedDataIds.E12_LONG_NAME_SUPPLIES, c,
                "Workshop Supplies Long Case", bd(99.99), "MYR", U1, SplitType.EQUAL, null, null);
        saveShare(e12, U1, bd(33.33), bd(33.33), true, null);
        saveShare(e12, U2, bd(33.33), bd(33.33), false, null);
        saveShare(e12, U3, bd(33.33), bd(33.33), false, null);
    }

    private void seedAllSettledCollection() {
        Collection c = saveCollection(SeedDataIds.C10_ALL_SETTLED, "Budget Road Trip",
                "Short getaway with all expenses fully settled — used to verify zero net balances", CollectionCategory.TRIP, "MYR", U1, CollectionStatus.ACTIVE);

        saveMember(c, U1, CollectionRole.ADMIN);
        saveMember(c, U2, CollectionRole.MEMBER);

        Expense e13 = saveExpense(SeedDataIds.E13_ALL_SETTLED_MEAL, c, "Fuel and Toll", bd(240), "MYR", U1, SplitType.EQUAL, null, null);
        saveShare(e13, U1, bd(120), bd(120), true, null);
        saveShare(e13, U2, bd(120), bd(120), true, NOW.minusDays(2));
    }

    private void seedMultiInviteCollection() {
        Collection c = saveCollection(SeedDataIds.C11_MULTI_INVITE, "Flat Share Planning",
                "New collection with multiple pending invitations — used to test invitation deduplication and PENDING state", CollectionCategory.MONTHLY, "MYR", U1, CollectionStatus.ACTIVE);

        saveMember(c, U1, CollectionRole.ADMIN);
    }

    private void seedInvitations() {
        Collection bali = collectionRepo.getReferenceById(SeedDataIds.C1_BALI_TRIP);
        Collection officeLunch = collectionRepo.getReferenceById(SeedDataIds.C2_OFFICE_LUNCH);
        Collection sgWeekend = collectionRepo.getReferenceById(SeedDataIds.C3_SG_WEEKEND);
        Collection holidayDinner = collectionRepo.getReferenceById(SeedDataIds.C5_HOLIDAY_DINNER);
        Collection emptyPlanning = collectionRepo.getReferenceById(SeedDataIds.C7_EMPTY_COLLECTION);
        Collection soloCoffee = collectionRepo.getReferenceById(SeedDataIds.C8_SINGLE_MEMBER);
        Collection longName = collectionRepo.getReferenceById(SeedDataIds.C9_LONG_NAME);
        Collection budgetRoadTrip = collectionRepo.getReferenceById(SeedDataIds.C10_ALL_SETTLED);
        Collection flatShare = collectionRepo.getReferenceById(SeedDataIds.C11_MULTI_INVITE);

        saveInvitationIfMissing(SeedDataIds.INV_PENDING_FRANK, bali, U1, U6, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_ACCEPTED_ALICE, sgWeekend, U2, U1, CollectionRole.MEMBER, InvitationStatus.ACCEPTED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_CAROL, holidayDinner, U4, U3, CollectionRole.MEMBER, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_EMMA_C11, flatShare, U1, U5, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_DAVID_C11, flatShare, U1, U4, CollectionRole.EDITOR, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_BOB_C7, emptyPlanning, U5, U2, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_CAROL_C7, emptyPlanning, U5, U3, CollectionRole.EDITOR, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_DAVID_C7, emptyPlanning, U5, U4, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_FRANK_C7, emptyPlanning, U5, U6, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_ALICE_C8, soloCoffee, U6, U1, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_BOB_C8, soloCoffee, U6, U2, CollectionRole.EDITOR, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_CAROL_C8, soloCoffee, U6, U3, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_DAVID_C8, soloCoffee, U6, U4, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_EMMA_C1, bali, U1, U5, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_FRANK_C2, officeLunch, U2, U6, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_EMMA_C3, sgWeekend, U2, U5, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_ALICE_C5, holidayDinner, U4, U1, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_EMMA_C8, soloCoffee, U6, U5, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_PENDING_FRANK_C9, longName, U1, U6, CollectionRole.MEMBER, InvitationStatus.PENDING);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_DAVID_C2, officeLunch, U2, U4, CollectionRole.MEMBER, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_CAROL_C3, sgWeekend, U2, U3, CollectionRole.MEMBER, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_BOB_C5, holidayDinner, U4, U2, CollectionRole.MEMBER, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_DAVID_C9, longName, U1, U4, CollectionRole.EDITOR, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_EMMA_C10, budgetRoadTrip, U1, U5, CollectionRole.MEMBER, InvitationStatus.DECLINED);
        saveInvitationIfMissing(SeedDataIds.INV_DECLINED_FRANK_C11, flatShare, U1, U6, CollectionRole.MEMBER, InvitationStatus.DECLINED);
    }

    private void backfillClosedCollectionSettlements() {
        expenseRepo.findById(SeedDataIds.E10_CLOSED_MOVIE_TICKETS).ifPresent(expense -> {
            for (ExpenseShare share : shareRepo.findByExpense(expense)) {
                if (!Boolean.TRUE.equals(share.getExpenseShareSettled())) {
                    share.setExpenseShareSettled(true);
                    share.setExpenseShareSettledDateTime(NOW.minusDays(23));
                    shareRepo.save(share);
                }
            }
        });
    }

    private void backfillExistingSeedDrift() {
        expenseRepo.findById(SeedDataIds.E8_TAXED_CATERING).ifPresent(expense -> {
            if (expense.getExpenseAmount() == null || expense.getExpenseAmount().compareTo(bd(318)) != 0) {
                expense.setExpenseAmount(bd(318));
                expenseRepo.save(expense);
            }
        });

        if (!expenseRepo.existsById(SeedDataIds.E12_LONG_NAME_SUPPLIES)) {
            collectionRepo.findById(SeedDataIds.C9_LONG_NAME).ifPresent(collection -> {
                Expense e12 = saveExpense(SeedDataIds.E12_LONG_NAME_SUPPLIES, collection,
                        "Workshop Supplies Long Case", bd(99.99), "MYR", U1, SplitType.EQUAL, null, null);
                saveShare(e12, U1, bd(33.33), bd(33.33), true, null);
                saveShare(e12, U2, bd(33.33), bd(33.33), false, null);
                saveShare(e12, U3, bd(33.33), bd(33.33), false, null);
            });
        }

        expenseRepo.findAll().forEach(expense -> {
            if (expense.getExpenseCreatedBy() == null) {
                expense.setExpenseCreatedBy(expense.getExpensePaidBy());
                expenseRepo.save(expense);
            }
        });
    }

    private Collection saveCollection(String id, String name, String desc, CollectionCategory category,
                                      String currency, String ownerId, CollectionStatus status) {
        return collectionRepo.save(Collection.builder()
                .collectionId(id)
                .collectionName(name)
                .collectionDescription(desc)
                .collectionCategory(category)
                .collectionTypeName(category.name())
                .collectionCurrency(currency)
                .collectionStatus(status)
                .collectionOwnerId(ownerId)
                .build());
    }

    private void seedCollectionTypes() {
        for (String userId : new String[]{U1, U2, U3, U4, U5, U6}) {
            saveUserType(userId, "Trip", true);
            saveUserType(userId, "Expense", true);
            saveUserType(userId, "Monthly", true);
            saveUserType(userId, "Other", true);
        }
        if (!collectionTypeRepo.existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(U1, "Roommates")) {
            collectionTypeRepo.save(CollectionType.builder()
                    .collectionTypeUserId(U1)
                    .collectionTypeName("Roommates")
                    .collectionTypeSystem(false)
                    .build());
        }
    }

    private void saveUserType(String userId, String name, boolean system) {
        if (collectionTypeRepo.existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(userId, name)) {
            return;
        }
        collectionTypeRepo.save(CollectionType.builder()
                .collectionTypeUserId(userId)
                .collectionTypeName(name)
                .collectionTypeSystem(system)
                .build());
    }

    private void saveMember(Collection c, String userId, CollectionRole role) {
        memberRepo.save(CollectionMember.builder()
                .collection(c)
                .collectionMemberUserId(userId)
                .collectionMemberRole(role)
                .build());
    }

    private Expense saveExpense(String id, Collection c, String title, BigDecimal amount, String currency,
                                String paidBy, SplitType splitType, BigDecimal taxRate, String taxType) {
        return expenseRepo.save(Expense.builder()
                .expenseId(id)
                .collection(c)
                .expenseTitle(title)
                .expenseAmount(amount)
                .expenseCurrency(currency)
                .expensePaidBy(paidBy)
                .expenseCreatedBy(paidBy)
                .expenseSplitType(splitType)
                .expenseTaxRate(taxRate)
                .expenseTaxType(taxType)
                .build());
    }

    private void saveSplitRule(Expense e, String userId, BigDecimal pct, BigDecimal fixed, Integer weight) {
        splitRuleRepo.save(SplitRule.builder()
                .expense(e)
                .splitRuleUserId(userId)
                .splitRulePercentage(pct)
                .splitRuleFixedAmount(fixed)
                .splitRuleWeight(weight)
                .build());
    }

    private void saveShare(Expense e, String userId, BigDecimal base, BigDecimal total,
                           boolean settled, LocalDateTime settledAt) {
        saveShare(null, e, userId, base, total, settled, settledAt);
    }

    private void saveShare(String id, Expense e, String userId, BigDecimal base, BigDecimal total,
                           boolean settled, LocalDateTime settledAt) {
        shareRepo.save(ExpenseShare.builder()
                .expenseShareId(id)
                .expense(e)
                .expenseShareUserId(userId)
                .expenseShareBaseAmount(base)
                .expenseShareTaxAmount(total.subtract(base))
                .expenseShareTotalAmount(total)
                .expenseShareSettled(settled)
                .expenseShareSettledDateTime(settledAt)
                .build());
    }

    private void saveInvitationIfMissing(String id, Collection collection, String inviter, String invitee,
                                CollectionRole role, InvitationStatus status) {
        if (invitationRepo.existsById(id)) {
            return;
        }
        invitationRepo.save(Invitation.builder()
                .invitationId(id)
                .collection(collection)
                .invitationInviter(inviter)
                .invitationInvitee(invitee)
                .invitationRole(role)
                .invitationStatus(status)
                .build());
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }
}
