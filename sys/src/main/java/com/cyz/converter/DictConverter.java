package com.cyz.converter;

import com.cyz.dto.request.DictItemCreateRequest;
import com.cyz.dto.request.DictItemUpdateRequest;
import com.cyz.dto.request.DictTypeCreateRequest;
import com.cyz.dto.request.DictTypeUpdateRequest;
import com.cyz.dto.response.DictItemResponse;
import com.cyz.dto.response.DictTypeResponse;
import com.cyz.dto.response.DictTranslationResponse;
import com.cyz.entity.SysDict;

import java.util.List;
import java.util.stream.Collectors;

public class DictConverter {

    // ---- 类型头转换 ----

    public static SysDict toTypeEntity(DictTypeCreateRequest request) {
        return SysDict.builder()
                .dictType(request.getDictType())
                .dictName(request.getDictName())
                .status(request.getStatus() != null ? request.getStatus() : "ENABLED")
                .remark(request.getRemark())
                .build();
    }

    public static void updateTypeEntity(SysDict entity, DictTypeUpdateRequest request) {
        if (request.getDictName() != null) {
            entity.setDictName(request.getDictName());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }
    }

    public static DictTypeResponse toTypeResponse(SysDict entity) {
        DictTypeResponse response = new DictTypeResponse();
        response.setId(entity.getId());
        response.setDictType(entity.getDictType());
        response.setDictName(entity.getDictName());
        response.setStatus(entity.getStatus());
        response.setRemark(entity.getRemark());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    // ---- 字典项转换 ----

    public static SysDict toItemEntity(DictItemCreateRequest request) {
        return SysDict.builder()
                .dictType(request.getDictType())
                .dictCode(request.getDictCode())
                .dictLabel(request.getDictLabel())
                .dictValue(request.getDictValue())
                .sort(request.getSort() != null ? request.getSort() : 0)
                .status(request.getStatus() != null ? request.getStatus() : "ENABLED")
                .remark(request.getRemark())
                .build();
    }

    public static void updateItemEntity(SysDict entity, DictItemUpdateRequest request) {
        if (request.getDictCode() != null) {
            entity.setDictCode(request.getDictCode());
        }
        if (request.getDictLabel() != null) {
            entity.setDictLabel(request.getDictLabel());
        }
        if (request.getDictValue() != null) {
            entity.setDictValue(request.getDictValue());
        }
        if (request.getSort() != null) {
            entity.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }
    }

    public static DictItemResponse toItemResponse(SysDict entity) {
        DictItemResponse response = new DictItemResponse();
        response.setId(entity.getId());
        response.setDictType(entity.getDictType());
        response.setDictCode(entity.getDictCode());
        response.setDictLabel(entity.getDictLabel());
        response.setDictValue(entity.getDictValue());
        response.setSort(entity.getSort());
        response.setStatus(entity.getStatus());
        response.setRemark(entity.getRemark());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    // ---- 翻译转换 ----

    public static DictTranslationResponse toTranslationResponse(String dictType, List<SysDict> items) {
        List<DictTranslationResponse.TranslationItem> translationItems = items.stream()
                .map(item -> DictTranslationResponse.TranslationItem.builder()
                        .code(item.getDictCode())
                        .label(item.getDictLabel())
                        .value(item.getDictValue())
                        .build())
                .collect(Collectors.toList());

        return DictTranslationResponse.builder()
                .dictType(dictType)
                .translations(translationItems)
                .build();
    }
}
