package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.*;
import com.erp.model.mrp.enums.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesQtyMapper;
import com.erp.server.mrp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销量（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesQtyServiceImpl extends SuperServiceImpl<CfgRuleSalesQtyMapper, CfgRuleSalesQtyEntity> implements CfgRuleSalesQtyService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;

    @Resource
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;

    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(CfgRuleSalesQtyDTO.UpdateDTO updateDTO) {
        CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity = new CfgRuleSalesQtyEntity();
        BeanUtils.copyProperties(updateDTO, cfgRuleSalesQtyEntity);
        cfgRuleSalesQtyEntity.setOrderType(JSONUtil.parseArray(updateDTO.getOrderType()));
        cfgRuleSalesQtyEntity.setOrderTypeName(getOrderTypeName(updateDTO.getPlatform(), updateDTO.getOrderType()));
        //旧数据
        CfgRuleSalesQtyEntity oldEntity = this.getDefaultByPlatformType(updateDTO.getPlatform(),updateDTO.getRefId());
        if (ObjectUtil.isNotEmpty(oldEntity)) {
            cfgRuleSalesQtyEntity.setId(oldEntity.getId());
            oldEntity.setOrderTypeName(getOrderTypeName(oldEntity.getPlatform(), oldEntity.getOrderType().stream().map(Object::toString).collect(Collectors.toList())));
        }
        // 数据处理
        handleData(cfgRuleSalesQtyEntity,ObjectUtil.isNotEmpty(oldEntity) ? oldEntity : null, updateDTO.getIsCustom());

        boolean save = super.saveOrUpdate(cfgRuleSalesQtyEntity);
        if(!save) {
            throw new ServiceException("销量（规则设置）保存失败");
        }

        //销量信息调整
        List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = handleSalesFormula(updateDTO.getDefaultSalesQtyDTO(), updateDTO.getDynamicSalesQtyList(), updateDTO.getFixedSalesQtyList());
        cfgRuleSalesFormulaService.update(salesFormulaList,cfgRuleSalesQtyEntity,CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode(), updateDTO.getIsCustom());
        //新品销量信息调整
        if (Boolean.TRUE.equals(updateDTO.getIsCfgSameDefault())) {
            updateDTO.getDefaultNewSalesQtyDTO().setId(null);
        }
        if (Boolean.TRUE.equals(updateDTO.getIsCfgSameDynamic())) {
            updateDTO.getDynamicNewSalesQtyList().forEach(obj -> obj.setId(null));
        }
        List<CfgRuleSalesFormulaDTO.UpdateDTO> salesNewFormulaList = handleSalesFormula(updateDTO.getDefaultNewSalesQtyDTO(), updateDTO.getDynamicNewSalesQtyList(), updateDTO.getFixedSalesQtyList());
        cfgRuleSalesFormulaService.update(salesNewFormulaList,cfgRuleSalesQtyEntity,CfgRuleStockingRatioTypeEnum.NEW.getCode(), updateDTO.getIsCustom());

        //销量去噪
        cfgRuleSalesDenoisingService.update(updateDTO.getSalesDenoisingList(),cfgRuleSalesQtyEntity, CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode(),updateDTO.getIsCustom());
        //新品去噪信息处理
        if (Boolean.TRUE.equals(updateDTO.getIsCfgSameDenoising())) {
            updateDTO.getNewSalesDenoisingList().forEach(obj -> obj.setId(null));
        }
        cfgRuleSalesDenoisingService.update(updateDTO.getNewSalesDenoisingList(),cfgRuleSalesQtyEntity, CfgRuleStockingRatioTypeEnum.NEW.getCode(), updateDTO.getIsCustom());
        // 记录主单操作日志
        log.info("编辑 开始记录销量（规则设置）日志数据，id：【{}】", cfgRuleSalesQtyEntity.getId());
        operateLogService.addModuleOperateLogByObj(ObjectUtil.isEmpty(oldEntity) ? new CfgRuleSalesQtyEntity () : oldEntity, cfgRuleSalesQtyEntity,
                ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleSalesQtyEntity.getRefId(),cfgRuleSalesQtyEntity.getId()), "");
        return Boolean.TRUE;
    }

    /**
     * 处理orderType日志
     * @param platformType 平台类型
     * @param orderType 订单类型
     */
    private String getOrderTypeName(String platformType, List<String> orderType) {
        List<CfgPlatformMappingEntity> platformMapping = cfgPlatformMappingService.list();
        Map<String, String> platformMap = platformMapping.stream()
                .collect(Collectors.toMap(CfgPlatformMappingEntity::getPlatform, CfgPlatformMappingEntity::getType, (o1, o2) -> o1));
        if (CfgRulePlatformTypeEnum.AMAZON.getCode().equals(platformMap.get(platformType))) {
            return orderType.stream()
                    .map(FbaOrderTypeEnum::getNameByCode)
                    .collect(Collectors.joining(","));

        } else if (CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(platformMap.get(platformType))) {
            return orderType.stream()
                    .map(OverseasOrderTypeEnum::getNameByCode)
                    .collect(Collectors.joining(","));
        }
        return null;
    }

    @Override
    public CfgRuleSalesQtyDTO.ViewDTO view(String platformType) {
        CfgRuleSalesQtyDTO.ViewDTO viewDTO = new CfgRuleSalesQtyDTO.ViewDTO();
        //销量信息
        CfgRuleSalesQtyEntity entity = this.getDefaultByPlatformType(platformType,"");
        if (ObjectUtil.isEmpty(entity)) {
            return viewDTO;
        }
        BeanUtils.copyProperties(entity,viewDTO);
        viewDTO.setOrderType(entity.getOrderType().stream().map(Object::toString).collect(Collectors.toList()));

        //日销量数据
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(Collections.singletonList(entity.getId()));
        List<CfgRuleSalesFormulaDTO.ViewDTO> salesViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, salesFormulaList);
        Map<String, Map<String, List<CfgRuleSalesFormulaDTO.ViewDTO>>> salesMap = salesViewList.stream()
                        .collect(Collectors.groupingBy(
                                CfgRuleSalesFormulaDTO.ViewDTO::getType,
                                Collectors.groupingBy(CfgRuleSalesFormulaDTO.ViewDTO::getSkuType)
                        ));
        //默认日销量
        Map<String, List<CfgRuleSalesFormulaDTO.ViewDTO>> defaultSalesFormula = salesMap.get(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode());
        if (ObjectUtil.isNotEmpty(defaultSalesFormula)) {
            viewDTO.setDefaultSalesQtyDTO(defaultSalesFormula.get(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode()).stream().findFirst().orElse(null));
            viewDTO.setDefaultNewSalesQtyDTO(defaultSalesFormula.get(CfgRuleStockingRatioTypeEnum.NEW.getCode()).stream().findFirst().orElse(null));
        }
        //动态日销量
        Map<String, List<CfgRuleSalesFormulaDTO.ViewDTO>> dynamicSalesFormulaList = salesMap.get(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode());
        if (ObjectUtil.isNotEmpty(dynamicSalesFormulaList)) {
            viewDTO.setDynamicSalesQtyList(dynamicSalesFormulaList.get(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode()));
            viewDTO.setDynamicNewSalesQtyList(dynamicSalesFormulaList.get(CfgRuleStockingRatioTypeEnum.NEW.getCode()));
        }
        //固定日销量
        Map<String, List<CfgRuleSalesFormulaDTO.ViewDTO>> fixedSalesFormulaList = salesMap.get(CfgRuleSalesFormulaTypeEnum.FIXED.getCode());
        if (ObjectUtil.isNotEmpty(fixedSalesFormulaList)) {
            viewDTO.setFixedSalesQtyList(defaultSalesFormula.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        }
        //销量去噪
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(Collections.singletonList(entity.getId()));
        List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingViewList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingDTO.ViewDTO.class, salesDenoisingList);
        Map<String, List<CfgRuleSalesDenoisingDTO.ViewDTO>> salesDenoisingViewMap = salesDenoisingViewList.stream().collect(Collectors.groupingBy(CfgRuleSalesDenoisingDTO.ViewDTO::getSkuType));
        viewDTO.setSalesDenoisingList(salesDenoisingViewMap.get(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode()));
        viewDTO.setNewSalesDenoisingList(salesDenoisingViewMap.get(CfgRuleStockingRatioTypeEnum.NEW.getCode()));
        return viewDTO;
    }


    @Override
    public CfgRuleSalesQtyDTO.ViewDetailDTO viewDetail(String refId) {

        //销量信息
        CfgRuleSalesQtyEntity entity = getDefaultByPlatformType(null, refId);
        CfgRuleSalesQtyDTO.ViewDetailDTO viewDetailDTO = new CfgRuleSalesQtyDTO.ViewDetailDTO();
        if (ObjectUtils.isEmpty(entity)) {
            return viewDetailDTO;
        }
        BeanMapperUtils.copy(entity,viewDetailDTO);
        //日销量数据
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(Collections.singletonList(entity.getId()));
        List<CfgRuleSalesFormulaDTO.ViewDTO> salesViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, salesFormulaList);
        Map<String, List<CfgRuleSalesFormulaDTO.ViewDTO>> salesMap = salesViewList.stream()
                .collect(Collectors.groupingBy(CfgRuleSalesFormulaDTO.ViewDTO::getType));

        //默认日销量
        List<CfgRuleSalesFormulaDTO.ViewDTO> defaultSalesFormula = salesMap.get(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode());
        if (CollectionUtils.isNotEmpty(defaultSalesFormula)) {
            CfgRuleSalesFormulaDTO.ViewDTO defaultViewDTO = defaultSalesFormula.stream().findFirst().orElse(null);
            viewDetailDTO.setDefaultSalesQtyDTO(defaultViewDTO);
        }
        //动态日销量
        viewDetailDTO.setDynamicSalesQtyList(salesMap.get(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()));
        //固定日销量
        viewDetailDTO.setFixedSalesQtyList(salesMap.get(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()));
        //销量去噪
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(Collections.singletonList(entity.getId()));
        //去噪信息
        List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingViewList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingDTO.ViewDTO.class, salesDenoisingList);
        viewDetailDTO.setSalesDenoisingList(salesDenoisingViewList);
        return viewDetailDTO;
    }

    @Override
    public CfgRuleSalesQtyEntity getDefaultCfgRuleSalesQty(String platformType) {
        return getOne(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery()
                .eq(CfgRuleSalesQtyEntity::getPlatform, platformType)
                .eq(CfgRuleSalesQtyEntity::getRefId, "")
                .last("LIMIT 1")
        );
    }


    @Override
    public List<CfgRuleSalesQtyEntity> listDefaultCfgRuleSalesQty(String platformType) {
        return list(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery()
                .eq(CfgRuleSalesQtyEntity::getPlatform, platformType)
                .eq(CfgRuleSalesQtyEntity::getRefId, "")
        );
    }

    @Override
    public CfgRuleSalesQtyEntity getDefaultByPlatformAndSkuType(String platformType, String skuType) {
        return getOne(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery().eq(CfgRuleSalesQtyEntity::getRefId, "")
                .eq(CfgRuleSalesQtyEntity::getPlatform, platformType)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCfgData(List<CfgRuleSalesFormulaCalcEntity> cfgRuleSalesFormulaList, List<CfgRuleSalesDenoisingCalcEntity> cfgRuleSalesDenoisingList, List<ReplenishmentSuggestionEntity> suggestionList, String code) {
        for (ReplenishmentSuggestionEntity suggestion : suggestionList) {
            //移除旧数据
            deleteByRefId(suggestion.getId());
            //保存新数据
            CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity = new CfgRuleSalesQtyEntity();
            cfgRuleSalesQtyEntity.setId(IdWorker.getIdStr());
            cfgRuleSalesQtyEntity.setSalesQtyType(SalesQtyTypeEnum.BY_CREATE_TIME.getCode());
            cfgRuleSalesQtyEntity.setPlatform(suggestion.getPlatform());
            cfgRuleSalesQtyEntity.setRefId(suggestion.getId());
            cfgRuleSalesQtyEntity.setRefType(SourceTypeEnum.REPLENISHMENT_SUGGESTION.getCode());
            save(cfgRuleSalesQtyEntity);
            List<CfgRuleSalesFormulaEntity> ruleSalesFormulaList = cfgRuleSalesFormulaList.stream()
                    .map(v -> {
                        CfgRuleSalesFormulaEntity dto = new CfgRuleSalesFormulaEntity();
                        BeanUtils.copyProperties(v, dto);
                        dto.setSalesQtyId(cfgRuleSalesQtyEntity.getId());
                        dto.setId(null);
                        return dto;
                    })
                    .collect(Collectors.toList());
            cfgRuleSalesFormulaService.saveBatch(ruleSalesFormulaList);
            List<CfgRuleSalesDenoisingEntity> ruleSalesDenoisingList = cfgRuleSalesDenoisingList.stream()
                    .map(v -> {
                        CfgRuleSalesDenoisingEntity dto = new CfgRuleSalesDenoisingEntity();
                        BeanUtils.copyProperties(v, dto);
                        dto.setSalesQtyId(cfgRuleSalesQtyEntity.getId());
                        dto.setId(null);
                        return dto;
                    })
                    .collect(Collectors.toList());
            cfgRuleSalesDenoisingService.saveBatch(ruleSalesDenoisingList);
            String msg = CharSequenceUtil.format("从销量试算【试算编号：{}】应用了规则", code);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), suggestion.getId(), "应用");
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRefId(String refId) {
        //销量数据
        CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity = getByRefId(refId);
        if (ObjectUtil.isEmpty(cfgRuleSalesQtyEntity)) {
            return;
        }
        ApplicationContextUtils.getBean(CfgRuleSalesQtyServiceImpl.class).removeById(cfgRuleSalesQtyEntity.getId());

        //删除销量信息
        cfgRuleSalesFormulaService.deleteBySalesQtyId(cfgRuleSalesQtyEntity.getId());

        //删除销量去噪信息
        cfgRuleSalesDenoisingService.deleteBySalesQtyId(cfgRuleSalesQtyEntity.getId());
    }


    @Override
    public List<CfgRuleSalesQtyEntity> listByRefIdList(List<String> refIdList) {
        if (CollectionUtils.isEmpty(refIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgRuleSalesQtyEntity::getRefId,refIdList).list();
    }


    /**
     * 根据来源id查询
     * @author will
     * @date 2024/8/29 17:57
     * @param refId
     * @return CfgRuleSalesQtyEntity
     */
    @Override
    public CfgRuleSalesQtyEntity getByRefId (String refId) {
       return lambdaQuery().eq(CfgRuleSalesQtyEntity::getRefId,refId).last("limit 1").one();
    }

    /**
     * 销量信息合并
     * @param defaultSalesQtyDTO 默认销量
     * @param dynamicSalesQtyList 动态销量
     * @param fixedSalesQtyList 固定销量
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO> handleSalesFormula(CfgRuleSalesFormulaDTO.DefaultUpdateDTO defaultSalesQtyDTO,
                                                                      List<CfgRuleSalesFormulaDTO.DynamicUpdateDTO> dynamicSalesQtyList,
                                                                      List<CfgRuleSalesFormulaDTO.FixedUpdateDTO> fixedSalesQtyList) {
        List<CfgRuleSalesFormulaDTO.UpdateDTO> list = new ArrayList<>();
        //默认日销量
        if (ObjectUtil.isNotEmpty(defaultSalesQtyDTO)) {
            CfgRuleSalesFormulaDTO.UpdateDTO defaultDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.UpdateDTO.class, defaultSalesQtyDTO);
            defaultDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()).setPriority(MathUtil.THREE);
            list.add(defaultDTO);
        }
        //动态日销量
        if (CollectionUtils.isNotEmpty(dynamicSalesQtyList)) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> dynamicList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, dynamicSalesQtyList);
            dynamicList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()).setPriority(MathUtil.TWO));
            list.addAll(dynamicList);
        }
        //固定日销量
        if (CollectionUtils.isNotEmpty(fixedSalesQtyList)) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> fixedList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, fixedSalesQtyList);
            fixedList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()).setPriority(MathUtil.ONE));
            list.addAll(fixedList);
        }
        return list;
    }

    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 9:29
     * @param platform
     * @return List<CfgRuleSalesQtyEntity>
     */
    private CfgRuleSalesQtyEntity getDefaultByPlatformType(String platform, String refId) {
       return lambdaQuery()
               .eq(!ObjectUtils.isEmpty(platform), CfgRuleSalesQtyEntity::getPlatform, platform)
               .eq(CfgRuleSalesQtyEntity::getRefId,CharSequenceUtil.isNotBlank(refId) ? refId : "")
               .last("LIMIT 1")
               .one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity,CfgRuleSalesQtyEntity oldEntity,Boolean isCustom) {
        if (Boolean.TRUE.equals(isCustom) && ObjectUtil.isNotEmpty(oldEntity)) {
            if (ObjectUtil.isEmpty(cfgRuleSalesQtyEntity.getIsIgnoreOutOfStock())) {
                cfgRuleSalesQtyEntity.setIsIgnoreOutOfStock(oldEntity.getIsIgnoreOutOfStock());
            }
            if (CharSequenceUtil.isBlank(cfgRuleSalesQtyEntity.getSalesQtyType())) {
                cfgRuleSalesQtyEntity.setSalesQtyType(oldEntity.getSalesQtyType());
            }
            if (CollectionUtils.isNotEmpty(cfgRuleSalesQtyEntity.getOrderType())) {
                cfgRuleSalesQtyEntity.setOrderType(oldEntity.getOrderType());
            }
            if (ObjectUtil.isNotEmpty(cfgRuleSalesQtyEntity.getPlatform())) {
                cfgRuleSalesQtyEntity.setPlatform(oldEntity.getPlatform());
            }
            if (ObjectUtil.isNotEmpty(cfgRuleSalesQtyEntity.getIsCfgSameDefault())) {
                cfgRuleSalesQtyEntity.setIsCfgSameDefault(oldEntity.getIsCfgSameDefault());
            }
            if (ObjectUtil.isNotEmpty(cfgRuleSalesQtyEntity.getIsCfgSameDynamic())) {
                cfgRuleSalesQtyEntity.setIsCfgSameDynamic(oldEntity.getIsCfgSameDynamic());
            }
            if (ObjectUtil.isNotEmpty(cfgRuleSalesQtyEntity.getIsCfgSameDenoising())) {
                cfgRuleSalesQtyEntity.setIsCfgSameDenoising(oldEntity.getIsCfgSameDenoising());
            }
            if (ObjectUtil.isNotEmpty(cfgRuleSalesQtyEntity.getSalesEstimateType())) {
                cfgRuleSalesQtyEntity.setSalesEstimateType(oldEntity.getSalesEstimateType());
            }
        }
    }
}
