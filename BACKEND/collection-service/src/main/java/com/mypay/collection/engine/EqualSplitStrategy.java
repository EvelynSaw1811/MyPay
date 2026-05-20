package com.mypay.collection.engine;

import com.mypay.collection.dto.ParticipantShare;
import com.mypay.common.constant.SplitType;
import com.mypay.common.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public SplitType type() { return SplitType.EQUAL; }

    @Override
    public List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants) {
        int n = participants.size();
        BigDecimal share = MoneyUtil.divide(totalAmount, BigDecimal.valueOf(n));

        List<ShareResult> results = participants.stream()
                .map(p -> ShareResult.builder()
                        .userId(p.getUserId())
                        .baseAmount(share)
                        .taxAmount(BigDecimal.ZERO)
                        .totalAmount(share)
                        .build())
                .collect(java.util.stream.Collectors.toList());

        RemainderDistributor.distribute(results, totalAmount);
        return results;
    }
}
