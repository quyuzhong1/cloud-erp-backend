package com.erp.server.scm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.service.PurchasePriceChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PURCHASE_PRICE_CHANGE)
public class PurchasePriceChangeApproveHandler extends AbstractApproveHandler {

    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return purchasePriceChangeService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("采购调价表，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //采购调价
        PurchasePriceChangeEntity entity = purchasePriceChangeService.getById(dto.getBusinessId());
        BatchResultDTO resultDTO = purchasePriceChangeService.approveEnd(entity, dto.getApproveStatus().getStatus(), "", null);
        return resultDTO.getSuccess();
    }
}
