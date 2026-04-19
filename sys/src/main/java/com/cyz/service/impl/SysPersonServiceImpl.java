package com.cyz.service.impl;

import com.cyz.common.exception.BizException;
import com.cyz.common.util.ExcelUtil;
import com.cyz.common.util.Sm4Util;
import com.cyz.config.Sm4Properties;
import com.cyz.converter.PersonConverter;
import com.cyz.converter.PersonExcelRowMapper;
import com.cyz.dto.request.PersonCreateRequest;
import com.cyz.dto.request.PersonQueryRequest;
import com.cyz.dto.request.PersonUpdateRequest;
import com.cyz.dto.response.ImportResultResponse;
import com.cyz.dto.response.PersonResponse;
import com.cyz.entity.SysOrg;
import com.cyz.entity.SysPerson;
import com.cyz.repository.SysOrgRepository;
import com.cyz.repository.SysPersonRepository;
import com.cyz.service.SysPersonService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPersonServiceImpl implements SysPersonService {

    private final SysPersonRepository personRepository;
    private final SysOrgRepository orgRepository;
    private final Sm4Properties sm4Properties;
    private final PersonConverter personConverter;

    @PostConstruct
    public void init() {
        PersonExcelRowMapper.setSm4Key(sm4Properties.getKey());
    }

    @Override
    @Transactional
    public PersonResponse create(PersonCreateRequest request) {
        orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                .orElseThrow(() -> new BizException("所属机构不存在: " + request.getOrgCode()));

        if (personRepository.existsByStaffNumAndIsDeletedFalse(request.getStaffNum())) {
            throw new BizException("员工工号已存在: " + request.getStaffNum());
        }

        String encIdCard = Sm4Util.encrypt(request.getIdCard(), sm4Properties.getKey());
        if (personRepository.existsByIdCardAndIsDeletedFalse(encIdCard)) {
            throw new BizException("身份证号码已存在");
        }

        SysPerson person = personConverter.toEntity(request);
        SysPerson saved = personRepository.save(person);
        String orgName = orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                .map(SysOrg::getOrgShortName).orElse(null);
        return personConverter.toResponse(saved, orgName);
    }

    @Override
    @Transactional
    public PersonResponse update(Long id, PersonUpdateRequest request) {
        SysPerson person = personRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("人员不存在: " + id));

        if (request.getOrgCode() != null) {
            orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                    .orElseThrow(() -> new BizException("所属机构不存在: " + request.getOrgCode()));
        }

        if (request.getStaffNum() != null && !request.getStaffNum().equals(person.getStaffNum())) {
            if (personRepository.existsByStaffNumAndIsDeletedFalse(request.getStaffNum())) {
                throw new BizException("员工工号已存在: " + request.getStaffNum());
            }
        }

        personConverter.updateEntity(person, request);
        SysPerson saved = personRepository.save(person);
        String orgCode = person.getOrgCode();
        String orgName = orgCode != null ? orgRepository.findByOrgCodeAndIsDeletedFalse(orgCode)
                .map(SysOrg::getOrgShortName).orElse(null) : null;
        return personConverter.toResponse(saved, orgName);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysPerson person = personRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("人员不存在: " + id));
        person.setIsDeleted(true);
        personRepository.save(person);
    }

    @Override
    public PersonResponse getById(Long id) {
        SysPerson person = personRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("人员不存在: " + id));
        String orgName = person.getOrgCode() != null ? orgRepository.findByOrgCodeAndIsDeletedFalse(person.getOrgCode())
                .map(SysOrg::getOrgShortName).orElse(null) : null;
        return personConverter.toResponse(person, orgName);
    }

    @Override
    public Page<PersonResponse> list(PersonQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysPerson> page = personRepository.findByConditions(
                request.getFullName(), request.getStaffNum(),
                request.getOrgCode(), request.getStatus(), pageRequest);

        List<SysPerson> persons = page.getContent();
        if (persons.isEmpty()) {
            return page.map(p -> personConverter.toResponse(p, null));
        }

        // 批量查询机构名称
        List<String> orgCodes = persons.stream()
                .map(SysPerson::getOrgCode)
                .filter(c -> c != null)
                .distinct()
                .toList();
        Map<String, String> orgCodeToName = orgCodes.isEmpty()
                ? Collections.emptyMap()
                : orgRepository.findByOrgCodeInAndIsDeletedFalse(orgCodes).stream()
                        .collect(Collectors.toMap(SysOrg::getOrgCode, SysOrg::getOrgShortName));

        List<PersonResponse> responses = persons.stream()
                .map(p -> personConverter.toResponse(p, orgCodeToName.get(p.getOrgCode())))
                .toList();

        return new PageImpl<>(responses, pageRequest, page.getTotalElements());
    }

    @Override
    public byte[] exportExcel() {
        List<SysPerson> allPersons = personRepository.findByIsDeletedFalse(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ExcelUtil.write(baos, "人员数据", PersonExcelRowMapper.getHeaders(),
                    allPersons, PersonExcelRowMapper::toRow);
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

        Set<String> existingStaffNums = new HashSet<>(personRepository.findAllStaffNums());
        Set<String> existingIdCards = new HashSet<>(personRepository.findAllIdCards());

        List<SysPerson> toSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalCount = 0;

        try {
            List<SysPerson> persons = ExcelUtil.read(file.getInputStream(), 0, 1, PersonExcelRowMapper::fromRow);
            totalCount = persons.size();

            for (int i = 0; i < persons.size(); i++) {
                SysPerson person = persons.get(i);
                if (person.getStaffNum() == null || person.getStaffNum().isEmpty()) {
                    errors.add("第" + (i + 2) + "行：员工工号为空");
                    continue;
                }
                if (existingStaffNums.contains(person.getStaffNum())) {
                    errors.add("第" + (i + 2) + "行：员工工号已存在: " + person.getStaffNum());
                    continue;
                }
                if (person.getIdCard() != null && existingIdCards.contains(person.getIdCard())) {
                    errors.add("第" + (i + 2) + "行：身份证号码已存在");
                    continue;
                }
                existingStaffNums.add(person.getStaffNum());
                if (person.getIdCard() != null) {
                    existingIdCards.add(person.getIdCard());
                }
                toSave.add(person);
            }

            personRepository.saveAll(toSave);
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
}
