package com.cyz.repository;

import com.cyz.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    List<SysRoleMenu> findByRoleIdAndIsDeletedFalse(Long roleId);

    List<SysRoleMenu> findByRoleIdInAndIsDeletedFalse(List<Long> roleIds);

    List<SysRoleMenu> findByMenuIdAndIsDeletedFalse(Long menuId);
}
