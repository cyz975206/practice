package com.cyz.service;

import com.cyz.dto.request.PersonCreateRequest;
import com.cyz.dto.request.PersonQueryRequest;
import com.cyz.dto.request.PersonUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.PersonResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface SysPersonService {

    PersonResponse create(PersonCreateRequest request);

    PersonResponse update(Long id, PersonUpdateRequest request);

    void delete(Long id);

    PersonResponse getById(Long id);

    Page<PersonResponse> list(PersonQueryRequest request);

    byte[] exportExcel();

    ImportResultResponse importExcel(MultipartFile file);
}
