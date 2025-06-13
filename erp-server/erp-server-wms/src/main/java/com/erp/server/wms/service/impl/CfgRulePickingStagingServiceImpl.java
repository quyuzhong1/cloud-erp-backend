package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.erp.server.wms.mapper.CfgRulePickingStagingMapper;
import com.erp.server.wms.service.CfgRulePickingStagingService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 拣货暂存规则 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Service
public class CfgRulePickingStagingServiceImpl extends SuperServiceImpl<CfgRulePickingStagingMapper, CfgRulePickingStagingEntity> implements CfgRulePickingStagingService {

    @Override
    public CfgRulePickingStagingEntity getByWarehouseId(String warehouseId, String billType) {
        if (CharSequenceUtil.isBlank(warehouseId) || CharSequenceUtil.isBlank(billType)) {
            return null;
        }
        return this.lambdaQuery().eq(CfgRulePickingStagingEntity::getWarehouseId,warehouseId)
                .eq(CfgRulePickingStagingEntity::getBillType, billType).last(" limit 1 ").one();
    }
}
