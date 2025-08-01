package com.erp.server.wms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.server.wms.service.TransferApplicationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@ApproveBusinessKey(SourceTypeEnum.TRANSFER_APPLICATION)
public class TransferApplicationApproveHandler extends AbstractApproveHandler {

    @Resource
    private TransferApplicationService transferApplicationService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return transferApplicationService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        //申请调拨单
        TransferApplicationEntity entity = transferApplicationService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        BatchResultDTO resultDTO = transferApplicationService.disApprove(entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //申请调拨单
        TransferApplicationEntity entity = transferApplicationService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        return transferApplicationService.approveEnd(entity, dto.getApproveStatus().getStatus(), dto.getComment(), null);
    }
}
