package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleScrapInfoEntity;
import com.erp.server.wms.service.SampleScrapInfoService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_SCRAP_INFO)
public class SampleScrapApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleScrapInfoService sampleScrapInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleScrapInfoService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleScrapInfoService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleScrapInfoEntity entity = sampleScrapInfoService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品报废单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleScrapInfoService.approveEnd(approveOneDTO,entity);
    }
}
