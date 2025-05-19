package com.erp.server.plm.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.plm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/2 16:04
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private LogisticsProductService logisticsProductService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductChangeService productChangeService;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case PRODUCT_LOGISTICS:
                //产品物流
                productLogisticsApproveEnd(dto);
                break;
            case PILOT_APPLICATION:
                //试产量产单
                pilotApplicationApproveEnd(dto);
                break;
            case PRODUCT_DETAIL:
                //产品信息
                productDetailApproveEnd(dto);
                break;
            case PRODUCT_BOM_INFO:
                //Bom信息
                bomInfoApproveEnd(dto);
                break;
            case PRODUCT_CHANGE:
                //Bom信息
                productChangeApproveEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }
    /**
     * Bom信息审核通过
     * @param dto
     */
    private Boolean bomInfoApproveEnd(EndProcessDTO dto) {
        BomInfoEntity entity = bomInfoService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return bomInfoService.approveEnd(approveOne,entity);
    }
    /**
     * 产品变更审核通过
     * @param dto
     */
    private Boolean productChangeApproveEnd(EndProcessDTO dto) {
        ProductChangeEntity entity = productChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return productChangeService.approveEnd(approveOne,entity);
    }
    /**
     * 产品信息审核通过
     * @param dto
     */
    private Boolean productDetailApproveEnd(EndProcessDTO dto) {
        ProductDetailEntity entity = productDetailService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return productDetailService.approveEnd(approveOne,entity);
    }
    /**
     * 盘盈盘亏单审核通过
     * @param dto
     */
    private Boolean productLogisticsApproveEnd(EndProcessDTO dto) {
        ProductLogisticsEntity entity = productLogisticsService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return logisticsProductService.approveEnd(approveOne,entity);
    }

    /**
     * 试产量产单审核通过
     */
    private boolean pilotApplicationApproveEnd(EndProcessDTO dto) {
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        PilotApplicationEntity entity = new PilotApplicationEntity();
        entity.setId(dto.getBusinessId());
        Boolean approveEnd = pilotApplicationService.approveEnd(approveOne, entity);
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getApproveStatus().getStatus())) {
            //回写产品管理--采购信息--一级和二级供应商 审核流回调导致状态无法查询，则判断通过则直接通知
            pilotApplicationService.writeProductPurchaseBackByWork(dto.getBusinessId());
            pilotApplicationService.approvePilotApplicationNoticeByWork(dto.getBusinessId());
        }
        return approveEnd;
    }

}
