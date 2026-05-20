package com.mypay.collection.engine;

import com.mypay.collection.dto.ParticipantShare;
import com.mypay.common.constant.SplitType;

import java.math.BigDecimal;
import java.util.List;

public interface SplitStrategy {
    SplitType type();
    List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants);
}
