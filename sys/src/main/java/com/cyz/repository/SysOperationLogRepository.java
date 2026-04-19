package com.cyz.repository;

import com.cyz.entity.SysOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysOperationLogRepository extends JpaRepository<SysOperationLog, Long> {

    @Query("SELECT l FROM SysOperationLog l WHERE l.isDeleted = false " +
            "AND (:operatorName IS NULL OR l.operatorName LIKE %:operatorName%) " +
            "AND (:module IS NULL OR l.module = :module) " +
            "AND (:operationType IS NULL OR l.operationType = :operationType) " +
            "AND (:result IS NULL OR l.operationResult = :result) " +
            "AND (:startTime IS NULL OR l.operationTime >= :startTime) " +
            "AND (:endTime IS NULL OR l.operationTime <= :endTime) " +
            "ORDER BY l.id DESC")
    Page<SysOperationLog> findByConditions(@Param("operatorName") String operatorName,
                                           @Param("module") String module,
                                           @Param("operationType") String operationType,
                                           @Param("result") String result,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    List<SysOperationLog> findByIsDeletedFalseAndOperationTimeBefore(LocalDateTime before);
}
