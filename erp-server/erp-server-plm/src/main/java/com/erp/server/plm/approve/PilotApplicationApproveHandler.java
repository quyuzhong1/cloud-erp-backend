package com.erp.server.plm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.server.plm.service.PilotApplicationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.PILOT_APPLICATION)
public class PilotApplicationApproveHandler extends AbstractApproveHandler {

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = pilotApplicationService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = pilotApplicationService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
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
