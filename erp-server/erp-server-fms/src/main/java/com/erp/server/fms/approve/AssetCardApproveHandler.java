package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetCardEntity;
import com.erp.server.fms.service.AssetCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 资产卡片审批处理器
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_CARD)
public class AssetCardApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetCardService assetCardService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetCardService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetCardService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetCardEntity assetCardEntity = assetCardService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(assetCardService.getVariablesMap(assetCardEntity));
        return assetCardService.approveEnd(approveOne, assetCardEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}

