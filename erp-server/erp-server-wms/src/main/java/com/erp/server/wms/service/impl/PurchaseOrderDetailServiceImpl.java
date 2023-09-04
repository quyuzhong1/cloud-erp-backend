package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.server.wms.mapper.PurchaseOrderDetailMapper;
import com.erp.server.wms.service.PurchaseOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Override
    public List<PurchaseOrderDetailEntity> ListProductDetailEntityByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return baseMapper.listByIds(ids);
    }

    @Override
    public Boolean saveOrUpdatePurchaseOrderDetail(List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList) {
        List<String> detailIds = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> detailEntityList = ListProductDetailEntityByIds(detailIds);
        List<String> ids = purchaseOrderDetailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(existIdList)) {
            List<PurchaseOrderDetailEntity> existDetailEntityList = ListProductDetailEntityByIds(existIdList);
            if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
                baseMapper.updateBatchSelective(existDetailEntityList);
            }
        }
        List<String> notExistIdList = detailIds.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
/*        List<PurchaseOrderDetailEntity> notExistDetailEntityList = new ArrayList<>();
        List<PurchaseOrderDetailEntity> existDetailEntityList = new ArrayList<>();*/
        for (PurchaseOrderDetailEntity detailEntity : purchaseOrderDetailEntityList) {
            if (notExistIdList.contains(detailEntity.getId())) {
//                notExistDetailEntityList.add(detailEntity);
                this.save(detailEntity);
            }
            if (existIdList.contains(detailEntity.getId())) {
//                existDetailEntityList.add(detailEntity);
                this.saveOrUpdate(detailEntity);
            }
        }
/*        if (CollectionUtils.isNotEmpty(notExistDetailEntityList)) {
            this.saveBatch(notExistDetailEntityList);
        }
        if (CollectionUtils.isNotEmpty(existDetailEntityList)) {
            baseMapper.updateBatchSelective(existDetailEntityList);
        }*/
        return Boolean.TRUE;
    }
}
