package com.cyz.repository;

import com.cyz.entity.SysSystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysSystemLogRepository extends JpaRepository<SysSystemLog, Long> {

    @Query("SELECT l FROM SysSystemLog l WHERE l.isDeleted = false " +
            "AND (:logLevel IS NULL OR l.logLevel = :logLevel) " +
            "AND (:logModule IS NULL OR l.logModule = :logModule) " +
            "AND (:startTime IS NULL OR l.occurTime >= :startTime) " +
            "AND (:endTime IS NULL OR l.occurTime <= :endTime) " +
            "ORDER BY l.id DESC")
    Page<SysSystemLog> findByConditions(@Param("logLevel") String logLevel,
                                        @Param("logModule") String logModule,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        Pageable pageable);

    List<SysSystemLog> findByIsDeletedFalseAndOccurTimeBefore(LocalDateTime before);
}
