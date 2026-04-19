package com.cyz.repository;

import com.cyz.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    List<SysUserRole> findByUserIdAndIsDeletedFalse(Long userId);

    List<SysUserRole> findByRoleIdAndIsDeletedFalse(Long roleId);

    List<SysUserRole> findByUserIdInAndIsDeletedFalse(List<Long> userIds);
}
