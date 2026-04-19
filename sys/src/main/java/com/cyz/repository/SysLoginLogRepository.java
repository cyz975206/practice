package com.cyz.repository;

import com.cyz.entity.SysLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {

    @Query("SELECT l FROM SysLoginLog l WHERE l.isDeleted = false " +
            "AND (:username IS NULL OR l.username LIKE %:username%) " +
            "AND (:loginType IS NULL OR l.loginType = :loginType) " +
            "AND (:loginResult IS NULL OR l.loginResult = :loginResult) " +
            "AND (:startTime IS NULL OR l.loginTime >= :startTime) " +
            "AND (:endTime IS NULL OR l.loginTime <= :endTime) " +
            "ORDER BY l.id DESC")
    Page<SysLoginLog> findByConditions(@Param("username") String username,
                                       @Param("loginType") String loginType,
                                       @Param("loginResult") String loginResult,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       Pageable pageable);

    List<SysLoginLog> findByIsDeletedFalseAndLoginTimeBefore(LocalDateTime before);
}
