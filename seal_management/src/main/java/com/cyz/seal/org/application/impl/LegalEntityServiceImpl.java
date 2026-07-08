package com.cyz.seal.org.application.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.org.application.LegalEntityService;
import com.cyz.seal.org.domain.LegalEntity;
import com.cyz.seal.org.infrastructure.persistence.mapper.LegalEntityMapper;
import com.cyz.seal.org.interfaces.dto.LegalEntityCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegalEntityServiceImpl extends ServiceImpl<LegalEntityMapper, LegalEntity> implements LegalEntityService {

    @Override
    @Transactional
    public LegalEntity create(LegalEntityCreateRequest req) {
        if (lambdaQuery().eq(LegalEntity::getCode, req.code()).exists()) {
            throw new BusinessException("法人实体编码已存在: " + req.code());
        }
        LegalEntity entity = new LegalEntity();
        entity.setCode(req.code());
        entity.setFullName(req.fullName());
        entity.setShortName(req.shortName());
        entity.setEntityType(req.entityType());
        entity.setStatus(1);
        save(entity);
        return entity;
    }
}
