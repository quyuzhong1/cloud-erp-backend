package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
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

    @Override
    public void add(PurchaseOrderSupplierDTO.AddDTO dto) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto,entity);
        this.save(entity);
    }

    @Override
    public void update(PurchaseOrderSupplierDTO.UpdateDTO dto) {
        if (ObjectUtils.isEmpty(dto)) {
            return;
        }
        PurchaseOrderSupplierEntity entity = new PurchaseOrderSupplierEntity();
        BeanMapperUtils.copy(dto,entity);
        this.updateById(entity);
    }

}
