package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.DictItemCreateRequest;
import com.cyz.dto.request.DictItemQueryRequest;
import com.cyz.dto.request.DictItemUpdateRequest;
import com.cyz.dto.request.DictTypeCreateRequest;
import com.cyz.dto.request.DictTypeQueryRequest;
import com.cyz.dto.request.DictTypeUpdateRequest;
import com.cyz.dto.response.DictItemResponse;
import com.cyz.dto.response.DictTranslationResponse;
import com.cyz.dto.response.DictTypeResponse;
import com.cyz.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据字典管理")
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ========== 字典类型 ==========

    @Operation(summary = "创建字典类型")
    @PostMapping("/type")
    public R<DictTypeResponse> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        return R.ok(dictService.createType(request));
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/type/{id}")
    public R<DictTypeResponse> updateType(@Parameter(description = "字典类型ID") @PathVariable Long id,
                                          @Valid @RequestBody DictTypeUpdateRequest request) {
        return R.ok(dictService.updateType(id, request));
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public R<Void> deleteType(@Parameter(description = "字典类型ID") @PathVariable Long id) {
        dictService.deleteType(id);
        return R.ok();
    }

    @Operation(summary = "查询字典类型详情")
    @GetMapping("/type/{id}")
    public R<DictTypeResponse> getTypeById(@Parameter(description = "字典类型ID") @PathVariable Long id) {
        return R.ok(dictService.getTypeById(id));
    }

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/type")
    public R<Page<DictTypeResponse>> listType(DictTypeQueryRequest request) {
        return R.ok(dictService.listType(request));
    }

    @Operation(summary = "更新字典类型状态")
    @PutMapping("/type/{id}/status")
    public R<Void> updateTypeStatus(@Parameter(description = "字典类型ID") @PathVariable Long id,
                                    @Parameter(description = "状态") @RequestParam String status) {
        dictService.updateTypeStatus(id, status);
        return R.ok();
    }

    // ========== 字典项 ==========

    @Operation(summary = "创建字典项")
    @PostMapping("/item")
    public R<DictItemResponse> createItem(@Valid @RequestBody DictItemCreateRequest request) {
        return R.ok(dictService.createItem(request));
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/item/{id}")
    public R<DictItemResponse> updateItem(@Parameter(description = "字典项ID") @PathVariable Long id,
                                          @Valid @RequestBody DictItemUpdateRequest request) {
        return R.ok(dictService.updateItem(id, request));
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/item/{id}")
    public R<Void> deleteItem(@Parameter(description = "字典项ID") @PathVariable Long id) {
        dictService.deleteItem(id);
        return R.ok();
    }

    @Operation(summary = "查询字典项详情")
    @GetMapping("/item/{id}")
    public R<DictItemResponse> getItemById(@Parameter(description = "字典项ID") @PathVariable Long id) {
        return R.ok(dictService.getItemById(id));
    }

    @Operation(summary = "分页查询字典项")
    @GetMapping("/item")
    public R<Page<DictItemResponse>> listItem(DictItemQueryRequest request) {
        return R.ok(dictService.listItem(request));
    }

    @Operation(summary = "更新字典项状态")
    @PutMapping("/item/{id}/status")
    public R<Void> updateItemStatus(@Parameter(description = "字典项ID") @PathVariable Long id,
                                    @Parameter(description = "状态") @RequestParam String status) {
        dictService.updateItemStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "批量更新字典项排序")
    @PutMapping("/item/sort")
    public R<Void> batchUpdateItemSort(@Parameter(description = "字典项ID列表") @RequestParam List<Long> ids,
                                       @Parameter(description = "排序号列表") @RequestParam List<Integer> sorts) {
        dictService.batchUpdateItemSort(ids, sorts);
        return R.ok();
    }

    // ========== 字典翻译 ==========

    @Operation(summary = "获取字典类型下所有翻译项")
    @GetMapping("/translation/{dictType}")
    public R<DictTranslationResponse> translateByType(
            @Parameter(description = "字典类型编码") @PathVariable String dictType) {
        return R.ok(dictService.translateByType(dictType));
    }

    @Operation(summary = "字典项编码翻译为显示名")
    @GetMapping("/translation/{dictType}/code/{dictCode}")
    public R<String> translateCode(
            @Parameter(description = "字典类型编码") @PathVariable String dictType,
            @Parameter(description = "字典项编码") @PathVariable String dictCode) {
        return R.ok(dictService.translateCode(dictType, dictCode));
    }

    @Operation(summary = "字典项值翻译为显示名")
    @GetMapping("/translation/{dictType}/value/{dictValue}")
    public R<String> translateValue(
            @Parameter(description = "字典类型编码") @PathVariable String dictType,
            @Parameter(description = "字典项值") @PathVariable String dictValue) {
        return R.ok(dictService.translateValue(dictType, dictValue));
    }
}
