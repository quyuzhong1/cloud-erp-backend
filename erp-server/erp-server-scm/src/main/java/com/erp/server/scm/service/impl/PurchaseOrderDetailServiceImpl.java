package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseApplicationRefPoEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.PurchaseApplicationRefPoService;
import com.erp.server.scm.service.PurchaseOrderDetailService;
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
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Override
    public void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseOrderDetailEntity> list = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        //处理明细中的数据id
        doOpHandleDataId(list,purchaseOrderId);
        //批量新增
        boolean flag = this.saveBatch(list);
        if (flag) {
            //新增关联关系
            List<PurchaseApplicationRefPoEntity> refList = new ArrayList<>();
            for (PurchaseOrderDetailEntity entity : list) {
                if (StringUtils.isBlank(entity.getPurchaseApplicationDetailId())) {
                    continue;
                }
                PurchaseApplicationRefPoEntity refPoEntity = new PurchaseApplicationRefPoEntity();
                refPoEntity.setPurchaseOrderId(purchaseOrderId);
                refPoEntity.setPurchaseOrderDetailId(entity.getId());
                refPoEntity.setPurchaseApplicationId(entity.getPurchaseApplicationId());
                refPoEntity.setPurchaseApplicationDetailId(entity.getPurchaseApplicationDetailId());
                refList.add(refPoEntity);
            }
            purchaseApplicationRefPoService.saveBatch(refList);
        }
    }

    @Override
    public PurchaseOrderDetailEntity getByPurchaseOrderIdAndSkuId(String purchaseOrderId, String skuId) {
        return lambdaQuery()
                .eq(PurchaseOrderDetailEntity::getPurchaseApplicationId,purchaseOrderId)
                .eq(PurchaseOrderDetailEntity::getSkuId,skuId)
                .one();
    }

    @Override
    public void update(List<PurchaseOrderDetailDTO.UpdateDTO> details, String purchaseOrderId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseOrderDetailEntity> oldList = this.listByPurchaseOrderId(purchaseOrderId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        List<PurchaseOrderDetailEntity> newList = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        doOpHandleDataId(newList,purchaseOrderId);
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId) {
        return  lambdaQuery().eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseOrderDetailDTO.UpdateDTO> newList, List<PurchaseOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        return newIds.stream().filter(s -> !oldIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDataId (List<PurchaseOrderDetailEntity> newList, String purchaseOrderId) {
        //仓库信息
        List<String> deliveryWarehouseIds = newList.stream().map(PurchaseOrderDetailEntity::getDeliveryWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(deliveryWarehouseIds);

        //收料组织信息
        List<String> receiveOrgIds = newList.stream().map(PurchaseOrderDetailEntity::getReceiveOrgId).collect(Collectors.toList());
        List<BaseIdDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(receiveOrgIds);


        for (PurchaseOrderDetailEntity entity : newList) {
            entity.setPurchaseOrderId(purchaseOrderId);
            //仓库名称
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException(ApiError.ERROR_99002);
            }
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getDeliveryWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
            entity.setDeliveryWarehouseName(warehouseName);
            //收料组织名称
            if (CollectionUtils.isEmpty(accountingCompanyList)) {
                throw new ServiceException(ApiError.ERROR_9040);
            }
            String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getReceiveOrgId())).map(BaseIdDTO::getName).findFirst().orElse(null);
            entity.setReceiveOrgName(receiveOrgName);
        }
    }
}
