package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetLocationEntity;
import com.erp.server.fms.service.AssetLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 资产位置审批处理器
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_LOCATION)
public class AssetLocationApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetLocationService assetLocationService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetLocationService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetLocationService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetLocationEntity assetLocationEntity = assetLocationService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(assetLocationService.getVariablesMap(assetLocationEntity));
        return assetLocationService.approveEnd(approveOne, assetLocationEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}

