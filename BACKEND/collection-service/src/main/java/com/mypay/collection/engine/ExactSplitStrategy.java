package com.mypay.collection.engine;

import com.mypay.collection.dto.ParticipantShare;
import com.mypay.common.constant.SplitType;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExactSplitStrategy implements SplitStrategy {

    @Override
    public SplitType type() { return SplitType.EXACT; }

    @Override
    public List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants) {
        BigDecimal sum = participants.stream()
                .map(ParticipantShare::getFixedAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        if (sum.compareTo(MoneyUtil.round(totalAmount)) != 0) {
            throw new BadRequestException(
                    "Fixed amounts sum (" + sum + ") does not equal total (" + totalAmount + ")");
        }

        return participants.stream()
                .map(p -> ShareResult.builder()
                        .userId(p.getUserId())
                        .baseAmount(MoneyUtil.round(p.getFixedAmount()))
                        .taxAmount(BigDecimal.ZERO)
                        .totalAmount(MoneyUtil.round(p.getFixedAmount()))
                        .build())
                .toList();
    }
}
