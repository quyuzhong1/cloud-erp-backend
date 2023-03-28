package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.server.scm.mapper.PurchaseOrderSupplierMapper;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderSupplierServiceImpl extends SuperServiceImpl<PurchaseOrderSupplierMapper, PurchaseOrderSupplierEntity> implements PurchaseOrderSupplierService {

    @Override
    public void deleteByPurchaseOrderIds(List<String> purchaseOrderIds) {
        lambdaUpdate().in(PurchaseOrderSupplierEntity::getPurchaseOrderId,purchaseOrderIds).remove();
    }

    @Override
    public PurchaseOrderSupplierEntity listByPurchaseOrderId(String purchaseOrderId) {
        return  lambdaQuery().eq(PurchaseOrderSupplierEntity::getPurchaseOrderId,purchaseOrderId).one();
    }
}
