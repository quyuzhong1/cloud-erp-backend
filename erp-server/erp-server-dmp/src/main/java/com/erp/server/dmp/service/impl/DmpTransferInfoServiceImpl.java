package com.erp.server.dmp.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpTransferInfoEntity;
import com.erp.server.dmp.mapper.DmpTransferInfoMapper;
import com.erp.server.dmp.service.DmpTransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 直接调拨单 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-19
 */
@Slf4j
@Service
public class DmpTransferInfoServiceImpl extends SuperServiceImpl<DmpTransferInfoMapper, DmpTransferInfoEntity> implements DmpTransferInfoService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(DmpTransferInfoEntity entity) {

        // 1. 检查订单是否存在

        // 2. 检查库存变更是否需要更新

        // 3. 订单不存在
            // 不属于监控仓库，丢弃
            // 属于监控仓库，新增订单

        // 4. 订单存在
            // 不属于监控仓库，删除已有订单
            // 属于监控仓库，更新订单
        // 5. 

    }


}
