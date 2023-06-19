package com.erp.server.wms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.wms.mapper.PurchaseOrderDetailMapper;
import com.erp.server.wms.service.PurchaseOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Override
    public Boolean saveOrUpdatePurchaseOrderDetail(List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList) {
        List<String> detailIds = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> detailEntityList = this.listByIds(detailIds);
        List<String> ids = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> existDetailEntityList = this.listByIds(existIdList);
        List<PurchaseOrderDetailEntity> notExistDetailEntityList = this.listByIds(notExistIdList);
        if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
            this.updateBatchById(existDetailEntityList);
        }
        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
            this.saveBatch(notExistDetailEntityList);
        }
        return Boolean.TRUE;
    }
}
