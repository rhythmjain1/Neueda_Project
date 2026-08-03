package com.neueda.tms.repository;

import com.neueda.tms.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByStatus(Alert.AlertStatus status, Pageable pageable);

    List<Alert> findByTransactionId(Long transactionId);

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.transaction t
        JOIN FETCH a.rule r
        WHERE (:status IS NULL OR a.status = :status)
          AND (:severity IS NULL OR a.severity = :severity)
          AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
          AND (:toDate IS NULL OR a.createdAt <= :toDate)
          AND (:search IS NULL OR :search = '' OR
               LOWER(t.transactionRef) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(t.accountId) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(t.customerName) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Alert> searchAlerts(
            @Param("status") Alert.AlertStatus status,
            @Param("severity") Alert.AlertSeverity severity,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("search") String search,
            Pageable pageable
    );

    long countByStatus(Alert.AlertStatus status);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = 'OPEN' OR a.status = 'FORWARDED'")
    long countPending();

    @Query("""
        SELECT a.status, COUNT(a) FROM Alert a GROUP BY a.status
        """)
    List<Object[]> countGroupByStatus();

    @Query("""
        SELECT a FROM Alert a
        JOIN FETCH a.transaction t
        WHERE t.accountId = :accountId
        ORDER BY a.createdAt DESC
        """)
    List<Alert> findByAccountId(@Param("accountId") String accountId);

    @Query("""
        SELECT COUNT(a) FROM Alert a WHERE a.createdAt >= :since
        """)
    long countAlertsSince(@Param("since") LocalDateTime since);
}
