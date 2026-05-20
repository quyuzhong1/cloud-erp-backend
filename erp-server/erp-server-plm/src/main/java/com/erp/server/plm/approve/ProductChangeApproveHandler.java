package com.erp.server.plm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.server.plm.service.ProductChangeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.PRODUCT_CHANGE)
public class ProductChangeApproveHandler extends AbstractApproveHandler {

    @Resource
    private ProductChangeService productChangeService;

    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        return productChangeService.approve(dto);
    }

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = productChangeService.cancelProcess(dto);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO batchResultDTO = productChangeService.disApprove(dto.getId());
        return batchResultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setComment(dto.getComment());
        ProductChangeEntity productChangeEntity = productChangeService.getById(dto.getBusinessId());
        return productChangeService.approveEnd(approveOne,productChangeEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
