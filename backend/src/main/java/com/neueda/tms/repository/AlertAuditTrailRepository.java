package com.neueda.tms.repository;

import com.neueda.tms.model.AlertAuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertAuditTrailRepository extends JpaRepository<AlertAuditTrail, Long> {

    List<AlertAuditTrail> findByAlertIdOrderByCreatedAtAsc(Long alertId);

    @Query("""
        SELECT a FROM AlertAuditTrail a
        JOIN FETCH a.alert al
        WHERE (:fromDate IS NULL OR a.createdAt >= :fromDate)
          AND (:toDate IS NULL OR a.createdAt <= :toDate)
          AND (:action IS NULL OR a.action = :action)
        ORDER BY a.createdAt DESC
        """)
    Page<AlertAuditTrail> findAuditReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("action") AlertAuditTrail.AuditAction action,
            Pageable pageable
    );
}
