package com.mypay.currency.repository;

import com.mypay.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
    Optional<ExchangeRate> findTopByExchangeRateBaseCurrencyAndExchangeRateTargetCurrencyOrderByExchangeRateFetchedDateTimeDesc(String base, String target);
}
