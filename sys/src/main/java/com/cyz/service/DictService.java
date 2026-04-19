package com.cyz.service;

import com.cyz.dto.request.DictItemCreateRequest;
import com.cyz.dto.request.DictItemQueryRequest;
import com.cyz.dto.request.DictItemUpdateRequest;
import com.cyz.dto.request.DictTypeCreateRequest;
import com.cyz.dto.request.DictTypeQueryRequest;
import com.cyz.dto.request.DictTypeUpdateRequest;
import com.cyz.dto.response.DictItemResponse;
import com.cyz.dto.response.DictTranslationResponse;
import com.cyz.dto.response.DictTypeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DictService {

    // ---- 字典类型 ----

    DictTypeResponse createType(DictTypeCreateRequest request);

    DictTypeResponse updateType(Long id, DictTypeUpdateRequest request);

    void deleteType(Long id);

    DictTypeResponse getTypeById(Long id);

    Page<DictTypeResponse> listType(DictTypeQueryRequest request);

    void updateTypeStatus(Long id, String status);

    // ---- 字典项 ----

    DictItemResponse createItem(DictItemCreateRequest request);

    DictItemResponse updateItem(Long id, DictItemUpdateRequest request);

    void deleteItem(Long id);

    DictItemResponse getItemById(Long id);

    Page<DictItemResponse> listItem(DictItemQueryRequest request);

    void updateItemStatus(Long id, String status);

    void batchUpdateItemSort(List<Long> ids, List<Integer> sorts);

    // ---- 字典翻译 ----

    DictTranslationResponse translateByType(String dictType);

    String translateCode(String dictType, String dictCode);

    String translateValue(String dictType, String dictValue);

    String getDictValue(String dictType, String dictCode);
}
