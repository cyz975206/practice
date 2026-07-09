package com.cyz.seal.iam.interfaces.dto;

/**
 * 更新用户（人员信息）请求。不含用户名/密码；各字段可选。
 * org_id 变更会校验同法人实体；工号变更校验实体内唯一。
 */
public record UserUpdateRequest(
        String realName,
        Long orgId,
        String employeeNo,
        String phone,
        String position,
        Integer status
) {
}
