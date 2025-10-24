package com.erp.server.plm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.plm.entity.AssetPurchaseOrderEntity;
import com.erp.server.plm.service.AssetPurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: wtr
 * @Date: 2025/10/24 15:54
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_PURCHASE_ORDER)
public class AssetPurchaseOrderApproveHandler extends AbstractApproveHandler {


    @Resource
    private AssetPurchaseOrderService assetPurchaseOrderService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetPurchaseOrderService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetPurchaseOrderService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetPurchaseOrderEntity assetPurchaseOrderEntity = assetPurchaseOrderService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return assetPurchaseOrderService.approveEnd(approveOne,assetPurchaseOrderEntity);

    }
}
