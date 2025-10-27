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
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.erp.server.scm.service.SubcontractOrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.SUBCONTRACT_ORDER)
public class SubcontractOrderApproveHandler extends AbstractApproveHandler {

    @Resource
    private SubcontractOrderService subcontractOrderService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        //委外订单
        SubcontractOrderEntity entity = subcontractOrderService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98073);
        }
        BatchResultDTO resultDTO = subcontractOrderService.cancelProcess(dto,entity);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = subcontractOrderService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //委外订单
        SubcontractOrderEntity entity = subcontractOrderService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return subcontractOrderService.approveEnd(approveOneDTO,entity);
    }
}
