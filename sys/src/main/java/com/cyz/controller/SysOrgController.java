package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.OrgCreateRequest;
import com.cyz.dto.request.OrgQueryRequest;
import com.cyz.dto.request.OrgUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.OrgResponse;
import com.cyz.dto.response.OrgTreeResponse;
import com.cyz.service.SysOrgService;
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
import java.util.List;

@Tag(name = "机构管理")
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class SysOrgController {

    private final SysOrgService orgService;

    @Operation(summary = "创建机构")
    @OperationLog(module = "机构管理", type = OperationType.ADD, description = "创建机构")
    @PostMapping
    public R<OrgResponse> create(@Valid @RequestBody OrgCreateRequest request) {
        return R.ok(orgService.create(request));
    }

    @Operation(summary = "更新机构")
    @OperationLog(module = "机构管理", type = OperationType.EDIT, description = "更新机构")
    @PutMapping("/{id}")
    public R<OrgResponse> update(@Parameter(description = "机构ID") @PathVariable Long id,
                                 @Valid @RequestBody OrgUpdateRequest request) {
        return R.ok(orgService.update(id, request));
    }

    @Operation(summary = "删除机构")
    @OperationLog(module = "机构管理", type = OperationType.DELETE, description = "删除机构")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "机构ID") @PathVariable Long id) {
        orgService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询机构详情")
    @GetMapping("/{id}")
    public R<OrgResponse> getById(@Parameter(description = "机构ID") @PathVariable Long id) {
        return R.ok(orgService.getById(id));
    }

    @Operation(summary = "分页查询机构")
    @GetMapping
    public R<Page<OrgResponse>> list(OrgQueryRequest request) {
        return R.ok(orgService.list(request));
    }

    @Operation(summary = "获取机构树")
    @GetMapping("/tree")
    public R<List<OrgTreeResponse>> getTree() {
        return R.ok(orgService.getTree());
    }

    @Operation(summary = "更新机构状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@Parameter(description = "机构ID") @PathVariable Long id,
                                @Parameter(description = "状态") @RequestParam String status) {
        orgService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "批量更新排序")
    @PutMapping("/sort")
    public R<Void> batchUpdateSort(@Parameter(description = "机构ID列表") @RequestParam List<Long> ids,
                                   @Parameter(description = "排序号列表") @RequestParam List<Integer> sorts) {
        orgService.batchUpdateSort(ids, sorts);
        return R.ok();
    }

    @Operation(summary = "导出机构Excel")
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        byte[] data = orgService.exportExcel();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + new String("机构数据.xlsx".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @Operation(summary = "导入机构Excel")
    @PostMapping("/import/excel")
    public R<ImportResultResponse> importExcel(@Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        return R.ok(orgService.importExcel(file));
    }
}
