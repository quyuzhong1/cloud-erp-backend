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
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.server.scm.service.PurchaseOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.PURCHASE_ORDER)
public class PurchaseOrderApproveHandler extends AbstractApproveHandler {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        PurchaseOrderEntity entity = purchaseOrderService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        BatchResultDTO resultDTO = purchaseOrderService.cancelProcess(dto,entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = purchaseOrderService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //采购订单
        PurchaseOrderEntity entity = purchaseOrderService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98025);
        }
        ApproveOneDTO baseApproveParamDTO = new ApproveOneDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setId(dto.getBusinessId());
        return purchaseOrderService.approveEnd(baseApproveParamDTO, entity);
    }
}
