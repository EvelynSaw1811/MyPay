package com.mypay.currency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CURRENCY_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "curr_id", columnDefinition = "CHAR(36)", updatable = false)
    private String currencyId;

    @Column(name = "curr_code", columnDefinition = "CHAR(3)", nullable = false, unique = true)
    private String currencyCode;

    @Column(name = "curr_name", length = 100)
    private String currencyName;

    @Column(name = "curr_symbol", length = 5)
    private String currencySymbol;

    @Column(name = "curr_active")
    @Builder.Default
    private boolean currencyActive = true;
}
