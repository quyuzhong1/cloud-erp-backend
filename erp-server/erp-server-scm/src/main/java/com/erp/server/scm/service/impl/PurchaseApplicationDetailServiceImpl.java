package com.erp.server.scm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseApplicationDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationDetailEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseApplicationDetailMapper;
import com.erp.server.scm.service.PurchaseApplicationDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseApplicationDetailServiceImpl extends SuperServiceImpl<PurchaseApplicationDetailMapper, PurchaseApplicationDetailEntity> implements PurchaseApplicationDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void add(List<PurchaseApplicationDetailDTO.AddDTO> details, String purchaseApplicationId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseApplicationDetailEntity> list = BeanMapperUtils.copyList(PurchaseApplicationDetailEntity.class, details);
        doOpHandleDataId(list,purchaseApplicationId);
        this.saveBatch(list);
    }

    @Override
    public void update(List<PurchaseApplicationDetailDTO.UpdateDTO> details, String purchaseApplicationId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseApplicationDetailEntity> oldList = this.listByPurchaseApplicationId(purchaseApplicationId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        List<PurchaseApplicationDetailEntity> newList = BeanMapperUtils.copyList(PurchaseApplicationDetailEntity.class, details);
        doOpHandleDataId(newList,purchaseApplicationId);
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<PurchaseApplicationDetailEntity> listByPurchaseApplicationId(String purchaseApplicationId) {
        return  lambdaQuery().eq(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationId).list();
    }

    @Override
    public void removeByPurchaseApplicationIds(List<String> purchaseApplicationIds) {
        lambdaUpdate().in(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationIds).remove();
    }

    @Override
    public PurchaseApplicationDetailEntity getByPurchaseApplicationIdAndSkuId(String purchaseApplicationId, String skuId) {
        return lambdaQuery()
                .eq(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationId)
                .eq(PurchaseApplicationDetailEntity::getSkuId,skuId)
                .one();
    }

    @Override
    public List<PurchaseApplicationDetailEntity> listCreatePurchaseOrderDetail(List<String> purchaseApplicationIds) {
        return  lambdaQuery()
                .in(PurchaseApplicationDetailEntity::getPurchaseApplicationId,purchaseApplicationIds)
                .ne(PurchaseApplicationDetailEntity::getCreatePoType, CreatePoTypeEnum.ALL_GENERATED.getStatus())
                .orderByAsc(PurchaseApplicationDetailEntity::getPurchaseApplicationId)
                .list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseApplicationDetailDTO.UpdateDTO> newList, List<PurchaseApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseApplicationDetailEntity::getId).collect(Collectors.toList());
        return newIds.stream().filter(s -> !oldIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<PurchaseApplicationDetailEntity> newList,String purchaseApplicationId) {
        //仓库信息
        List<String> destWarehouseIdList = newList.stream().map(PurchaseApplicationDetailEntity::getDestWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(destWarehouseIdList);
        for (PurchaseApplicationDetailEntity entity : newList) {
            entity.setPurchaseApplicationId(purchaseApplicationId);
            if (CollectionUtils.isEmpty(warehouseList)) {
               continue;
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDestWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDestWarehouseName(warehouseName);
        }
    }

}
