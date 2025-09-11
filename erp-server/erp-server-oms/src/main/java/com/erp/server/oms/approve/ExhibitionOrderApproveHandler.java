package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.entity.ExhibitionOrderEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.ExhibitionOrderService;
import com.erp.server.oms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.EXHIBITION_ORDER)
public class ExhibitionOrderApproveHandler extends AbstractApproveHandler {
    @Resource
    private ExhibitionOrderService exhibitionOrderService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO result = exhibitionOrderService.cancelProcess(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO result = exhibitionOrderService.disApprove(dto.getId());
        return result.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ExhibitionOrderEntity entity = exhibitionOrderService.getByIdOpt(dto.getBusinessId()).orElseThrow(() -> new ServiceException("未找到展会订单数据"));
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        return exhibitionOrderService.approveEnd(approveOneDTO,entity);
    }
    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<com.erp.model.oms.dto.OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.EXHIBITION_ORDER.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
