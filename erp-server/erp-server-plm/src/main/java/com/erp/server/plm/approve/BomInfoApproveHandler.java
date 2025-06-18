package com.erp.server.plm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.server.plm.service.BomInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PRODUCT_BOM_INFO)
public class BomInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private BomInfoService bomInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = bomInfoService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("BOM信息，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        BomInfoEntity entity = bomInfoService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return bomInfoService.approveEnd(approveOne,entity);
    }
}
