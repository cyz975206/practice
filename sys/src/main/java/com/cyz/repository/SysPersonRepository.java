package com.cyz.repository;

import com.cyz.entity.SysPerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysPersonRepository extends JpaRepository<SysPerson, Long> {

    Optional<SysPerson> findByIdAndIsDeletedFalse(Long id);

    Optional<SysPerson> findByStaffNumAndIsDeletedFalse(String staffNum);

    boolean existsByStaffNumAndIsDeletedFalse(String staffNum);

    boolean existsByIdCardAndIsDeletedFalse(String idCard);

    Page<SysPerson> findByIsDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM SysPerson p WHERE p.isDeleted = false " +
            "AND (:fullName IS NULL OR p.fullName LIKE %:fullName%) " +
            "AND (:staffNum IS NULL OR p.staffNum = :staffNum) " +
            "AND (:orgCode IS NULL OR p.orgCode = :orgCode) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.id DESC")
    Page<SysPerson> findByConditions(@Param("fullName") String fullName,
                                     @Param("staffNum") String staffNum,
                                     @Param("orgCode") String orgCode,
                                     @Param("status") String status,
                                     Pageable pageable);

    @Query("SELECT p.idCard FROM SysPerson p WHERE p.isDeleted = false")
    List<String> findAllIdCards();

    @Query("SELECT p.staffNum FROM SysPerson p WHERE p.isDeleted = false")
    List<String> findAllStaffNums();

    @Query("SELECT p.phone FROM SysPerson p WHERE p.isDeleted = false")
    List<String> findAllPhones();
}
