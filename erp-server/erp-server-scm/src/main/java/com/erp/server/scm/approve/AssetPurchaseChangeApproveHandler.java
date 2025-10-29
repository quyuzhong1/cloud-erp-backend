package com.erp.server.scm.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.scm.entity.AssetPurchaseChangeEntity;
import com.erp.server.scm.service.AssetPurchaseChangeService;
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
@ApproveBusinessKey(SourceTypeEnum.ASSET_PURCHASE_CHANGE)
public class AssetPurchaseChangeApproveHandler extends AbstractApproveHandler {


    @Resource
    private AssetPurchaseChangeService assetPurchaseChangeService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetPurchaseChangeService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetPurchaseChangeService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetPurchaseChangeEntity assetPurchaseChangeEntity = assetPurchaseChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(dto.getVariablesMap());
        return assetPurchaseChangeService.approveEnd(approveOne,assetPurchaseChangeEntity);

    }
}
