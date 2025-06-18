package com.erp.server.wms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.wms.service.TransferInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@ApproveBusinessKey(SourceTypeEnum.TRANSFER_INFO)
public class TransferInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private TransferInfoService transferInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return transferInfoService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        TransferInfoEntity entity = transferInfoService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        BatchResultDTO resultDTO = transferInfoService.disApprove(entity,Boolean.TRUE,Boolean.TRUE);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //直接调拨单
        TransferInfoEntity entity = transferInfoService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        return transferInfoService.approveEnd(entity, dto.getApproveStatus().getStatus(), "", Boolean.TRUE);
    }
}
