package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.server.wms.service.SampleRecipientService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 样品领用单审核处理器
 * @author wuhaotian
 * @date 2025-09-15
 */
@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_RECIPIENT)
public class SampleRecipientApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleRecipientService sampleRecipientService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleRecipientService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleRecipientService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleRecipientEntity entity = sampleRecipientService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品领用单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleRecipientService.approveEnd(approveOneDTO, entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
