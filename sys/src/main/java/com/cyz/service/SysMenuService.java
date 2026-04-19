package com.cyz.service;

import com.cyz.dto.request.MenuCreateRequest;
import com.cyz.dto.request.MenuQueryRequest;
import com.cyz.dto.request.MenuUpdateRequest;
import com.cyz.dto.response.MenuResponse;
import com.cyz.dto.response.MenuTreeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SysMenuService {

    MenuResponse create(MenuCreateRequest request);

    MenuResponse update(Long id, MenuUpdateRequest request);

    void delete(Long id);

    MenuResponse getById(Long id);

    Page<MenuResponse> list(MenuQueryRequest request);

    List<MenuTreeResponse> getTree();
}
