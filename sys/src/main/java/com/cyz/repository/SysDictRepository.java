package com.cyz.repository;

import com.cyz.entity.SysDict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysDictRepository extends JpaRepository<SysDict, Long> {

    // ---- 类型头查询 ----

    Optional<SysDict> findByDictTypeAndDictCodeIsNullAndIsDeletedFalse(String dictType);

    boolean existsByDictTypeAndDictCodeIsNullAndIsDeletedFalse(String dictType);

    @Query("SELECT d FROM SysDict d WHERE d.isDeleted = false AND d.dictCode IS NULL " +
            "AND (:dictType IS NULL OR d.dictType LIKE %:dictType%) " +
            "AND (:dictName IS NULL OR d.dictName LIKE %:dictName%) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "ORDER BY d.id DESC")
    Page<SysDict> findTypeByConditions(@Param("dictType") String dictType,
                                        @Param("dictName") String dictName,
                                        @Param("status") String status,
                                        Pageable pageable);

    // ---- 字典项查询 ----

    Optional<SysDict> findByIdAndDictCodeIsNotNullAndIsDeletedFalse(Long id);

    boolean existsByDictTypeAndDictCodeAndIsDeletedFalse(String dictType, String dictCode);

    boolean existsByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(String dictType);

    long countByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(String dictType);

    Page<SysDict> findByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(String dictType, Pageable pageable);

    @Query("SELECT d FROM SysDict d WHERE d.isDeleted = false AND d.dictType = :dictType AND d.dictCode IS NOT NULL " +
            "AND (:status IS NULL OR d.status = :status) " +
            "ORDER BY d.sort, d.id")
    Page<SysDict> findItemByConditions(@Param("dictType") String dictType,
                                        @Param("status") String status,
                                        Pageable pageable);

    List<SysDict> findByDictTypeAndDictCodeIsNotNullAndIsDeletedFalseOrderBySortAscIdAsc(String dictType);

    // ---- 翻译查询 ----

    @Query("SELECT d FROM SysDict d WHERE d.isDeleted = false AND d.dictType = :dictType AND d.dictCode IS NOT NULL ORDER BY d.sort, d.id")
    List<SysDict> findAllItemsByType(@Param("dictType") String dictType);
}
