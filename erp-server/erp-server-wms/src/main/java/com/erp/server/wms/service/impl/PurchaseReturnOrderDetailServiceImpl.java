package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.server.wms.mapper.PurchaseReturnOrderDetailMapper;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 采购退货单明细 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Service
public class PurchaseReturnOrderDetailServiceImpl extends SuperServiceImpl<PurchaseReturnOrderDetailMapper, PurchaseReturnOrderDetailEntity> implements PurchaseReturnOrderDetailService {

    @Override
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return lambdaQuery().in(PurchaseReturnOrderDetailEntity::getSourceDetailId,sourceDetailIds).list();
    }
}
