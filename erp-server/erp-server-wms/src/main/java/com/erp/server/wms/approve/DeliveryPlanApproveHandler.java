package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.wms.entity.WmsDeliveryPlanEntity;
import com.erp.server.wms.service.WmsDeliveryPlanService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.DELIVERY_PLAN)
public class DeliveryPlanApproveHandler extends AbstractApproveHandler {

    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = wmsDeliveryPlanService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = wmsDeliveryPlanService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //FBA发货单
        WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return wmsDeliveryPlanService.approveEnd(approveOne,entity);
    }
}
