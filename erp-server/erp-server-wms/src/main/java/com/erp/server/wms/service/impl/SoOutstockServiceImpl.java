package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.oms.entity.SoOutstockEntity;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.SoOutstockService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().in(SoOutstockEntity::getSourceId, ids).list();
    }
}
