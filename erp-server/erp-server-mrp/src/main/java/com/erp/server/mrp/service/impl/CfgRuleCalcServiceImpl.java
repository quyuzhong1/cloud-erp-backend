package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.HistorySalesTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.mrp.es.entity.OrderHistorySalesEsEntity;
import com.erp.server.mrp.es.service.OrderHistorySalesEsService;
import com.erp.server.mrp.mapper.CfgRuleCalcMapper;
import com.erp.server.mrp.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;

import java.time.LocalDate;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 试算配置 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CfgRuleCalcServiceImpl extends SuperServiceImpl<CfgRuleCalcMapper, CfgRuleCalcEntity> implements CfgRuleCalcService {
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgRuleSalesFormulaCalcService cfgRuleSalesFormulaCalcService;
    @Resource
    private OrderHistorySalesEsService orderHistorySalesEsService;

    @Resource
    private CfgRuleSalesDenoisingCalcService cfgRuleSalesDenoisingCalcService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleCalcDTO.AddDTO addDTO) {
        CfgRuleCalcEntity entity = CfgRuleCalcDTO.AddDTO.buildCfgRuleCalcEntity(addDTO);
        if (HistorySalesTypeEnum.SYSTEM.getCode().equals(addDTO.getSaleType())) {
            List<OrderHistorySalesEsEntity> salesInfos = orderHistorySalesEsService.findByShopIdInAndSkuIdInAndDateBetween(addDTO.getShopIds(), addDTO.getSkuIds(),
                    addDTO.getStartCalcDate().minusDays(361), addDTO.getStartCalcDate().minusDays(1));
        } else {

        }
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLSS);
        entity.setCode(code);
        save(entity);
        List<CfgRuleSalesFormulaCalcEntity> formulaCalcEntities = handleSalesFormula(addDTO, entity.getId());
        cfgRuleSalesFormulaCalcService.saveBatch(formulaCalcEntities);
        List<CfgRuleSalesDenoisingCalcEntity> salesDenoising = handleSalesDenoising(addDTO, entity.getId());
        cfgRuleSalesDenoisingCalcService.saveBatch(salesDenoising);
        //todo 异步计算销量预测
        return new BaseResultDTO.AddDTO(entity.getId(), code);
    }

    @Override
    public void downloadHistorySales(CfgRuleCalcDTO.DownloadDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史销量导出", FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_CALC.getCode(), dto);
    }


    List<CfgRuleSalesDenoisingCalcEntity> handleSalesDenoising(CfgRuleCalcDTO.AddDTO addDTO, String cfgRuleCalcId) {
        List<CfgRuleSalesDenoisingCalcEntity> denoisingList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingCalcEntity.class, addDTO.getSalesDenoisingList());
        denoisingList.forEach(obj -> obj.setCfgRuleCalcId(cfgRuleCalcId));
        return denoisingList;
    }


    /**
     * 构造销量计算参数
     *
     * @param addDTO 参数
     */
    private List<CfgRuleSalesFormulaCalcEntity> handleSalesFormula(CfgRuleCalcDTO.AddDTO addDTO, String cfgRuleCalcId) {
        List<CfgRuleSalesFormulaCalcEntity> list = new ArrayList<>();
        //默认日销量
        if (ObjectUtil.isNotEmpty(addDTO.getDefaultSalesQtyDTO())) {
            CfgRuleSalesFormulaCalcEntity defaultDTO = BeanMapperUtils.map(CfgRuleSalesFormulaCalcEntity.class, addDTO.getDefaultSalesQtyDTO());
            defaultDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()).setPriority(MathUtil.THREE);
            defaultDTO.setCfgRuleCalcId(cfgRuleCalcId);
            list.add(defaultDTO);
        }
        //动态日销量
        if (CollectionUtils.isNotEmpty(addDTO.getDynamicSalesQtyList())) {
            List<CfgRuleSalesFormulaCalcEntity> dynamicList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcEntity.class, addDTO.getDynamicSalesQtyList());
            dynamicList.forEach(obj -> {
                obj.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()).setPriority(MathUtil.TWO);
                obj.setCfgRuleCalcId(cfgRuleCalcId);
            });
            list.addAll(dynamicList);
        }
        //固定日销量
        if (CollectionUtils.isNotEmpty(addDTO.getFixedSalesQtyList())) {
            List<CfgRuleSalesFormulaCalcEntity> fixedList = BeanMapperUtils.copyList(CfgRuleSalesFormulaCalcEntity.class, addDTO.getFixedSalesQtyList());
            fixedList.forEach(obj -> {
                obj.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()).setPriority(MathUtil.ONE);
                obj.setCfgRuleCalcId(cfgRuleCalcId);
            });
            list.addAll(fixedList);
        }
        return list;
    }
}
