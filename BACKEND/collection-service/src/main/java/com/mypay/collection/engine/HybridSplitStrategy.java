package com.mypay.collection.engine;

import com.mypay.collection.dto.ParticipantShare;
import com.mypay.common.constant.SplitType;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HybridSplitStrategy implements SplitStrategy {

    @Override
    public SplitType type() { return SplitType.HYBRID; }

    @Override
    public List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants) {
        List<ParticipantShare> fixed = participants.stream()
                .filter(p -> p.getFixedAmount() != null && p.getFixedAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        List<ParticipantShare> shared = participants.stream()
                .filter(p -> p.getFixedAmount() == null || p.getFixedAmount().compareTo(BigDecimal.ZERO) == 0)
                .toList();

        BigDecimal fixedSum = fixed.stream()
                .map(ParticipantShare::getFixedAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        BigDecimal remainder = MoneyUtil.subtract(totalAmount, fixedSum);
        if (MoneyUtil.isNegative(remainder)) {
            throw new BadRequestException("Fixed amounts exceed total expense amount");
        }

        List<ShareResult> results = new ArrayList<>();

        for (ParticipantShare p : fixed) {
            BigDecimal amt = MoneyUtil.round(p.getFixedAmount());
            results.add(ShareResult.builder()
                    .userId(p.getUserId())
                    .baseAmount(amt)
                    .taxAmount(BigDecimal.ZERO)
                    .totalAmount(amt)
                    .build());
        }

        if (!shared.isEmpty()) {
            BigDecimal equalShare = MoneyUtil.divide(remainder, BigDecimal.valueOf(shared.size()));
            List<ShareResult> sharedResults = shared.stream()
                    .map(p -> ShareResult.builder()
                            .userId(p.getUserId())
                            .baseAmount(equalShare)
                            .taxAmount(BigDecimal.ZERO)
                            .totalAmount(equalShare)
                            .build())
                    .collect(Collectors.toList());

            BigDecimal sharedSum = sharedResults.stream()
                    .map(ShareResult::getTotalAmount)
                    .reduce(BigDecimal.ZERO, MoneyUtil::add);
            BigDecimal sharedRemainder = MoneyUtil.subtract(remainder, sharedSum);
            if (sharedRemainder.compareTo(BigDecimal.ZERO) != 0) {
                ShareResult first = sharedResults.get(0);
                first.setTotalAmount(MoneyUtil.add(first.getTotalAmount(), sharedRemainder));
                first.setBaseAmount(MoneyUtil.add(first.getBaseAmount(), sharedRemainder));
            }
            results.addAll(sharedResults);
        }

        return results;
    }
}
