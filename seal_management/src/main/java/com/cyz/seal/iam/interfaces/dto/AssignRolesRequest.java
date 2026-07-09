package com.cyz.seal.iam.interfaces.dto;

import java.util.List;

public record AssignRolesRequest(List<Long> roleIds) {
}
