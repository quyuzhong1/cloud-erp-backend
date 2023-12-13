package com.erp.server.oms.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.oms.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务层
 * @date 2023/7/3 15:38
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SoB2cService soB2cService;

    @Override
    public Boolean approveEnd(EndProcessDTO dto) {
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case SO_CHANGE:
                //销售变更
                soChangeApproveEnd(dto);
                break;
            case SO_INFO:
                //销售订单
                soInfoApproveEnd(dto);
                break;
            case CUSTOMER_INFO:
                //客户信息
                customerInfoApproveEnd(dto);
                break;
            case SO_B2C:
                //b2c销售订单
                SoB2cApproveEnd(dto);
                break;
            default:
                break;
        }
        return Boolean.TRUE;
    }

    


    /**
     * 销售变更单审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean soChangeApproveEnd(EndProcessDTO dto) {
        //销售变更单
        List<SoChangeEntity> list = soChangeService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return soChangeService.approveEnd(baseApproveParamDTO,list);
    }

    /**
     * 客户信息审核结束
     * @Author Will
     * @Date 2023/7/4 11:26
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean customerInfoApproveEnd(EndProcessDTO dto) {
        //销售变更单
        List<CustomerInfoEntity> list = customerInfoService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return customerInfoService.approveEnd(baseApproveParamDTO,list);
    }

    /**
     * 销售订单审核结束
     * @Author Luo_WG
     * @Date 2023/7/4 11:29
     * @param dto
     * @return java.lang.Boolean
     **/
    private Boolean soInfoApproveEnd(EndProcessDTO dto) {
        List<SoInfoEntity> list = soInfoService.listByIds(Arrays.asList(dto.getBusinessId()));
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return soInfoService.approveEnd(baseApproveParamDTO,list);
    }

    /**
     * @description: b2c销售订单
     * @author Will
     * @date: 2023/12/13 14:19
     * @param dto
     * @return Boolean
     */
    private Boolean SoB2cApproveEnd(EndProcessDTO dto) {
        SoB2cEntity entity = soB2cService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return soB2cService.approveEnd(approveOneDTO,entity);
    }
}
