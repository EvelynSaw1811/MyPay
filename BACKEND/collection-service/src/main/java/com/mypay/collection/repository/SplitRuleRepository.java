package com.mypay.collection.repository;

import com.mypay.collection.entity.Expense;
import com.mypay.collection.entity.SplitRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitRuleRepository extends JpaRepository<SplitRule, String> {
    List<SplitRule> findByExpense(Expense expense);
}
