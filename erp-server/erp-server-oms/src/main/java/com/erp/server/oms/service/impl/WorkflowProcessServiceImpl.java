package com.erp.server.oms.service.impl;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.*;
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

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    @Resource
    private SoPriceService soPriceService;

    @Resource
    private SoPriceChangeService soPriceChangeService;

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
            case TIK_TOK_FULLY:
                //b2c销售订单
                SoB2cApproveEnd(dto);
                break;
            case CUSTOMER_B2B_CHANGE_SELLER:
                //客户信息销售员变更
                customerB2bChangeSellerApproveEnd(dto);
                break;
            case SO_PRICE:
                //销售价目
                soPriceApproveEnd(dto);
                break;
            case SO_PRICE_CHANGE:
                //销售价目
                soPriceApproveChangeEnd(dto);
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

    private Boolean customerB2bChangeSellerApproveEnd(EndProcessDTO dto) {
        //销售变更单
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getBusinessId());
        CustomerB2bSellerChangeEntity entity = customerB2bSellerChangeService.getByMainId(customerInfo.getId());
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        baseApproveParamDTO.setComment(dto.getComment());
        return customerB2bSellerChangeService.approveEnd(baseApproveParamDTO,entity,new BatchResultDTO(),customerInfo);
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
        return soB2cService.approveEnd(approveOneDTO,entity,null);
    }

    /**
     * 销售价目结束审核
     * @author will
     * @date 2025/3/26 10:46
     * @param dto
     * @return java.lang.Boolean
     */
    private Boolean soPriceApproveEnd(EndProcessDTO dto) {
        SoPriceEntity entity = soPriceService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return soPriceService.approveEnd(approveOneDTO,entity);
    }

    /**
     * 销售调价结束审核
     * @author will
     * @date 2025/3/26 10:46
     * @param dto
     * @return java.lang.Boolean
     */
    private Boolean soPriceApproveChangeEnd(EndProcessDTO dto) {
        SoPriceChangeEntity entity = soPriceChangeService.getById(dto.getBusinessId());
        ApproveOneDTO approveOneDTO = new ApproveOneDTO();
        approveOneDTO.setType(dto.getApproveStatus().getStatus());
        approveOneDTO.setId(dto.getBusinessId());
        return soPriceChangeService.approveEnd(approveOneDTO,entity);
    }
}
