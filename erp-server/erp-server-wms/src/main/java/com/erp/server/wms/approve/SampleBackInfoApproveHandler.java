package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleBackInfoEntity;
import com.erp.server.wms.service.SampleBackInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 样品退回单审核处理器
 * @author wuhaotian
 * @date 2025-09-15
 */
@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO)
public class SampleBackInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleBackInfoService sampleBackInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleBackInfoService.cancelProcess(dto);
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleBackInfoService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleBackInfoEntity entity = sampleBackInfoService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleBackInfoService.approveEnd(approveOneDTO, entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
