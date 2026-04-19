package com.cyz.converter;

import com.cyz.common.util.PinyinUtil;
import com.cyz.common.util.Sm4Util;
import com.cyz.config.Sm4Properties;
import com.cyz.dto.request.PersonCreateRequest;
import com.cyz.dto.request.PersonUpdateRequest;
import com.cyz.dto.response.PersonResponse;
import com.cyz.entity.SysPerson;
import org.springframework.stereotype.Component;

@Component
public class PersonConverter {

    private final Sm4Properties sm4Properties;

    public PersonConverter(Sm4Properties sm4Properties) {
        this.sm4Properties = sm4Properties;
    }

    public SysPerson toEntity(PersonCreateRequest request) {
        String fullName = request.getSurname() + request.getGivenName();
        return SysPerson.builder()
                .surname(request.getSurname())
                .surnamePinyin(PinyinUtil.toPinyin(request.getSurname()))
                .givenName(request.getGivenName())
                .givenNamePinyin(PinyinUtil.toPinyin(request.getGivenName()))
                .fullName(fullName)
                .fullNamePinyin(PinyinUtil.toPinyin(fullName))
                .idCard(Sm4Util.encrypt(request.getIdCard(), sm4Properties.getKey()))
                .phone(Sm4Util.encrypt(request.getPhone(), sm4Properties.getKey()))
                .staffNum(request.getStaffNum())
                .orgCode(request.getOrgCode())
                .status(request.getStatus() != null ? request.getStatus() : "1")
                .build();
    }

    public void updateEntity(SysPerson entity, PersonUpdateRequest request) {
        if (request.getSurname() != null) {
            entity.setSurname(request.getSurname());
            entity.setSurnamePinyin(PinyinUtil.toPinyin(request.getSurname()));
        }
        if (request.getGivenName() != null) {
            entity.setGivenName(request.getGivenName());
            entity.setGivenNamePinyin(PinyinUtil.toPinyin(request.getGivenName()));
        }
        if (request.getSurname() != null || request.getGivenName() != null) {
            String fullName = (request.getSurname() != null ? request.getSurname() : entity.getSurname())
                    + (request.getGivenName() != null ? request.getGivenName() : entity.getGivenName());
            entity.setFullName(fullName);
            entity.setFullNamePinyin(PinyinUtil.toPinyin(fullName));
        }
        if (request.getIdCard() != null) {
            entity.setIdCard(Sm4Util.encrypt(request.getIdCard(), sm4Properties.getKey()));
        }
        if (request.getPhone() != null) {
            entity.setPhone(Sm4Util.encrypt(request.getPhone(), sm4Properties.getKey()));
        }
        if (request.getStaffNum() != null) {
            entity.setStaffNum(request.getStaffNum());
        }
        if (request.getOrgCode() != null) {
            entity.setOrgCode(request.getOrgCode());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    public PersonResponse toResponse(SysPerson entity) {
        return toResponse(entity, null);
    }

    public PersonResponse toResponse(SysPerson entity, String orgName) {
        PersonResponse response = new PersonResponse();
        response.setId(entity.getId());
        response.setSurname(entity.getSurname());
        response.setSurnamePinyin(entity.getSurnamePinyin());
        response.setGivenName(entity.getGivenName());
        response.setGivenNamePinyin(entity.getGivenNamePinyin());
        response.setFullName(entity.getFullName());
        response.setFullNamePinyin(entity.getFullNamePinyin());
        response.setIdCard(Sm4Util.decrypt(entity.getIdCard(), sm4Properties.getKey()));
        response.setPhone(Sm4Util.decrypt(entity.getPhone(), sm4Properties.getKey()));
        response.setStaffNum(entity.getStaffNum());
        response.setOrgCode(entity.getOrgCode());
        response.setOrgName(orgName);
        response.setStatus(entity.getStatus());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }
}
