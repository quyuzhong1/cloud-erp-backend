package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.ApproveDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.factory.ApproveEndHandlerFactory;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.plm.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/2 16:04
 */
@Service
public class WorkflowProcessServiceImpl implements WorkflowProcessService {

    @Resource
    private ApproveEndHandlerFactory approveEndHandlerFactory;

    @Resource
    private LogisticsProductService logisticsProductService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Resource
    private PilotApplicationDetailService pilotApplicationDetailService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductChangeService productChangeService;
    @Resource
    private ProductChangeDetailsService productChangeDetailService;

    @Override
    public Map<String, Object> getVariablesMap(EndProcessDTO dto) {
        Map<String, Object> variablesMap = new HashMap<>();
        String businessKey = dto.getBusinessKey();
        switch (SourceTypeEnum.getByCode(businessKey)) {
            case PRODUCT_LOGISTICS:
                //产品物流
                variablesMap = getProductLogisticsMap(dto);
                break;
            case PILOT_APPLICATION:
                //试产量产单
                variablesMap = getPilotApplicationMap(dto);
                break;
            case PRODUCT_DETAIL:
                variablesMap = getProductDetailMap(dto);
                //产品信息
                break;
            case PRODUCT_BOM_INFO:
                variablesMap = getBomInfoMap(dto);
                //Bom信息
                break;
            case PRODUCT_CHANGE:
                variablesMap = getProductChangeMap(dto);
                //Bom信息
                break;
            default:
                break;
        }

        return variablesMap;
    }

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

    /**
     * Bom信息审核通过
     * @param dto
     */
    private Map<String, Object> getBomInfoMap(EndProcessDTO dto) {
        BomInfoEntity entity = bomInfoService.getById(dto.getBusinessId());
        if(Objects.isNull(entity)){
            return null;
        }
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        return variablesMap;
    }
    /**
     * 产品信息审核通过
     * @param dto
     */
    private Map<String, Object> getProductChangeMap(EndProcessDTO dto) {
        ProductChangeEntity entity = productChangeService.getById(dto.getBusinessId());
        if(Objects.isNull(entity)){
            return null;
        }
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<ProductChangeDetailsEntity> detailList = productChangeDetailService.lambdaQuery().eq(ProductChangeDetailsEntity::getChangeInfoId, entity.getId()).list();
        if(CollUtil.isNotEmpty(detailList)){
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        return variablesMap;
    }

    /**
     * 产品信息审核通过
     * @param dto
     */
    private Map<String, Object> getProductDetailMap(EndProcessDTO dto) {
        ProductDetailEntity entity = productDetailService.getById(dto.getBusinessId());
        if(Objects.isNull(entity)){
            return null;
        }
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        return variablesMap;
    }


    /**
     * 产品物流
     * @param dto
     */
    private Map<String, Object> getProductLogisticsMap(EndProcessDTO dto) {
        ProductLogisticsEntity entity = productLogisticsService.getById(dto.getBusinessId());
        if(Objects.isNull(entity)){
            return null;
        }
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        return variablesMap;
    }

    /**
     * 试产检查
     * @param dto
     */
    private Map<String, Object> getPilotApplicationMap(EndProcessDTO dto) {
        PilotApplicationEntity entity = pilotApplicationService.getById(dto.getBusinessId());
        if(Objects.isNull(entity)){
            return null;
        }
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<PilotApplicationDetailEntity> detailList = pilotApplicationDetailService.lambdaQuery().eq(PilotApplicationDetailEntity::getMainId, entity.getId()).list();
        if(CollUtil.isNotEmpty(detailList)){
            variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        }
        return variablesMap;
    }
}
