package com.erp.server.wms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.server.wms.service.TransferOutService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@ApproveBusinessKey(SourceTypeEnum.TRANSFER_OUT)
public class TransferOutApproveHandler extends AbstractApproveHandler {

    @Resource
    private TransferOutService transferOutService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        transferOutService.cancel(new ApproveDTO.BatchCancelProcessDTO(Collections.singletonList(dto.getId())));
        return Boolean.TRUE;
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        TransferOutEntity entity = transferOutService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_TRANSFER_OUT);
        }
        transferOutService.disApprove(entity);
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //调入单
        TransferOutEntity entity = transferOutService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_NOT_EXIST_TRANSFER_OUT);
        }
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return transferOutService.approveEnd(approveOneDTO,entity);
    }
}
