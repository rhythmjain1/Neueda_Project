package com.neueda.tms.repository;

import com.neueda.tms.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionRef(String transactionRef);

    Page<Transaction> findByAccountId(String accountId, Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t
        WHERE (:search IS NULL OR :search = '' OR
               LOWER(t.transactionRef) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(t.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(t.accountId) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR t.status = :status)
          AND (:transactionType IS NULL OR t.transactionType = :transactionType)
          AND (:countryCode IS NULL OR :countryCode = '' OR t.countryCode = :countryCode)
          AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
          AND (:toDate IS NULL OR t.createdAt <= :toDate)
          AND (:minAmount IS NULL OR t.amount >= :minAmount)
          AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
        """)
    Page<Transaction> searchTransactions(
            @Param("search") String search,
            @Param("status") Transaction.TransactionStatus status,
            @Param("transactionType") Transaction.TransactionType transactionType,
            @Param("countryCode") String countryCode,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    // For rapid transactions rule: count transactions from same account in time window
    @Query("""
        SELECT COUNT(t) FROM Transaction t
        WHERE t.accountId = :accountId
          AND t.createdAt >= :since
        """)
    long countRecentTransactionsByAccount(
            @Param("accountId") String accountId,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt >= :since")
    long countTransactionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM Transaction t")
    long countAll();
}
