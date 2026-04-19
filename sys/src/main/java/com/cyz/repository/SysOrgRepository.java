package com.cyz.repository;

import com.cyz.entity.SysOrg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysOrgRepository extends JpaRepository<SysOrg, Long> {

    Optional<SysOrg> findByIdAndIsDeletedFalse(Long id);

    Optional<SysOrg> findByOrgCodeAndIsDeletedFalse(String orgCode);

    List<SysOrg> findByOrgCodeInAndIsDeletedFalse(List<String> orgCodes);

    boolean existsByOrgCodeAndIsDeletedFalse(String orgCode);

    List<SysOrg> findByParentOrgCodeAndIsDeletedFalseOrderBySort(String parentOrgCode);

    List<SysOrg> findByIsDeletedFalseOrderByOrgCode();

    Page<SysOrg> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT o FROM SysOrg o WHERE o.isDeleted = false " +
            "AND (:shortName IS NULL OR o.orgShortName LIKE %:shortName%) " +
            "AND (:level IS NULL OR o.orgLevel = :level) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:parentCode IS NULL OR o.parentOrgCode = :parentCode) " +
            "ORDER BY o.sort, o.id")
    Page<SysOrg> findByConditions(@Param("shortName") String shortName,
                                   @Param("level") String level,
                                   @Param("status") String status,
                                   @Param("parentCode") String parentCode,
                                   Pageable pageable);

    @Query("SELECT o.orgCode FROM SysOrg o WHERE o.isDeleted = false")
    List<String> findAllOrgCodes();

    @Query("SELECT o FROM SysOrg o WHERE o.isDeleted = false ORDER BY o.sort, o.id")
    List<SysOrg> findAllActiveOrderBySort();
}
