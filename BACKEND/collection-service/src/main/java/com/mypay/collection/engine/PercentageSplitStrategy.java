package com.mypay.collection.engine;

import com.mypay.collection.dto.ParticipantShare;
import com.mypay.common.constant.SplitType;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PercentageSplitStrategy implements SplitStrategy {

    @Override
    public SplitType type() { return SplitType.PERCENTAGE; }

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    @Override
    public List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants) {
        BigDecimal totalPct = participants.stream()
                .map(ParticipantShare::getPercentage)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        if (HUNDRED.subtract(totalPct).abs().compareTo(TOLERANCE) > 0) {
            throw new BadRequestException("Percentages must sum to 100, got: " + totalPct);
        }

        List<ShareResult> results = participants.stream()
                .map(p -> {
                    BigDecimal base = MoneyUtil.divide(
                            MoneyUtil.multiply(totalAmount, p.getPercentage()), HUNDRED);
                    return ShareResult.builder()
                            .userId(p.getUserId())
                            .baseAmount(base)
                            .taxAmount(BigDecimal.ZERO)
                            .totalAmount(base)
                            .build();
                })
                .collect(Collectors.toList());

        RemainderDistributor.distribute(results, totalAmount);
        return results;
    }
}
