package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ClientTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleTransferInfoEntity;
import com.erp.server.wms.service.SampleTransferInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 样品转移单审核处理器
 * @author wuhaotian
 * @date 2025-10-28
 */
@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_TRANSFER_INFO)
public class SampleTransferInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleTransferInfoService sampleTransferInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleTransferInfoService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleTransferInfoService.disApprove(dto.getId(), ClientTypeEnum.WEB);
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleTransferInfoEntity entity = sampleTransferInfoService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品转移单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleTransferInfoService.approveEnd(approveOneDTO, entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
