package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
            for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : list) {

            }


        }
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
