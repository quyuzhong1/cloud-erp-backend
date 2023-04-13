package com.erp.server.wms.service.impl;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.WarehouseReceiveService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
@Service
public class WarehouseReceiveServiceImpl extends SuperServiceImpl<WarehouseReceiveMapper, WarehouseReceiveEntity> implements WarehouseReceiveService {

    @Resource
    private ProductOrderFeign productOrderFeign;

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    public Boolean add(WarehouseReceiveDTO.AddDTO dto) {
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        productOrderFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());

        WarehouseReceiveEntity warehouseReceiveEntity = new WarehouseReceiveEntity();
        warehouseReceiveEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        warehouseReceiveEntity.setCode("");
        warehouseReceiveEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
        warehouseReceiveEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
       // warehouseReceiveEntity.setSupplierId(purchaseOrderEntity.getSu);


        return true;
    }
}
