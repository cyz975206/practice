package com.cyz.seal.org.application;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyz.seal.org.domain.LegalEntity;
import com.cyz.seal.org.interfaces.dto.LegalEntityCreateRequest;

/**
 * 法人实体应用服务。
 */
public interface LegalEntityService extends IService<LegalEntity> {

    /** 创建法人实体（编码唯一性校验）。 */
    LegalEntity create(LegalEntityCreateRequest req);
}
