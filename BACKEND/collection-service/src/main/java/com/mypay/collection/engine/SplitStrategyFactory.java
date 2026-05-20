package com.mypay.collection.engine;

import com.mypay.common.constant.SplitType;
import com.mypay.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SplitStrategyFactory {

    private final Map<SplitType, SplitStrategy> strategies;

    public SplitStrategyFactory(List<SplitStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SplitStrategy::type, Function.identity()));
    }

    public SplitStrategy get(SplitType type) {
        SplitStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new BadRequestException("Unsupported split type: " + type);
        }
        return strategy;
    }
}
