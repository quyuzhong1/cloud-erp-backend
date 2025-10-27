package com.erp.server.oms.approve;

import cn.hutool.core.collection.CollUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ApproveBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER)
public class CustomerB2bChangeSellerApproveHandler extends AbstractApproveHandler {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        List<BatchResultDTO> batchResultDTOS = customerB2bSellerChangeService.batchCancel(new ApproveDTO.BatchCancelProcessDTO(dto));
        return CollUtil.isEmpty(batchResultDTOS) ? Boolean.TRUE : batchResultDTOS.get(0).getSuccess();
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        log.error("b2b客户销售员变更，id【{}】无反审核功能",dto.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        //销售变更单
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getBusinessId());
        CustomerB2bSellerChangeEntity entity = customerB2bSellerChangeService.getByMainId(customerInfo.getId());
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Collections.singletonList(dto.getBusinessId()));
        baseApproveParamDTO.setComment(dto.getComment());
        return customerB2bSellerChangeService.approveEnd(baseApproveParamDTO,entity,new BatchResultDTO(),customerInfo);
    }
}
