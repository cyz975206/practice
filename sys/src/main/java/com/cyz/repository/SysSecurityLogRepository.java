package com.cyz.repository;

import com.cyz.entity.SysSecurityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysSecurityLogRepository extends JpaRepository<SysSecurityLog, Long> {

    @Query("SELECT l FROM SysSecurityLog l WHERE l.isDeleted = false " +
            "AND (:riskType IS NULL OR l.riskType = :riskType) " +
            "AND (:riskLevel IS NULL OR l.riskLevel = :riskLevel) " +
            "AND (:handleStatus IS NULL OR l.handleStatus = :handleStatus) " +
            "AND (:startTime IS NULL OR l.occurTime >= :startTime) " +
            "AND (:endTime IS NULL OR l.occurTime <= :endTime) " +
            "ORDER BY l.id DESC")
    Page<SysSecurityLog> findByConditions(@Param("riskType") String riskType,
                                           @Param("riskLevel") String riskLevel,
                                           @Param("handleStatus") String handleStatus,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    List<SysSecurityLog> findByIsDeletedFalseAndOccurTimeBefore(LocalDateTime before);
}
