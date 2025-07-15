package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.server.wms.service.OtherOutstockService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
@ApproveBusinessKey(SourceTypeEnum.OTHER_OUTSTOCK)
public class OtherOutstockApproveHandler extends AbstractApproveHandler {

    @Resource
    private OtherOutstockService otherOutstockService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return otherOutstockService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        return otherOutstockService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        OtherOutstockEntity entity = otherOutstockService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return otherOutstockService.approveEnd(approveOne,entity);
    }
}
