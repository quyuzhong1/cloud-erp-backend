package com.erp.server.wms.approve;

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
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.FIRST_MILE_DELIVERY)
public class FbaDeliveryApproveHandler extends AbstractApproveHandler {

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        BatchResultDTO resultDTO = firstMileDeliveryService.cancelProcess(dto);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        BatchResultDTO resultDTO = firstMileDeliveryService.disApprove(dto.getId());
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //FBA发货单
        FirstMileDeliveryEntity entity = firstMileDeliveryService.getById(dto.getBusinessId());
        ApproveOneDTO approveOne = new ApproveOneDTO();
        approveOne.setType(dto.getApproveStatus().getStatus());
        approveOne.setId(dto.getBusinessId());
        approveOne.setDeliveryDate(dto.getDeliveryDate());
        approveOne.setComment(dto.getComment());
        Boolean approve = firstMileDeliveryService.approveEnd(approveOne, entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个FBA发货单",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {
        if (CollUtil.isEmpty(dto.getComments())) {
            log.warn("无评论无需添加日志，businessKey = {},id={}",dto.getBusinessKey(),dto.getId());
            return;
        }
        List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();
        dto.getComments().stream().map(obj -> new OperateLogDTO.AddModuleOperateLogDTO(CharSequenceUtil.format("【{}】流程添加评论【{}】",dto.getApprovePlatformEnum().getName(),obj), ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(), dto.getId(), "添加评论"))
                .forEach(operateLogList::add);
        operateLogService.batchAddModuleOperateLog(operateLogList);
    }
}
