package com.cyz.repository;

import com.cyz.entity.SysTaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SysTaskLogRepository extends JpaRepository<SysTaskLog, Long> {

    @Query("SELECT l FROM SysTaskLog l WHERE " +
            "(:taskName IS NULL OR :taskName = '' OR l.taskName LIKE %:taskName%) " +
            "AND (:funPath IS NULL OR :funPath = '' OR l.funPath = :funPath) " +
            "AND (:runResult IS NULL OR l.runResult = :runResult) " +
            "AND (:startTime IS NULL OR l.createTime >= :startTime) " +
            "AND (:endTime IS NULL OR l.createTime <= :endTime) " +
            "ORDER BY l.id DESC")
    Page<SysTaskLog> findByConditions(
            @Param("taskName") String taskName,
            @Param("funPath") String funPath,
            @Param("runResult") Integer runResult,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
