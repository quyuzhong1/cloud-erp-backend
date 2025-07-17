package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.erp.server.wms.service.SoDeliveryNoticeChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_DELIVERY_NOTICE_CHANGE)
public class DeliveryNoticeChangeApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = soDeliveryNoticeChangeService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("发货通知单，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //发货通知变更
        SoDeliveryNoticeChangeEntity entity = soDeliveryNoticeChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return soDeliveryNoticeChangeService.approveEnd(approveOneDTO,entity);
    }
}
