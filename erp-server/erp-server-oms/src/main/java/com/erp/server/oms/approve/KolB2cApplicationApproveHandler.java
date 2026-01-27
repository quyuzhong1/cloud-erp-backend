package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.entity.KolB2cApplicationEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.service.KolB2cApplicationService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoReturnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.KOL_B2C_APPLICATION)
public class KolB2cApplicationApproveHandler extends AbstractApproveHandler {

    @Resource
    private KolB2cApplicationService kolB2cApplicationService;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        return kolB2cApplicationService.approve(dto);
    }

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = kolB2cApplicationService.cancelProcess(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = kolB2cApplicationService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        KolB2cApplicationEntity entity = kolB2cApplicationService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE,"B2C寄样申请");
        }
        Boolean approve = kolB2cApplicationService.approveEnd(new ApproveOneDTO(dto.getBusinessId(),dto.getApproveStatus().getStatus(),dto.getComment()),entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.BILL_APPROVE_FAILED, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个销售退货单",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.KOL_B2C_APPLICATION.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
