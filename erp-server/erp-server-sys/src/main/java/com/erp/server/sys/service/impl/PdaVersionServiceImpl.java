package com.erp.server.sys.service.impl;

import com.erp.model.sys.entity.PdaVersionEntity;
import com.erp.server.sys.mapper.PdaVersionMapper;
import com.erp.server.sys.service.PdaVersionService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
 */
@Slf4j
@Service
public class PdaVersionServiceImpl extends SuperServiceImpl<PdaVersionMapper, PdaVersionEntity> implements PdaVersionService {

    @Override
    public PdaVersionEntity getPdaVersion() {
        return lambdaQuery().orderByDesc(PdaVersionEntity::getUpdateTime).last("LIMIT 1").one();
    }
}
