package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.converter.MenuConverter;
import com.cyz.dto.request.MenuCreateRequest;
import com.cyz.dto.request.MenuQueryRequest;
import com.cyz.dto.request.MenuUpdateRequest;
import com.cyz.dto.response.MenuResponse;
import com.cyz.dto.response.MenuTreeResponse;
import com.cyz.entity.SysMenu;
import com.cyz.entity.SysRoleMenu;
import com.cyz.repository.SysConfigRepository;
import com.cyz.repository.SysMenuRepository;
import com.cyz.repository.SysRoleMenuRepository;
import com.cyz.service.SysMenuService;
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
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final MenuConverter menuConverter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysConfigRepository configRepository;

    @Override
    @Transactional
    public MenuResponse create(MenuCreateRequest request) {
        if (menuRepository.existsByMenuCodeAndIsDeletedFalse(request.getMenuCode())) {
            throw new BizException("菜单编码已存在: " + request.getMenuCode());
        }

        if (request.getParentId() != null) {
            menuRepository.findByIdAndIsDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new BizException("父菜单不存在: " + request.getParentId()));
        }

        SysMenu menu = menuConverter.toEntity(request);
        SysMenu saved = menuRepository.save(menu);

        evictMenuTreeCache();

        return menuConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public MenuResponse update(Long id, MenuUpdateRequest request) {
        SysMenu menu = menuRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("菜单不存在: " + id));

        if (request.getParentId() != null) {
            menuRepository.findByIdAndIsDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new BizException("父菜单不存在: " + request.getParentId()));
        }

        menuConverter.updateEntity(menu, request);
        SysMenu saved = menuRepository.save(menu);

        evictMenuTreeCache();

        return menuConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysMenu menu = menuRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("菜单不存在: " + id));

        menu.setIsDeleted(true);
        menuRepository.save(menu);

        List<SysRoleMenu> roleMenus = roleMenuRepository.findByMenuIdAndIsDeletedFalse(id);
        roleMenus.forEach(rm -> rm.setIsDeleted(true));
        if (!roleMenus.isEmpty()) {
            roleMenuRepository.saveAll(roleMenus);
        }

        evictMenuTreeCache();
    }

    @Override
    public MenuResponse getById(Long id) {
        SysMenu menu = menuRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("菜单不存在: " + id));
        return menuConverter.toResponse(menu);
    }

    @Override
    public Page<MenuResponse> list(MenuQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysMenu> page = menuRepository.findByConditions(
                request.getMenuName(), request.getMenuType(),
                request.getStatus(), pageRequest);
        return page.map(menuConverter::toResponse);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuTreeResponse> getTree() {
        Object cached = redisTemplate.opsForValue().get(CacheConstant.MENU_TREE);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            try {
                return (List<MenuTreeResponse>) list;
            } catch (ClassCastException ignored) {
            }
        }

        List<SysMenu> allMenus = menuRepository.findAllActiveOrderBySort();
        List<MenuTreeResponse> tree = menuConverter.buildTree(allMenus);

        long expireMinutes = getCacheExpireMinutes();
        redisTemplate.opsForValue().set(CacheConstant.MENU_TREE, tree, expireMinutes, TimeUnit.MINUTES);

        return tree;
    }

    private void evictMenuTreeCache() {
        redisTemplate.delete(CacheConstant.MENU_TREE);
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
