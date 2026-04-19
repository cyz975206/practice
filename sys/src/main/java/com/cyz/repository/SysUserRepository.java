package com.cyz.repository;

import com.cyz.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByIdAndIsDeletedFalse(Long id);

    Optional<SysUser> findByUsernameAndIsDeletedFalse(String username);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    long countByIsDeletedFalse();

    @Query("SELECT u FROM SysUser u WHERE u.isDeleted = false " +
            "AND (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%) " +
            "AND (:orgCode IS NULL OR u.orgCode = :orgCode) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "ORDER BY u.id DESC")
    Page<SysUser> findByConditions(@Param("username") String username,
                                   @Param("nickname") String nickname,
                                   @Param("orgCode") String orgCode,
                                   @Param("status") String status,
                                   Pageable pageable);
}
