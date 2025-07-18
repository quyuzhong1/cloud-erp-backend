package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.factory.ApproveEndHandlerFactory;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.*;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务层
 * @date 2023/7/3 15:38
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private ApproveEndHandlerFactory approveEndHandlerFactory;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        SourceTypeEnum sourceType = SourceTypeEnum.getByCode(businessKey);
        if (null == sourceType) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_APPROVE_BUSINESSKEY,dto.getApproveStatus().getName(),businessKey);
        }
        AbstractApproveHandler handler = approveEndHandlerFactory.getHandler(sourceType);
        return  handler.approveEnd(BeanUtil.toBean(dto, ApproveDTO.EndProcessDTO.class));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        String businessKey = dto.getBusinessKey();
        SourceTypeEnum sourceType = SourceTypeEnum.getByCode(businessKey);
        if (null == sourceType) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_APPROVE_BUSINESSKEY, ApproveTypeEnum.DIS_APPROVE.getName(),businessKey);
        }
        AbstractApproveHandler handler = approveEndHandlerFactory.getHandler(sourceType);
        return  handler.disApprove(dto);
    }

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        SourceTypeEnum sourceType = SourceTypeEnum.getByCode(businessKey);
        if (null == sourceType) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_APPROVE_BUSINESSKEY, ApproveTypeEnum.REVOKE.getName(),businessKey);
        }
        AbstractApproveHandler handler = approveEndHandlerFactory.getHandler(sourceType);
        return  handler.cancelProcess(dto);
    }
}
