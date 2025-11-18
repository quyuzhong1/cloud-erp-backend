package com.erp.server.scm.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.server.scm.service.SupplierPhaseService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@ApproveBusinessKey(SourceTypeEnum.SUPPLIER_PHASE)
public class SupplierPhaseApproveHandler extends AbstractApproveHandler {

    @Resource
    private SupplierPhaseService supplierPhaseService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return supplierPhaseService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //供应商
        SupplierPhaseEntity entity = supplierPhaseService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        return supplierPhaseService.approveEnd(new ApproveOneDTO(entity.getId(),dto.getApproveStatus().getStatus(),dto.getComment()),entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
