package com.erp.server.oms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.oms.entity.CustomerCreditApplyEntity;
import com.erp.server.oms.service.CustomerCreditApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.CUSTOMER_CREDIT_APPLY)
public class CustomerCreditApplyApproveHandler extends AbstractApproveHandler {

    @Resource
    private CustomerCreditApplyService customerCreditApplyService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = customerCreditApplyService.cancelProcess(dto);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //客户信息
        CustomerCreditApplyEntity entity = customerCreditApplyService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return customerCreditApplyService.approveEnd(approveOneDTO,entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
