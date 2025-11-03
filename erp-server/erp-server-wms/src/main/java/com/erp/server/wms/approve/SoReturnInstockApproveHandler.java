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
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoReturnInstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.SO_RETURN_INSTOCK)
public class SoReturnInstockApproveHandler extends AbstractApproveHandler {

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return soReturnInstockService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoReturnInstockEntity entity = soReturnInstockService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"销售退货入库单");
        }
        BatchResultDTO resultDTO = soReturnInstockService.disApprove(entity,Boolean.TRUE);
        return resultDTO.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoReturnInstockEntity entity = soReturnInstockService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"销售退货入库单");
        }
        Boolean approve = soReturnInstockService.approveEnd(new ApproveOneDTO(dto.getBusinessId(),dto.getApproveStatus().getStatus(),dto.getComment()),entity);
        if (Boolean.FALSE.equals(approve)) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE, SourceTypeEnum.getName(dto.getBusinessKey()));
        }
        //非erp审核添加日志
        if (ApprovePlatformEnum.ERP.equals(dto.getApprovePlatformEnum())) {
            return Boolean.TRUE;
        }
        //添加日志
        return operateLogService.addModuleOperateLog(CharSequenceUtil.format("【{}】审核，审核【{}】了一个销售退货入库单",dto.getApprovePlatformEnum().getName(), ApproveTypeEnum.getName(dto.getApproveStatus().getStatus())), ModuleTypeEnum.SO_RETURN_INSTOCK.getCode(),dto.getBusinessId(), "审核操作");
    }

    @Override
    public void addComment(ApproveDTO.AddCommentDTO dto) {

    }
}
