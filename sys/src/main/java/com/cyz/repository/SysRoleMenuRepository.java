package com.cyz.repository;

import com.cyz.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    List<SysRoleMenu> findByRoleIdAndIsDeletedFalse(Long roleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    List<SysRoleMenu> findByRoleIdInAndIsDeletedFalse(List<Long> roleIds);

    List<SysRoleMenu> findByMenuIdAndIsDeletedFalse(Long menuId);
}
