package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import com.erp.server.oms.service.SoMultiChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_MULTI_CHANNEL)
public class SoMultiChannelApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoMultiChannelService soMultiChannelService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = soMultiChannelService.cancelProcess(dto);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = soMultiChannelService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoMultiChannelEntity entity = soMultiChannelService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return soMultiChannelService.approveEnd(approveOneDTO,entity);
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.SO_CHANGE.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
