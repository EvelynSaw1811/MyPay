package com.mypay.currency.repository;

import com.mypay.currency.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
    List<Currency> findByCurrencyActiveTrue();
    Optional<Currency> findByCurrencyCode(String code);
    boolean existsByCurrencyCode(String code);
}
