package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.PersonCreateRequest;
import com.cyz.dto.request.PersonQueryRequest;
import com.cyz.dto.request.PersonUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.PersonResponse;
import com.cyz.service.SysPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag(name = "人员管理")
@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class SysPersonController {

    private final SysPersonService personService;

    @Operation(summary = "创建人员")
    @OperationLog(module = "人员管理", type = OperationType.ADD, description = "创建人员")
    @PostMapping
    public R<PersonResponse> create(@Valid @RequestBody PersonCreateRequest request) {
        return R.ok(personService.create(request));
    }

    @Operation(summary = "更新人员")
    @OperationLog(module = "人员管理", type = OperationType.EDIT, description = "更新人员")
    @PutMapping("/{id}")
    public R<PersonResponse> update(@Parameter(description = "人员ID") @PathVariable Long id,
                                    @Valid @RequestBody PersonUpdateRequest request) {
        return R.ok(personService.update(id, request));
    }

    @Operation(summary = "删除人员")
    @OperationLog(module = "人员管理", type = OperationType.DELETE, description = "删除人员")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "人员ID") @PathVariable Long id) {
        personService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询人员详情")
    @GetMapping("/{id}")
    public R<PersonResponse> getById(@Parameter(description = "人员ID") @PathVariable Long id) {
        return R.ok(personService.getById(id));
    }

    @Operation(summary = "分页查询人员")
    @GetMapping
    public R<Page<PersonResponse>> list(PersonQueryRequest request) {
        return R.ok(personService.list(request));
    }

    @Operation(summary = "导出人员Excel")
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        byte[] data = personService.exportExcel();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + new String("人员数据.xlsx".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @Operation(summary = "导入人员Excel")
    @PostMapping("/import/excel")
    public R<ImportResultResponse> importExcel(@Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        return R.ok(personService.importExcel(file));
    }
}
