package com.mypay.currency.service.impl;

import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.common.util.MoneyUtil;
import com.mypay.currency.dto.ConversionResponse;
import com.mypay.currency.dto.CurrencyResponse;
import com.mypay.currency.dto.ExchangeRateResponse;
import com.mypay.currency.dto.ExchangeRateTableResponse;
import com.mypay.currency.entity.ExchangeRate;
import com.mypay.currency.repository.CurrencyRepository;
import com.mypay.currency.repository.ExchangeRateRepository;
import com.mypay.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${exchange-rate.api-url}")
    private String apiUrl;

    @Value("${exchange-rate.cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    private static final String CACHE_KEY_PREFIX = "exrate:";
    private static final Map<String, BigDecimal> HARDCODED_USD_RATES = Map.of(
            "MYR", new BigDecimal("4.4700"),
            "SGD", new BigDecimal("1.3500"),
            "USD", BigDecimal.ONE
    );

    @Override
    public List<CurrencyResponse> getSupportedCurrencies() {
        return currencyRepository.findByCurrencyActiveTrue().stream()
                .map(c -> CurrencyResponse.builder()
                        .code(c.getCurrencyCode())
                        .name(c.getCurrencyName())
                        .symbol(c.getCurrencySymbol())
                        .active(c.isCurrencyActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ExchangeRateTableResponse getAllRatesFrom(String baseCurrency) {
        String base = baseCurrency.toUpperCase();
        List<String> targets = List.of("MYR", "SGD", "USD");
        Map<String, BigDecimal> rates = new HashMap<>();
        for (String target : targets) {
            if (!target.equals(base)) {
                rates.put(target, resolveRate(base, target));
            }
        }
        return ExchangeRateTableResponse.builder()
                .baseCurrency(base)
                .rates(rates)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public ExchangeRateResponse getRate(String from, String to) {
        BigDecimal rate = resolveRate(from.toUpperCase(), to.toUpperCase());
        return ExchangeRateResponse.builder()
                .baseCurrency(from.toUpperCase())
                .targetCurrency(to.toUpperCase())
                .rate(rate)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public ConversionResponse convert(String from, String to, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        String fromUpper = from.toUpperCase();
        String toUpper = to.toUpperCase();
        BigDecimal rate = resolveRate(fromUpper, toUpper);
        BigDecimal converted = MoneyUtil.multiply(amount, rate);
        return ConversionResponse.builder()
                .fromCurrency(fromUpper)
                .toCurrency(toUpper)
                .originalAmount(amount)
                .convertedAmount(converted)
                .rate(rate)
                .build();
    }

    private BigDecimal resolveRate(String from, String to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        String cacheKey = CACHE_KEY_PREFIX + from + ":" + to;

        Object cached = getCachedRate(cacheKey);
        if (cached != null) {
            return new BigDecimal(cached.toString());
        }

        BigDecimal rate = fetchFromExternalApi(from, to);
        if (rate == null) {
            rate = fetchFromDb(from, to);
        }
        if (rate == null) {
            rate = computeFromHardcoded(from, to);
        }

        cacheRate(cacheKey, rate);
        persistRate(from, to, rate);
        return rate;
    }

    private Object getCachedRate(String cacheKey) {
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("Redis cache read failed for {}: {}", cacheKey, e.getMessage());
            return null;
        }
    }

    private void cacheRate(String cacheKey, BigDecimal rate) {
        try {
            redisTemplate.opsForValue().set(cacheKey, rate.toPlainString(), cacheTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis cache write failed for {}: {}", cacheKey, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private BigDecimal fetchFromExternalApi(String from, String to) {
        try {
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            if (response == null || !"success".equals(response.get("result"))) {
                return null;
            }
            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            if (rates == null) return null;

            BigDecimal usdToFrom = rates.containsKey(from) ? new BigDecimal(rates.get(from).toString()) : null;
            BigDecimal usdToTo = rates.containsKey(to) ? new BigDecimal(rates.get(to).toString()) : null;
            if (usdToFrom == null || usdToTo == null) return null;

            return usdToTo.divide(usdToFrom, 6, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("External exchange rate API call failed: {}", e.getMessage());
            return null;
        }
    }

    private BigDecimal fetchFromDb(String from, String to) {
        return exchangeRateRepository
                .findTopByExchangeRateBaseCurrencyAndExchangeRateTargetCurrencyOrderByExchangeRateFetchedDateTimeDesc(from, to)
                .map(ExchangeRate::getExchangeRateValue)
                .orElse(null);
    }

    private BigDecimal computeFromHardcoded(String from, String to) {
        log.warn("Using hardcoded exchange rates for {}->{}", from, to);
        BigDecimal usdToFrom = HARDCODED_USD_RATES.get(from);
        BigDecimal usdToTo = HARDCODED_USD_RATES.get(to);
        if (usdToFrom == null || usdToTo == null) {
            throw new ResourceNotFoundException("No exchange rate available for " + from + " -> " + to);
        }
        return usdToTo.divide(usdToFrom, 6, RoundingMode.HALF_UP);
    }

    private void persistRate(String from, String to, BigDecimal rate) {
        try {
            ExchangeRate entity = ExchangeRate.builder()
                    .exchangeRateBaseCurrency(from)
                    .exchangeRateTargetCurrency(to)
                    .exchangeRateValue(rate)
                    .exchangeRateFetchedDateTime(LocalDateTime.now())
                    .build();
            exchangeRateRepository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to persist exchange rate: {}", e.getMessage());
        }
    }
}
