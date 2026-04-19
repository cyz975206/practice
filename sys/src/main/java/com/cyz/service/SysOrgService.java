package com.cyz.service;

import com.cyz.dto.request.OrgCreateRequest;
import com.cyz.dto.request.OrgQueryRequest;
import com.cyz.dto.request.OrgUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.OrgResponse;
import com.cyz.dto.response.OrgTreeResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SysOrgService {

    OrgResponse create(OrgCreateRequest request);

    OrgResponse update(Long id, OrgUpdateRequest request);

    void delete(Long id);

    OrgResponse getById(Long id);

    Page<OrgResponse> list(OrgQueryRequest request);

    List<OrgTreeResponse> getTree();

    void updateStatus(Long id, String status);

    void batchUpdateSort(List<Long> ids, List<Integer> sorts);

    byte[] exportExcel();

    ImportResultResponse importExcel(MultipartFile file);
}
