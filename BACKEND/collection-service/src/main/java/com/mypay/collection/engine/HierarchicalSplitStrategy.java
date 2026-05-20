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
public class HierarchicalSplitStrategy implements SplitStrategy {

    @Override
    public SplitType type() { return SplitType.HIERARCHICAL; }

    @Override
    public List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants) {
        int totalWeight = participants.stream()
                .mapToInt(p -> p.getShareWeight() != null ? p.getShareWeight() : 1)
                .sum();

        if (totalWeight == 0) {
            throw new BadRequestException("Total weight must be greater than zero");
        }

        BigDecimal totalWeightBd = BigDecimal.valueOf(totalWeight);

        List<ShareResult> results = participants.stream()
                .map(p -> {
                    int weight = p.getShareWeight() != null ? p.getShareWeight() : 1;
                    BigDecimal share = MoneyUtil.divide(
                            MoneyUtil.multiply(totalAmount, BigDecimal.valueOf(weight)),
                            totalWeightBd);
                    return ShareResult.builder()
                            .userId(p.getUserId())
                            .baseAmount(share)
                            .taxAmount(BigDecimal.ZERO)
                            .totalAmount(share)
                            .build();
                })
                .collect(Collectors.toList());

        RemainderDistributor.distribute(results, totalAmount);
        return results;
    }
}
