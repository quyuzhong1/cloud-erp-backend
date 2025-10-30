package com.erp.server.fms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.fms.entity.AssetDisposalEntity;
import com.erp.model.scm.entity.AssetNoticeEntity;
import com.erp.server.fms.service.AssetDisposalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: jack
 * @Date: 2025-10-30
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.ASSET_DISPOSAL)
public class AssetDisposalApproveHandler extends AbstractApproveHandler {

    @Resource
    private AssetDisposalService assetDisposalService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = assetDisposalService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = assetDisposalService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        AssetDisposalEntity assetDisposalEntity = assetDisposalService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setVariablesMap(assetDisposalService.getVariablesMap(assetDisposalEntity));
        return assetDisposalService.approveEnd(approveOne,assetDisposalEntity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
