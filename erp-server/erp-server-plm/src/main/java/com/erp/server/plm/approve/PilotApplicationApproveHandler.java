package com.erp.server.plm.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.PilotApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.PILOT_APPLICATION)
public class PilotApplicationApproveHandler extends AbstractApproveHandler {

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = pilotApplicationService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = pilotApplicationService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setComment(dto.getComment());
        PilotApplicationEntity entity = new PilotApplicationEntity();
        entity.setId(dto.getBusinessId());
        Boolean approveEnd = pilotApplicationService.approveEnd(approveOne, entity);
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getApproveStatus().getStatus())) {
            //回写产品管理--采购信息--一级和二级供应商 审核流回调导致状态无法查询，则判断通过则直接通知
            pilotApplicationService.writeProductPurchaseBackByWork(dto.getBusinessId());
            pilotApplicationService.approvePilotApplicationNoticeByWork(dto.getBusinessId());
        }
        if (Boolean.FALSE.equals(approveEnd)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        OperateLogEntity sysLogEntity = new OperateLogEntity();
        sysLogEntity.setBusinessId(dto.getBusinessId());
        sysLogEntity.setOperation("审核操作");
        //添加评论日志
        String operateContent = CharSequenceUtil.format("【{}】审核，审核结果：【{}】，审核意见 ：【{}】",dto.getApprovePlatformEnum().getName(),  dto.getApproveStatus().getName(), dto.getComment());
        sysLogEntity.setContent(operateContent);
        operateLogService.addSysLogByOther(sysLogEntity);
        return approveEnd;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogEntity> list = new ArrayList<>();
        for (String comment : dto.getComments()) {
            OperateLogEntity sysLogEntity = new OperateLogEntity();
            sysLogEntity.setBusinessId(dto.getId());
            sysLogEntity.setOperation("添加评论");
            //添加评论日志
            String operateContent = CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(), comment);
            sysLogEntity.setContent(operateContent);
            list.add(sysLogEntity);
        }
        operateLogService.addSysLogByBatchSave(list);
    }
}
