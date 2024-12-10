package com.erp.server.wms.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.wms.service.*;
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
    private TransferApplicationService transferApplicationService;
    @Resource
    private StocktakingPlanService stocktakingPlanService;

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    @Resource
    private StocktakingProfitLossService  stocktakingProfitLossService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;

    @Resource
    private RequisitionApplicationChangeService requisitionApplicationChangeService;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case TRANSFER_APPLICATION:
                //调拨申请
                transferApplicationApproveEnd(dto);
                break;
            case STOCKTAKING_PLAN:
                //调拨申请
                StocktakingPlanApproveEnd(dto);
                break;
            case STOCKTAKING_TASK:
                //盘点任务
                stocktakingTaskApproveEnd(dto);
                break;
            case STOCKTAKING_PROFIT_LOSS:
                //盘盈盘亏单
                stocktakingProfitLossApproveEnd(dto);
                break;
            case FIRST_MILE_DELIVERY:
                //头程发货单
                fbaDeliveryApproveEnd(dto);
                break;
            case DELIVERY_PLAN:
                //海外发货计划
                deliveryPlanApproveEnd(dto);
                break;
            case TRANSFER_INFO:
                //直接调拨单
                transferInfoApproveEnd(dto);
                break;
            case SO_DELIVERY_NOTICE_CHANGE:
                //销售发货通知变更单
                deliveryNoticeChangeApproveEnd(dto);
                break;
            case REQUISITION_APPLICATION_CHANGE:
                //要货申请变更单
                requisitionApplicationChangeEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }

    /**
     * 盘盈盘亏单审核通过
     * @param dto
     */
    private Boolean stocktakingProfitLossApproveEnd(EndProcessDTO dto) {
        StocktakingProfitLossEntity entity = stocktakingProfitLossService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return stocktakingProfitLossService.approveEnd(approveOne,entity);
    }


    /**
     * 盘点任务单审核通过
     * @author yl
     * @date 2023-08-18 8:56
     * @param dto
     * @return void
     */
    private Boolean stocktakingTaskApproveEnd(EndProcessDTO dto) {

        StocktakingTaskEntity entity = stocktakingTaskService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return stocktakingTaskService.approveEnd(approveOne,entity);
    }

    /**
     * @description: 直接调拨单
     * @author Will
     * @date: 2023/8/2 16:13
     * @param dto
     * @return Boolean
     */
    private Boolean transferApplicationApproveEnd(EndProcessDTO dto) {
        //直接调拨单
        TransferApplicationEntity entity = transferApplicationService.getById(dto.getBusinessId());
        return transferApplicationService.approveEnd(entity, dto.getApproveStatus().getStatus(), "", null);
    }

    /**
     * @description: 直接调拨单
     * @author Will
     * @date: 2023/8/2 16:13
     * @param dto
     * @return Boolean
     */
    private Boolean StocktakingPlanApproveEnd(EndProcessDTO dto) {
        //直接调拨单
        StocktakingPlanEntity entity = stocktakingPlanService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return stocktakingPlanService.approveEnd(approveOne,entity);
    }

    /**
     * FBA发货单
     * @Author Luo_WG
     * @Date 2023/11/15 17:56
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean fbaDeliveryApproveEnd(EndProcessDTO dto) {
        //FBA发货单
        FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return firstMileDeliveryService.approveEnd(approveOne,entity);
    }

    /**
     * 海外发货计划
     * @Author Luo_WG
     * @Date 2023/11/17 16:14
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean deliveryPlanApproveEnd(EndProcessDTO dto) {
        //FBA发货单
        WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return wmsDeliveryPlanService.approveEnd(approveOne,entity);
    }

    /**
     * 直接调拨单
     * @Author Luo_WG
     * @Date 2024/9/6 18:18
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean transferInfoApproveEnd(EndProcessDTO dto) {
        //直接调拨单
        TransferInfoEntity entity = transferInfoService.getById(dto.getBusinessId());
        return transferInfoService.approveEnd(entity, dto.getApproveStatus().getStatus(), "", Boolean.TRUE);
    }
    /**
     * 发货通知变更
     * @Author Luo_WG
     * @Date 2024/9/6 18:18
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean deliveryNoticeChangeApproveEnd(EndProcessDTO dto) {
        //发货通知变更
        SoDeliveryNoticeChangeEntity entity = soDeliveryNoticeChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return soDeliveryNoticeChangeService.approveEnd(approveOneDTO,entity);
    }

    /**
     * 要货申请变更
     **/
    private Boolean requisitionApplicationChangeEnd(EndProcessDTO dto) {
        RequisitionApplicationChangeEntity entity = requisitionApplicationChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return requisitionApplicationChangeService.approveEnd(approveOneDTO,entity);
    }

}
