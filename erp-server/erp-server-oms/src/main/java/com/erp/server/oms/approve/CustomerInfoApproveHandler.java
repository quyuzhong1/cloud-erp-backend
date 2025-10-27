package com.erp.server.oms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.service.CustomerInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
@ApproveBusinessKey(SourceTypeEnum.CUSTOMER_INFO)
public class CustomerInfoApproveHandler extends AbstractApproveHandler {

    @Resource
    private CustomerInfoService customerInfoService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return customerInfoService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getId());
        if (ObjectUtil.isEmpty(customerInfo)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        BatchResultDTO resultDTO = customerInfoService.disApprove(customerInfo);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //客户信息
        List<CustomerInfoEntity> list = customerInfoService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return customerInfoService.approveEnd(baseApproveParamDTO,list);
    }
}
