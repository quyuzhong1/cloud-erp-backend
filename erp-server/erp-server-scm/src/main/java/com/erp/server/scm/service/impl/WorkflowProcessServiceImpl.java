package com.erp.server.scm.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.entity.*;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务层
 * @date 2023/7/3 15:38
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private PurchasePriceService purchasePriceService;

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private SubcontractOrderService subcontractOrderService;


    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case PURCHASE_PRICE:
                //采购价目
                purchasePriceApproveEnd(dto);
                break;
            case PURCHASE_PRICE_CHANGE:
                //采购调价
                purchasePriceChangeApproveEnd(dto);
                break;
            case SUPPLIER:
                //供应商
                supplierApproveEnd(dto);
                break;
            case PURCHASE_ORDER:
                //采购订单
                purchaseOrderApproveEnd(dto);
                break;
            case SUBCONTRACT_ORDER:
                //委外订单
                subcontractOrderApproveEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }

    /**
     * 采购价目审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean purchasePriceApproveEnd(EndProcessDTO dto) {
        //销售变更单
        PurchasePriceEntity entity = purchasePriceService.getById(dto.getBusinessId());
        return purchasePriceService.approveEnd(entity,dto.getApproveStatus().getStatus(),"", null);
    }

    /**
     * 采购调价审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private BatchResultDTO purchasePriceChangeApproveEnd(EndProcessDTO dto) {
        //销售变更单
        PurchasePriceChangeEntity entity = purchasePriceChangeService.getById(dto.getBusinessId());
        return purchasePriceChangeService.approveEnd(entity,dto.getApproveStatus().getStatus(), "", null);
    }

    /**
     * @description: 供应商审核结束
     * @author Will
     * @date: 2023/7/11 12:13
     * @param dto
     * @return Boolean
     */
    private Boolean supplierApproveEnd(EndProcessDTO dto) {
        //供应商
        SupplierEntity entity = supplierService.getById(dto.getBusinessId());
        return supplierService.approveEnd(entity,dto.getApproveStatus().getStatus(),"", null);
    }

    /**
     * @description: 采购订单结束审核
     * @author Will
     * @date: 2023/7/11 14:06
     * @param dto
     * @return Boolean
     */
    private Boolean purchaseOrderApproveEnd(EndProcessDTO dto) {
        //供应商
        PurchaseOrderEntity entity = purchaseOrderService.getById(dto.getBusinessId());
        ApproveOneDTO baseApproveParamDTO = new ApproveOneDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setId(dto.getBusinessId());
        return purchaseOrderService.approveEnd(baseApproveParamDTO,entity);
    }

    /**
     * @description: 委外订单结束审核
     * @author Will
     * @date: 2023/7/11 14:06
     * @param dto
     * @return Boolean
     */
    private Boolean subcontractOrderApproveEnd(EndProcessDTO dto) {
        //供应商
        SubcontractOrderEntity entity = subcontractOrderService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return subcontractOrderService.approveEnd(approveOneDTO,entity);
    }
}
