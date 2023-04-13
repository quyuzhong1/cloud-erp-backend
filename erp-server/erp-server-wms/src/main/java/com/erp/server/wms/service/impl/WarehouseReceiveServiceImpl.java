package com.erp.server.wms.service.impl;

import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDetailDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.server.wms.mapper.WarehouseReceiveMapper;
import com.erp.server.wms.service.WarehouseReceiveService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

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

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @Override
    public Boolean add(WarehouseReceiveDTO.AddDTO dto) {
        //获取采购订单主表信息
        PurchaseOrderEntity purchaseOrderEntity = productOrderFeign.getPurchaseOrderById(dto.getPurchaseOrderId());
        //获取采购单供应商信息
        PurchaseOrderSupplierEntity orderSupplierByOrderId = productOrderFeign.getOrderSupplierByOrderId(purchaseOrderEntity.getId());
        //获取用户信息
        SysUserDTO userDTO = sysUserFeign.getSysUserById(dto.getReceiveUserId());
        //获取用户部门
        SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(dto.getReceiveDeptId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getReceiveOrgId());
        //获取仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getDeliveryWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.PO, BusinessNoTypeEnum.CODE_PO.getCode()));
        //设置收货单主表
        WarehouseReceiveEntity warehouseReceiveEntity = new WarehouseReceiveEntity();
        warehouseReceiveEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        warehouseReceiveEntity.setCode(code);
        warehouseReceiveEntity.setPurchaseOrderId(purchaseOrderEntity.getId());
        warehouseReceiveEntity.setPurchaseOrderCode(purchaseOrderEntity.getCode());
        warehouseReceiveEntity.setSupplierId(orderSupplierByOrderId.getSupplierId());
        warehouseReceiveEntity.setSupplierName(orderSupplierByOrderId.getSupplierName());
        warehouseReceiveEntity.setReceiveUserId(dto.getReceiveUserId());
        warehouseReceiveEntity.setReceiveUserName(userDTO.getUserName());
        warehouseReceiveEntity.setReceiveDeptId(dto.getReceiveDeptId());
        warehouseReceiveEntity.setReceiveDeptName(departmentDTO.getName());
        warehouseReceiveEntity.setReceiveOrgId(dto.getReceiveOrgId());
        warehouseReceiveEntity.setReceiveOrgName(sysAccountingCompanyEntity.getCompanyName());
        warehouseReceiveEntity.setBillDate(dto.getBillTime());
        warehouseReceiveEntity.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        warehouseReceiveEntity.setDeliveryWarehouseName(warehouseEntity.getName());

        List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList = dto.getWarehouseReceiveDetailList();

        return true;
    }
}
