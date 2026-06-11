package com.mypay.currency.config;

import com.mypay.common.constant.SeedDataIds;
import com.mypay.currency.entity.Currency;
import com.mypay.currency.repository.CurrencyRepository;
import com.mypay.currency.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        saveCurrencyIfMissing("MYR", "Malaysian Ringgit", "RM", true);
        saveCurrencyIfMissing("SGD", "Singapore Dollar", "S$", true);
        saveCurrencyIfMissing("USD", "US Dollar", "$", true);
        saveCurrencyIfMissing("JPY", "Japanese Yen", "JPY", false);

        LocalDateTime fetchedAt = LocalDateTime.now();
        saveRateIfMissing(SeedDataIds.EXRT_MYR_SGD_STALE, "MYR", "SGD", "0.270000", fetchedAt.minusDays(30));
        saveRateIfMissing(SeedDataIds.EXRT_MYR_SGD_CURRENT, "MYR", "SGD", "0.285000", fetchedAt);
        saveRateIfMissing(SeedDataIds.EXRT_SGD_MYR_CURRENT, "SGD", "MYR", "3.508772", fetchedAt);
        saveRateIfMissing(SeedDataIds.EXRT_MYR_USD_CURRENT, "MYR", "USD", "0.210000", fetchedAt);
        saveRateIfMissing(SeedDataIds.EXRT_USD_MYR_CURRENT, "USD", "MYR", "4.761905", fetchedAt);
        saveRateIfMissing(SeedDataIds.EXRT_SGD_USD_CURRENT, "SGD", "USD", "0.736842", fetchedAt);
        saveRateIfMissing(SeedDataIds.EXRT_USD_SGD_CURRENT, "USD", "SGD", "1.357143", fetchedAt);

        log.info("Seeded or backfilled currencies and exchange rates");
    }

    private void saveCurrencyIfMissing(String code, String name, String symbol, boolean active) {
        if (currencyRepository.existsByCurrencyCode(code)) {
            return;
        }
        currencyRepository.save(Currency.builder()
                .currencyCode(code)
                .currencyName(name)
                .currencySymbol(symbol)
                .currencyActive(active)
                .build());
    }

    private void saveRateIfMissing(String id, String base, String target, String value, LocalDateTime fetchedAt) {
        if (exchangeRateRepository.existsById(id)) {
            return;
        }
        jdbc.update("""
                INSERT INTO exchange_rate_t
                  (exrt_id, exrt_base, exrt_target, exrt_rate, exrt_fetched)
                SELECT ?, ?, ?, ?, ?
                 WHERE NOT EXISTS (
                       SELECT 1 FROM exchange_rate_t WHERE exrt_id = ?
                 )
                """, id, base, target, new BigDecimal(value), fetchedAt, id);
    }
}
