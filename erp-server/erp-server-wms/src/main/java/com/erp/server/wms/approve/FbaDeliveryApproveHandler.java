package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.server.wms.service.FirstMileDeliveryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY)
public class FbaDeliveryApproveHandler extends AbstractApproveHandler {

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = firstMileDeliveryService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = firstMileDeliveryService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //FBA发货单
        FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setDeliveryDate(dto.getDeliveryDate());
        approveOne.setComment(dto.getComment());
        return firstMileDeliveryService.approveEnd(approveOne,entity);
    }
}
