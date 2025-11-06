package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetProfitLossEntity;
import com.erp.server.fms.service.AssetProfitLossService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 盘盈盘亏单审批处理器
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.INVENTORY_GAIN_LOSS)
public class AssetProfitLossApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetProfitLossService assetProfitLossService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetProfitLossService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetProfitLossService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetProfitLossEntity assetProfitLossEntity = assetProfitLossService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        return assetProfitLossService.approveEnd(approveOne, assetProfitLossEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}

