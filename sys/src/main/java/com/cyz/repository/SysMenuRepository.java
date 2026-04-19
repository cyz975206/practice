package com.cyz.repository;

import com.cyz.entity.SysMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    Optional<SysMenu> findByIdAndIsDeletedFalse(Long id);

    List<SysMenu> findByIdInAndIsDeletedFalse(Set<Long> ids);

    Optional<SysMenu> findByMenuCodeAndIsDeletedFalse(String menuCode);

    boolean existsByMenuCodeAndIsDeletedFalse(String menuCode);

    long countByIsDeletedFalse();

    List<SysMenu> findByParentIdAndIsDeletedFalseOrderBySort(Long parentId);

    @Query("SELECT m FROM SysMenu m WHERE m.isDeleted = false " +
            "AND (:menuName IS NULL OR m.menuName LIKE %:menuName%) " +
            "AND (:menuType IS NULL OR m.menuType = :menuType) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "ORDER BY m.sort, m.id")
    Page<SysMenu> findByConditions(@Param("menuName") String menuName,
                                   @Param("menuType") String menuType,
                                   @Param("status") String status,
                                   Pageable pageable);

    @Query("SELECT m FROM SysMenu m WHERE m.isDeleted = false ORDER BY m.sort, m.id")
    List<SysMenu> findAllActiveOrderBySort();
}
