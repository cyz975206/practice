package com.cyz.repository;

import com.cyz.entity.SysTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysTaskRepository extends JpaRepository<SysTask, Long> {

    Optional<SysTask> findByIdAndIsDeletedFalse(Long id);

    List<SysTask> findByServiceNameAndIsDeletedFalse(String serviceName);

    long countByIsDeletedFalse();

    List<SysTask> findAllByIsDeletedFalse();

    boolean existsByServiceNameAndFunPathAndIsDeletedFalse(String serviceName, String funPath);

    @Query("SELECT t FROM SysTask t WHERE t.isDeleted = false " +
            "AND (:name IS NULL OR t.name LIKE %:name%) " +
            "AND (:hasStart IS NULL OR t.hasStart = :hasStart) " +
            "ORDER BY t.id")
    Page<SysTask> findByConditions(@Param("name") String name,
                                   @Param("hasStart") Boolean hasStart,
                                   Pageable pageable);
}
