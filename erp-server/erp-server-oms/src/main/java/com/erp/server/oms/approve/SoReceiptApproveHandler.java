package com.erp.server.oms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.server.oms.service.SoReceiptService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.SO_RECEIPT)
public class SoReceiptApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoReceiptService soReceiptService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return soReceiptService.cancelProcess(dto.getId());
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoReceiptEntity soReceiptEntity = soReceiptService.getById(dto.getId());
        if (ObjectUtil.isEmpty(soReceiptEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        BatchResultDTO resultDTO = soReceiptService.disApprove(soReceiptEntity.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoReceiptEntity soReceiptEntity = soReceiptService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return soReceiptService.approveEnd(approveOneDTO,soReceiptEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
