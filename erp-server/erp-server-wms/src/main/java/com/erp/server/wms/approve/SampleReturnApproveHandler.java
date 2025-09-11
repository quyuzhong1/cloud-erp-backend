package com.erp.server.wms.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.entity.SampleReturnInfoEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SampleReturnInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SAMPLE_RETURN_INFO)
public class SampleReturnApproveHandler extends AbstractApproveHandler {

    @Resource
    private SampleReturnInfoService sampleReturnInfoService;

    @Resource
    private OperateLogService operateLogService;

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
    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<com.erp.model.wms.dto.OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.SAMPLE_RETURN_INFO.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
