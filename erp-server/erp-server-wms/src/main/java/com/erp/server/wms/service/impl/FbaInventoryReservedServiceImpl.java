package com.erp.server.wms.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import com.erp.server.wms.mapper.FbaInventoryReservedMapper;
import com.erp.server.wms.service.FbaInventoryReservedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * FBA库存预留信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Slf4j
@Service
public class FbaInventoryReservedServiceImpl extends SuperServiceImpl<FbaInventoryReservedMapper, FbaInventoryReservedEntity> implements FbaInventoryReservedService {

    @Override
    public List<FbaInventoryReservedEntity> listByMainId(String main) {
        return lambdaQuery().eq(FbaInventoryReservedEntity::getMainId, main).list();
    }
}
