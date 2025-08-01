package com.erp.server.scm.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.server.scm.service.PurchaseApplicationService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.PURCHASE_APPLICATION)
public class PurchaseApplicationApproveHandler extends AbstractApproveHandler {

    @Resource
    private PurchaseApplicationService purchaseApplicationService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        PurchaseApplicationEntity entity = purchaseApplicationService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        BatchResultDTO resultDTO = purchaseApplicationService.cancelProcess(entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        PurchaseApplicationEntity entity = purchaseApplicationService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        BatchResultDTO resultDTO = purchaseApplicationService.disApprove(entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //采购申请订单
        PurchaseApplicationEntity entity = purchaseApplicationService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98016);
        }
        ApproveOneDTO baseApproveParamDTO = new ApproveOneDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setId(dto.getBusinessId());
        baseApproveParamDTO.setComment(dto.getComment());
        return purchaseApplicationService.approveEnd(baseApproveParamDTO, entity);
    }
}
