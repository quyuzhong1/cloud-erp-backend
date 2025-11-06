package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.erp.server.fms.service.AssetStocktakingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 资产盘点表审批处理器
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_INVENTORY_SHEET)
public class AssetStocktakingApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetStocktakingService assetStocktakingService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetStocktakingService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetStocktakingService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetStocktakingEntity assetStocktakingEntity = assetStocktakingService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(assetStocktakingService.getVariablesMap(assetStocktakingEntity));
        return assetStocktakingService.approveEnd(approveOne, assetStocktakingEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}

