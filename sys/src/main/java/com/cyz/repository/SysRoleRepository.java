package com.cyz.repository;

import com.cyz.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByIdAndIsDeletedFalse(Long id);

    List<SysRole> findByIdInAndIsDeletedFalse(List<Long> ids);

    Optional<SysRole> findByRoleCodeAndIsDeletedFalse(String roleCode);

    boolean existsByRoleCodeAndIsDeletedFalse(String roleCode);

    long countByIsDeletedFalse();

    @Query("SELECT r FROM SysRole r WHERE r.isDeleted = false " +
            "AND (:roleName IS NULL OR r.roleName LIKE %:roleName%) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "ORDER BY r.sort, r.id")
    Page<SysRole> findByConditions(@Param("roleName") String roleName,
                                   @Param("status") String status,
                                   Pageable pageable);
}
