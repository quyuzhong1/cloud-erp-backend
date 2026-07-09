package com.erp.server.dmp.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpFbaInboundPlansEntity;
import com.erp.server.dmp.mapper.DmpFbaInboundPlansMapper;
import com.erp.server.dmp.service.DmpFbaInboundPlansService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * FBA InboundPlans 列表表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class DmpFbaInboundPlansServiceImpl extends SuperServiceImpl<DmpFbaInboundPlansMapper, DmpFbaInboundPlansEntity>
        implements DmpFbaInboundPlansService {

}

