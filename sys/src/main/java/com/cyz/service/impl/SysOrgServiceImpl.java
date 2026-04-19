package com.cyz.service.impl;

import com.cyz.common.exception.BizException;
import com.cyz.common.util.ExcelUtil;
import com.cyz.converter.OrgConverter;
import com.cyz.converter.OrgExcelRowMapper;
import com.cyz.dto.request.OrgCreateRequest;
import com.cyz.dto.request.OrgQueryRequest;
import com.cyz.dto.request.OrgUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.OrgResponse;
import com.cyz.dto.response.OrgTreeResponse;
import com.cyz.entity.SysOrg;
import com.cyz.repository.SysOrgRepository;
import com.cyz.service.SysOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysOrgServiceImpl implements SysOrgService {

    private final SysOrgRepository orgRepository;

    @Override
    @Transactional
    public OrgResponse create(OrgCreateRequest request) {
        // 校验上级机构
        if (request.getParentOrgCode() != null && !request.getParentOrgCode().isEmpty()) {
            orgRepository.findByOrgCodeAndIsDeletedFalse(request.getParentOrgCode())
                    .orElseThrow(() -> new BizException("上级机构不存在: " + request.getParentOrgCode()));
        }

        // 生成唯一机构号
        String orgCode = generateUniqueOrgCode();
        SysOrg org = OrgConverter.toEntity(request, orgCode);
        SysOrg saved = orgRepository.save(org);
        return OrgConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public OrgResponse update(Long id, OrgUpdateRequest request) {
        SysOrg org = orgRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("机构不存在: " + id));

        if (request.getParentOrgCode() != null && !request.getParentOrgCode().isEmpty()) {
            if (!request.getParentOrgCode().equals(org.getOrgCode())) {
                orgRepository.findByOrgCodeAndIsDeletedFalse(request.getParentOrgCode())
                        .orElseThrow(() -> new BizException("上级机构不存在: " + request.getParentOrgCode()));
            }
        }

        OrgConverter.updateEntity(org, request);
        SysOrg saved = orgRepository.save(org);
        return OrgConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysOrg org = orgRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("机构不存在: " + id));
        org.setIsDeleted(true);
        orgRepository.save(org);
    }

    @Override
    public OrgResponse getById(Long id) {
        SysOrg org = orgRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("机构不存在: " + id));
        return OrgConverter.toResponse(org);
    }

    @Override
    public Page<OrgResponse> list(OrgQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysOrg> page = orgRepository.findByConditions(
                request.getOrgShortName(), request.getOrgLevel(),
                request.getStatus(), request.getParentOrgCode(), pageRequest);
        return page.map(OrgConverter::toResponse);
    }

    @Override
    public List<OrgTreeResponse> getTree() {
        List<SysOrg> allOrgs = orgRepository.findAllActiveOrderBySort();
        return OrgConverter.buildTree(allOrgs);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        SysOrg org = orgRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("机构不存在: " + id));
        org.setStatus(status);
        orgRepository.save(org);
    }

    @Override
    @Transactional
    public void batchUpdateSort(List<Long> ids, List<Integer> sorts) {
        if (ids == null || sorts == null || ids.size() != sorts.size()) {
            throw new BizException("参数错误：ids和sorts长度不一致");
        }
        for (int i = 0; i < ids.size(); i++) {
            final Long orgId = ids.get(i);
            final Integer sortVal = sorts.get(i);
            SysOrg org = orgRepository.findByIdAndIsDeletedFalse(orgId)
                    .orElseThrow(() -> new BizException("机构不存在: " + orgId));
            org.setSort(sortVal);
            orgRepository.save(org);
        }
    }

    @Override
    public byte[] exportExcel() {
        List<SysOrg> allOrgs = orgRepository.findByIsDeletedFalseOrderByOrgCode();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ExcelUtil.write(baos, "机构数据", OrgExcelRowMapper.getHeaders(),
                    allOrgs, OrgExcelRowMapper::toRow);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BizException("导出Excel失败");
        }
    }

    @Override
    @Transactional
    public ImportResultResponse importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        Set<String> existingCodes = new HashSet<>(orgRepository.findAllOrgCodes());
        List<SysOrg> toSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCount = 0;

        try {
            List<SysOrg> orgs = ExcelUtil.read(file.getInputStream(), 0, 1, OrgExcelRowMapper::fromRow);
            totalCount = orgs.size();

            for (int i = 0; i < orgs.size(); i++) {
                SysOrg org = orgs.get(i);
                if (org.getOrgCode() == null || org.getOrgCode().isEmpty()) {
                    errors.add("第" + (i + 2) + "行：机构号为空");
                    continue;
                }
                if (existingCodes.contains(org.getOrgCode())) {
                    errors.add("第" + (i + 2) + "行：机构号已存在: " + org.getOrgCode());
                    continue;
                }
                existingCodes.add(org.getOrgCode());
                toSave.add(org);
            }

            orgRepository.saveAll(toSave);
        } catch (IOException e) {
            throw new BizException("读取Excel文件失败");
        }

        return ImportResultResponse.builder()
                .totalCount(totalCount)
                .successCount(toSave.size())
                .failCount(totalCount - toSave.size())
                .errors(errors)
                .build();
    }

    private String generateUniqueOrgCode() {
        String code;
        int maxAttempts = 100;
        int attempt = 0;
        do {
            code = "ORG" + System.currentTimeMillis() + String.format("%04d", attempt);
            attempt++;
        } while (orgRepository.existsByOrgCodeAndIsDeletedFalse(code) && attempt < maxAttempts);
        return code;
    }
}
