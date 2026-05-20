package com.mypay.currency.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "EXCHANGE_RATE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "exrt_id", columnDefinition = "CHAR(36)", updatable = false)
    private String exchangeRateId;

    @Column(name = "exrt_base", columnDefinition = "CHAR(3)", nullable = false)
    private String exchangeRateBaseCurrency;

    @Column(name = "exrt_target", columnDefinition = "CHAR(3)", nullable = false)
    private String exchangeRateTargetCurrency;

    @Column(name = "exrt_rate", precision = 20, scale = 6, nullable = false)
    private BigDecimal exchangeRateValue;

    @Column(name = "exrt_fetched", nullable = false)
    private LocalDateTime exchangeRateFetchedDateTime;
}
