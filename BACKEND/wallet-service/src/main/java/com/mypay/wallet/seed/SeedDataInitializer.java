package com.mypay.wallet.seed;

import com.mypay.common.constant.SeedUsers;
import com.mypay.wallet.entity.Account;
import com.mypay.wallet.entity.Payee;
import com.mypay.wallet.entity.Wallet;
import com.mypay.wallet.repository.AccountRepository;
import com.mypay.wallet.repository.PayeeRepository;
import com.mypay.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final AccountRepository accountRepo;
    private final WalletRepository walletRepo;
    private final PayeeRepository payeeRepo;
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        repairLegacyWalletIndexes();

        Account a1 = createAccountIfMissing(SeedUsers.U1, Map.of("MYR", bd(2500), "SGD", bd(500), "USD", bd(100)));
        Account a2 = createAccountIfMissing(SeedUsers.U2, Map.of("MYR", bd(1800), "SGD", bd(1200)));
        Account a3 = createAccountIfMissing(SeedUsers.U3, Map.of("MYR", bd(3200), "USD", bd(50)));
        Account a4 = createAccountIfMissing(SeedUsers.U4, Map.of("MYR", bd(2000)));
        Account a5 = createAccountIfMissing(SeedUsers.U5, Map.of("MYR", bd(1500), "SGD", bd(100), "USD", bd(0)));
        Account a6 = createAccountIfMissing(SeedUsers.U6, Map.of("MYR", bd(500), "SGD", bd(2000)));

        // U7 is inactive and intentionally has no wallet account.

        Map<Account, List<Object[]>> payeeMap = Map.of(
                a1, List.<Object[]>of(new Object[]{SeedUsers.U2, "Bob"}, new Object[]{SeedUsers.U3, "Carol"}, new Object[]{SeedUsers.U5, "Emma"}),
                a2, List.<Object[]>of(new Object[]{SeedUsers.U1, "Alice"}, new Object[]{SeedUsers.U6, "Frank"}),
                a3, List.<Object[]>of(new Object[]{SeedUsers.U5, "Emma"}),
                a4, List.<Object[]>of(new Object[]{SeedUsers.U5, "Emma"}, new Object[]{SeedUsers.U1, "Alice"}),
                a5, List.<Object[]>of(new Object[]{SeedUsers.U2, "Bob"}),
                a6, List.<Object[]>of(new Object[]{SeedUsers.U4, "David"})
        );

        payeeMap.forEach((account, contacts) ->
                contacts.forEach(p -> savePayeeIfMissing(account, (String) p[0], (String) p[1]))
        );

        log.info("[Seed] Created or backfilled accounts, wallets, and payee contacts.");
    }

    private Account createAccountIfMissing(String userId, Map<String, BigDecimal> balances) {
        Account account = accountRepo.findByAccountUserId(userId)
                .orElseGet(() -> accountRepo.save(Account.builder().accountUserId(userId).build()));

        // Remove wallets that should not exist for this user (stale data from previous runs).
        List<Wallet> existing = walletRepo.findByAccount_AccountId(account.getAccountId());
        List<Wallet> stale = existing.stream()
                .filter(w -> !balances.containsKey(w.getWalletCurrency()))
                .toList();
        if (!stale.isEmpty()) {
            walletRepo.deleteAll(stale);
            log.info("[Seed] Removed stale wallet(s) for user {}: {}", userId,
                    stale.stream().map(Wallet::getWalletCurrency).toList());
        }

        // Add any wallets that are missing.
        List<Wallet> toCreate = balances.entrySet().stream()
                .filter(entry -> !walletRepo.existsByAccount_AccountIdAndWalletCurrency(account.getAccountId(), entry.getKey()))
                .map(entry -> Wallet.builder()
                        .account(account)
                        .walletUserId(userId)
                        .walletCurrency(entry.getKey())
                        .walletBalance(entry.getValue())
                        .walletStatus("ACTIVE")
                        .build())
                .toList();
        if (!toCreate.isEmpty()) {
            walletRepo.saveAll(toCreate);
        }

        account.setWallets(walletRepo.findByAccount_AccountId(account.getAccountId()));
        return account;
    }

    private void savePayeeIfMissing(Account account, String userId, String nickname) {
        if (payeeRepo.existsByPayeeAccountIdAndPayeeUserId(account.getAccountId(), userId)) {
            return;
        }
        payeeRepo.save(Payee.builder()
                .payeeAccountId(account.getAccountId())
                .payeeUserId(userId)
                .payeeNickname(nickname)
                .build());
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v);
    }

    private void repairLegacyWalletIndexes() {
        if (indexExists("wallet_t", "UKry7qxxxfoh5pbw7b0a32ov2ib")) {
            jdbc.execute("ALTER TABLE wallet_t DROP INDEX UKry7qxxxfoh5pbw7b0a32ov2ib");
        }
        if (tableExists("wallet_account_t")) {
            jdbc.update("""
                    DELETE wa
                      FROM wallet_account_t wa
                      JOIN wallet_t w ON w.wllt_id = wa.wacc_wllt_id
                     WHERE w.wllt_acct_id IS NULL
                        OR w.wllt_acct_id = ''
                        OR w.wllt_currency IS NULL
                        OR w.wllt_currency = ''
                    """);
        }
        jdbc.update("""
                DELETE FROM wallet_t
                 WHERE wllt_acct_id IS NULL
                    OR wllt_acct_id = ''
                    OR wllt_currency IS NULL
                    OR wllt_currency = ''
                """);
        if (!indexExists("wallet_t", "uk_wallet_account_currency")) {
            jdbc.execute("ALTER TABLE wallet_t ADD CONSTRAINT uk_wallet_account_currency UNIQUE (wllt_acct_id, wllt_currency)");
        }
        repairLegacyPayeeIndexes();
    }

    private void repairLegacyPayeeIndexes() {
        if (!tableExists("payee_t")) {
            return;
        }
        if (!columnIsNullable("payee_t", "paye_wllt_id")) {
            jdbc.execute("ALTER TABLE payee_t MODIFY paye_wllt_id CHAR(36) NULL");
        }
        jdbc.update("""
                DELETE FROM payee_t
                 WHERE paye_acct_id IS NULL
                    OR paye_acct_id = ''
                """);
        if (indexExists("payee_t", "UK4aiwojhvgefydlqheaocgpg0k")) {
            jdbc.execute("ALTER TABLE payee_t DROP INDEX UK4aiwojhvgefydlqheaocgpg0k");
        }
        if (!indexExists("payee_t", "uk_payee_account_user")) {
            jdbc.execute("ALTER TABLE payee_t ADD CONSTRAINT uk_payee_account_user UNIQUE (paye_acct_id, paye_user_id)");
        }
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                  FROM information_schema.statistics
                 WHERE table_schema = DATABASE()
                   AND table_name = ?
                   AND index_name = ?
                """, Integer.class, tableName, indexName);
        return count != null && count > 0;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                  FROM information_schema.tables
                 WHERE table_schema = DATABASE()
                   AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private boolean columnIsNullable(String tableName, String columnName) {
        // Use queryForList so we get an empty list (not an exception) when the
        // column does not exist in a fresh schema.  A missing column needs no
        // ALTER, so treat it as already-nullable (return true).
        List<String> rows = jdbc.queryForList("""
                SELECT is_nullable
                  FROM information_schema.columns
                 WHERE table_schema = DATABASE()
                   AND table_name = ?
                   AND column_name = ?
                """, String.class, tableName, columnName);
        return rows.isEmpty() || "YES".equalsIgnoreCase(rows.get(0));
    }
}
