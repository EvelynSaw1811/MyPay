package com.mypay.collection.repository;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByCollection(Collection collection);
    List<Expense> findByCollectionAndExpensePaidBy(Collection collection, String paidByUserId);
    long countByCollection(Collection collection);
}
