package com.erp.server.wms.approve;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoReturnReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_RETURN_RECEIVE)
public class SoReturnReceiveApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return soReturnReceiveService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoReturnReceiveEntity entity = soReturnReceiveService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"销售退货签收单");
        }
        BatchResultDTO resultDTO = soReturnReceiveService.disApprove(entity);
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoReturnReceiveEntity entity = soReturnReceiveService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"销售退货签收单");
        }
        Boolean approve = soReturnReceiveService.approveEnd(new ApproveOneDTO(dto.getBusinessId(),dto.getApproveStatus().getStatus(),dto.getComment()),entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个销售退货签收单",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
