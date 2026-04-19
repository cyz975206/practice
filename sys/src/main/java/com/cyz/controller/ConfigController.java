package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.ConfigCreateRequest;
import com.cyz.dto.request.ConfigQueryRequest;
import com.cyz.dto.request.ConfigUpdateRequest;
import com.cyz.dto.response.ConfigResponse;
import com.cyz.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "创建系统配置")
    @OperationLog(module = "系统配置", type = OperationType.ADD, description = "创建系统配置")
    @PostMapping
    public R<ConfigResponse> create(@Valid @RequestBody ConfigCreateRequest request) {
        return R.ok(configService.create(request));
    }

    @Operation(summary = "更新系统配置")
    @OperationLog(module = "系统配置", type = OperationType.EDIT, description = "更新系统配置")
    @PutMapping("/{id}")
    public R<ConfigResponse> update(@Parameter(description = "配置ID") @PathVariable Long id,
                                    @Valid @RequestBody ConfigUpdateRequest request) {
        return R.ok(configService.update(id, request));
    }

    @Operation(summary = "删除系统配置")
    @OperationLog(module = "系统配置", type = OperationType.DELETE, description = "删除系统配置")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "配置ID") @PathVariable Long id) {
        configService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询系统配置详情")
    @GetMapping("/{id}")
    public R<ConfigResponse> getById(@Parameter(description = "配置ID") @PathVariable Long id) {
        return R.ok(configService.getById(id));
    }

    @Operation(summary = "分页查询系统配置")
    @GetMapping
    public R<Page<ConfigResponse>> list(ConfigQueryRequest request) {
        return R.ok(configService.list(request));
    }

    @Operation(summary = "按配置键查询配置值")
    @GetMapping("/key/{configKey}")
    public R<String> getByKey(@Parameter(description = "配置键") @PathVariable String configKey) {
        return R.ok(configService.getByKey(configKey));
    }
}
