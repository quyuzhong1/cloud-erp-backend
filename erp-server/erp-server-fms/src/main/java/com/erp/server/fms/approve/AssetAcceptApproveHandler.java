package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.erp.server.fms.service.AssetAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 资产验收单审批处理器
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_ACCEPTANCE)
public class AssetAcceptApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetAcceptService assetAcceptService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetAcceptService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetAcceptService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetAcceptEntity assetAcceptEntity = assetAcceptService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(assetAcceptService.getVariablesMap(assetAcceptEntity));
        return assetAcceptService.approveEnd(approveOne, assetAcceptEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}

