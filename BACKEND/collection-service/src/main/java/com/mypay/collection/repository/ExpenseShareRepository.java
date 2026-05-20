package com.mypay.collection.repository;

import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.Expense;
import com.mypay.collection.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, String> {
    List<ExpenseShare> findByExpense(Expense expense);
    Optional<ExpenseShare> findByExpenseShareIdAndExpense_ExpenseId(String expenseShareId, String expenseId);
    List<ExpenseShare> findByExpenseShareUserIdAndExpense_Collection_CollectionId(String userId, String collectionId);

    @Query("SELECT MAX(s.expenseShareSettledDateTime) FROM ExpenseShare s " +
           "WHERE s.expense.collection = :collection AND s.expenseShareSettled = true")
    Optional<LocalDateTime> findLastSettledDateTimeByCollection(@Param("collection") Collection collection);
}
