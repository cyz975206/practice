package com.cyz.repository;

import com.cyz.entity.SysConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    Optional<SysConfig> findByIdAndIsDeletedFalse(Long id);

    Optional<SysConfig> findByConfigKeyAndIsDeletedFalse(String configKey);

    boolean existsByConfigKeyAndIsDeletedFalse(String configKey);

    @Query("SELECT c FROM SysConfig c WHERE c.isDeleted = false " +
            "AND (:configKey IS NULL OR c.configKey LIKE %:configKey%) " +
            "AND (:configName IS NULL OR c.configName LIKE %:configName%) " +
            "ORDER BY c.sort, c.id")
    Page<SysConfig> findByConditions(@Param("configKey") String configKey,
                                     @Param("configName") String configName,
                                     Pageable pageable);
}
