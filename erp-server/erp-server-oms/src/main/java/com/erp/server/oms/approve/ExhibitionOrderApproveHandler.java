package com.erp.server.oms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ExhibitionOrderEntity;
import com.erp.server.oms.service.ExhibitionOrderService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.EXHIBITION_ORDER)
public class ExhibitionOrderApproveHandler extends AbstractApproveHandler {
    @Resource
    private ExhibitionOrderService exhibitionOrderService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = exhibitionOrderService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = exhibitionOrderService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ExhibitionOrderEntity entity = exhibitionOrderService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到展会订单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return exhibitionOrderService.approveEnd(approveOneDTO,entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
