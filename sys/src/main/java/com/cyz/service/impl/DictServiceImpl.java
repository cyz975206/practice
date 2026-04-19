package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.converter.DictConverter;
import com.cyz.dto.request.DictItemCreateRequest;
import com.cyz.dto.request.DictItemQueryRequest;
import com.cyz.dto.request.DictItemUpdateRequest;
import com.cyz.dto.request.DictTypeCreateRequest;
import com.cyz.dto.request.DictTypeQueryRequest;
import com.cyz.dto.request.DictTypeUpdateRequest;
import com.cyz.dto.response.DictItemResponse;
import com.cyz.dto.response.DictTranslationResponse;
import com.cyz.dto.response.DictTypeResponse;
import com.cyz.entity.SysDict;
import com.cyz.repository.SysConfigRepository;
import com.cyz.repository.SysDictRepository;
import com.cyz.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictRepository dictRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigRepository configRepository;

    // ========== 字典类型 ==========

    @Override
    @Transactional
    public DictTypeResponse createType(DictTypeCreateRequest request) {
        if (dictRepository.existsByDictTypeAndDictCodeIsNullAndIsDeletedFalse(request.getDictType())) {
            throw new BizException("字典类型编码已存在: " + request.getDictType());
        }

        SysDict dict = DictConverter.toTypeEntity(request);
        SysDict saved = dictRepository.save(dict);

        DictTypeResponse response = DictConverter.toTypeResponse(saved);
        response.setItemCount(0);
        return response;
    }

    @Override
    @Transactional
    public DictTypeResponse updateType(Long id, DictTypeUpdateRequest request) {
        SysDict dict = dictRepository.findByDictTypeAndDictCodeIsNullAndIsDeletedFalse(
                        dictRepository.findById(id)
                                .orElseThrow(() -> new BizException("字典类型不存在: " + id))
                                .getDictType())
                .orElseThrow(() -> new BizException("字典类型不存在: " + id));

        DictConverter.updateTypeEntity(dict, request);
        SysDict saved = dictRepository.save(dict);

        evictDictCache(saved.getDictType());

        DictTypeResponse response = DictConverter.toTypeResponse(saved);
        response.setItemCount((int) dictRepository.countByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(saved.getDictType()));
        return response;
    }

    @Override
    @Transactional
    public void deleteType(Long id) {
        SysDict dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典类型不存在: " + id));

        String dictType = dict.getDictType();

        List<SysDict> items = dictRepository.findByDictTypeAndDictCodeIsNotNullAndIsDeletedFalseOrderBySortAscIdAsc(dictType);
        items.forEach(item -> item.setIsDeleted(true));
        if (!items.isEmpty()) {
            dictRepository.saveAll(items);
        }

        dict.setIsDeleted(true);
        dictRepository.save(dict);

        evictDictCache(dictType);
    }

    @Override
    public DictTypeResponse getTypeById(Long id) {
        SysDict dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典类型不存在: " + id));

        if (dict.getDictCode() != null) {
            throw new BizException("该记录为字典项，非字典类型: " + id);
        }

        DictTypeResponse response = DictConverter.toTypeResponse(dict);
        response.setItemCount((int) dictRepository.countByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(dict.getDictType()));
        return response;
    }

    @Override
    public Page<DictTypeResponse> listType(DictTypeQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysDict> page = dictRepository.findTypeByConditions(
                request.getDictType(), request.getDictName(), request.getStatus(), pageRequest);

        return page.map(dict -> {
            DictTypeResponse response = DictConverter.toTypeResponse(dict);
            response.setItemCount((int) dictRepository.countByDictTypeAndDictCodeIsNotNullAndIsDeletedFalse(dict.getDictType()));
            return response;
        });
    }

    @Override
    @Transactional
    public void updateTypeStatus(Long id, String status) {
        SysDict dict = dictRepository.findById(id)
                .orElseThrow(() -> new BizException("字典类型不存在: " + id));

        if (dict.getDictCode() != null) {
            throw new BizException("该记录为字典项，非字典类型: " + id);
        }

        dict.setStatus(status);
        dictRepository.save(dict);

        evictDictCache(dict.getDictType());
    }

    // ========== 字典项 ==========

    @Override
    @Transactional
    public DictItemResponse createItem(DictItemCreateRequest request) {
        if (!dictRepository.existsByDictTypeAndDictCodeIsNullAndIsDeletedFalse(request.getDictType())) {
            throw new BizException("字典类型不存在: " + request.getDictType());
        }

        if (dictRepository.existsByDictTypeAndDictCodeAndIsDeletedFalse(request.getDictType(), request.getDictCode())) {
            throw new BizException("字典项编码已存在: " + request.getDictCode());
        }

        SysDict dict = DictConverter.toItemEntity(request);
        SysDict saved = dictRepository.save(dict);

        evictDictCache(request.getDictType());

        return DictConverter.toItemResponse(saved);
    }

    @Override
    @Transactional
    public DictItemResponse updateItem(Long id, DictItemUpdateRequest request) {
        SysDict dict = dictRepository.findByIdAndDictCodeIsNotNullAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("字典项不存在: " + id));

        if (request.getDictCode() != null && !request.getDictCode().equals(dict.getDictCode())) {
            if (dictRepository.existsByDictTypeAndDictCodeAndIsDeletedFalse(dict.getDictType(), request.getDictCode())) {
                throw new BizException("字典项编码已存在: " + request.getDictCode());
            }
        }

        DictConverter.updateItemEntity(dict, request);
        SysDict saved = dictRepository.save(dict);

        evictDictCache(dict.getDictType());

        return DictConverter.toItemResponse(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        SysDict dict = dictRepository.findByIdAndDictCodeIsNotNullAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("字典项不存在: " + id));
        dict.setIsDeleted(true);
        dictRepository.save(dict);

        evictDictCache(dict.getDictType());
    }

    @Override
    public DictItemResponse getItemById(Long id) {
        SysDict dict = dictRepository.findByIdAndDictCodeIsNotNullAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("字典项不存在: " + id));
        return DictConverter.toItemResponse(dict);
    }

    @Override
    public Page<DictItemResponse> listItem(DictItemQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysDict> page = dictRepository.findItemByConditions(
                request.getDictType(), request.getStatus(), pageRequest);
        return page.map(DictConverter::toItemResponse);
    }

    @Override
    @Transactional
    public void updateItemStatus(Long id, String status) {
        SysDict dict = dictRepository.findByIdAndDictCodeIsNotNullAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("字典项不存在: " + id));
        dict.setStatus(status);
        dictRepository.save(dict);

        evictDictCache(dict.getDictType());
    }

    @Override
    @Transactional
    public void batchUpdateItemSort(List<Long> ids, List<Integer> sorts) {
        if (ids == null || sorts == null || ids.size() != sorts.size()) {
            throw new BizException("参数错误：ids和sorts长度不一致");
        }
        String affectedDictType = null;
        for (int i = 0; i < ids.size(); i++) {
            final Long itemId = ids.get(i);
            final Integer sortVal = sorts.get(i);
            SysDict dict = dictRepository.findByIdAndDictCodeIsNotNullAndIsDeletedFalse(itemId)
                    .orElseThrow(() -> new BizException("字典项不存在: " + itemId));
            dict.setSort(sortVal);
            dictRepository.save(dict);
            affectedDictType = dict.getDictType();
        }
        if (affectedDictType != null) {
            evictDictCache(affectedDictType);
        }
    }

    // ========== 字典翻译 ==========

    @Override
    public DictTranslationResponse translateByType(String dictType) {
        String cacheKey = CacheConstant.DICT_PREFIX + dictType;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof DictTranslationResponse response) {
            return response;
        }

        List<SysDict> items = dictRepository.findAllItemsByType(dictType);
        DictTranslationResponse response = DictConverter.toTranslationResponse(dictType, items);

        long expireMinutes = getCacheExpireMinutes();
        redisTemplate.opsForValue().set(cacheKey, response, expireMinutes, TimeUnit.MINUTES);

        return response;
    }

    @Override
    public String translateCode(String dictType, String dictCode) {
        DictTranslationResponse translation = translateByType(dictType);
        if (translation == null || translation.getTranslations() == null) {
            return null;
        }
        return translation.getTranslations().stream()
                .filter(item -> dictCode.equals(item.getCode()))
                .map(DictTranslationResponse.TranslationItem::getLabel)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String translateValue(String dictType, String dictValue) {
        DictTranslationResponse translation = translateByType(dictType);
        if (translation == null || translation.getTranslations() == null) {
            return null;
        }
        return translation.getTranslations().stream()
                .filter(item -> dictValue != null && dictValue.equals(item.getValue()))
                .map(DictTranslationResponse.TranslationItem::getLabel)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getDictValue(String dictType, String dictCode) {
        DictTranslationResponse translation = translateByType(dictType);
        if (translation == null || translation.getTranslations() == null) {
            return null;
        }
        return translation.getTranslations().stream()
                .filter(item -> dictCode.equals(item.getCode()))
                .map(DictTranslationResponse.TranslationItem::getValue)
                .findFirst()
                .orElse(null);
    }

    private void evictDictCache(String dictType) {
        redisTemplate.delete(CacheConstant.DICT_PREFIX + dictType);
    }

    private long getCacheExpireMinutes() {
        int cacheExpireHour = getConfigIntDirect("cache_expire_hour", 1);
        int randomOffset = getConfigIntDirect("cache_random_offset_minute", 10);
        return cacheExpireHour * 60L + ThreadLocalRandom.current().nextLong(0, randomOffset + 1);
    }

    private int getConfigIntDirect(String key, int defaultValue) {
        return configRepository.findByConfigKeyAndIsDeletedFalse(key)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
