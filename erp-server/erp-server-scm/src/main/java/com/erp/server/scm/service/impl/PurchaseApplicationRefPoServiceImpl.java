package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import com.erp.server.scm.mapper.PurchaseApplicationRefPoMapper;
import com.erp.server.scm.service.PurchaseApplicationRefPoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 采购申请单和采购订单关联表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseApplicationRefPoServiceImpl extends SuperServiceImpl<PurchaseApplicationRefPoMapper, PurchaseApplicationRefPoEntity> implements PurchaseApplicationRefPoService {

    @Override
    public List<PurchaseApplicationRefPoDTO.ListDTO> list(PurchaseApplicationRefPoDTO.SearchParamDTO dto) {
        return  baseMapper.list(dto);
    }

    @Override
    public List<PurchaseApplicationRefPoEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        if (CollectionUtils.isEmpty(purchaseOrderIds)) {
            return new ArrayList<>();
        }
        return lambdaQuery().in(PurchaseApplicationRefPoEntity::getPurchaseOrderId,purchaseOrderIds).list();
    }

    @Override
    public void removeByPurchaseOrderIds(List<String> purchaseOrderIds) {
        lambdaUpdate().in(PurchaseApplicationRefPoEntity::getPurchaseOrderId,purchaseOrderIds).remove();
    }

}
