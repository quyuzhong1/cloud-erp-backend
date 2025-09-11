package com.erp.server.wms.approve;

import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.erp.server.wms.service.SampleReturnInfoService;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO)
public class SampleReturnApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleReturnInfoService sampleReturnInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = sampleReturnInfoService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = sampleReturnInfoService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SampleReturnInfoEntity entity = sampleReturnInfoService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到样品归还单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return sampleReturnInfoService.approveEnd(approveOneDTO,entity);
    }
}
