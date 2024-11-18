package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionDetailEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesQtyMapper;
import com.erp.server.mrp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private ReplenishmentSuggestionDetailService replenishmentSuggestionDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(CfgRuleSalesQtyDTO.UpdateDTO updateDTO) {
        Boolean isCfgSame = updateDTO.getIsCfgSame();

        //编辑常规品
        updateDTO.getConventionalDetail().setIsCfgSame(isCfgSame).setType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
        ApplicationContextUtils.getBean(CfgRuleSalesQtyServiceImpl.class).update(updateDTO.getConventionalDetail());

        //编辑新品
        updateDTO.getNewDetail().setIsCfgSame(isCfgSame).setType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
        updateDTO.getNewDetail().getDynamicSalesQtyList().forEach(obj -> obj.setId(null));
        updateDTO.getNewDetail().getFixedSalesQtyList().forEach(obj -> obj.setId(null));
        updateDTO.getNewDetail().getSalesDenoisingList().forEach(obj -> obj.setId(null));
        updateDTO.getNewDetail().getDefaultSalesQtyDTO().setId(null);
        ApplicationContextUtils.getBean(CfgRuleSalesQtyServiceImpl.class).update(updateDTO.getNewDetail());
        return Boolean.TRUE;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String update(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetailDTO) {
        CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity =  BeanMapperUtils.map(CfgRuleSalesQtyEntity.class, updateDetailDTO);
        //旧数据
        List<CfgRuleSalesQtyEntity> oldList = this.getDefaultByPlatformType(updateDetailDTO.getPlatformType(),updateDetailDTO.getRefId(),updateDetailDTO.getType());
        if (CollectionUtils.isNotEmpty(oldList)) {
            cfgRuleSalesQtyEntity.setId(oldList.get(0).getId());
        }

        // 数据处理
        handleData(cfgRuleSalesQtyEntity,CollectionUtils.isNotEmpty(oldList) ? oldList.get(0) : null,updateDetailDTO.getIsCustom());

        boolean save = super.saveOrUpdate(cfgRuleSalesQtyEntity);
        if(!save) {
            throw new ServiceException("销量（规则设置）保存失败");
        }

        //销量信息调整
        List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = handleSalesFormula(updateDetailDTO);
        cfgRuleSalesFormulaService.update(salesFormulaList,cfgRuleSalesQtyEntity.getId(),updateDetailDTO.getIsCustom());

        //销量去噪
        cfgRuleSalesDenoisingService.update(updateDetailDTO.getSalesDenoisingList(),cfgRuleSalesQtyEntity.getId(),updateDetailDTO.getIsCustom());

        if (CollectionUtils.isEmpty(oldList)) {
            return cfgRuleSalesQtyEntity.getId();
        }
        // 记录主单操作日志
        log.info("编辑 开始记录销量（规则设置）日志数据，id：【{}】", cfgRuleSalesQtyEntity.getId());
        operateLogService.addModuleOperateLogByObj(oldList.get(0), cfgRuleSalesQtyEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleSalesQtyEntity.getRefId(),cfgRuleSalesQtyEntity.getId()), "");
        return cfgRuleSalesQtyEntity.getId();
    }

    @Override
    public CfgRuleSalesQtyDTO.ViewDTO view(String platformType) {
        CfgRuleSalesQtyDTO.ViewDTO viewDTO = new CfgRuleSalesQtyDTO.ViewDTO();

        //销量信息
        List<CfgRuleSalesQtyEntity> list = this.getDefaultByPlatformType(platformType,"","");
        if (CollectionUtils.isEmpty(list)) {
            return  viewDTO;
        }
        List<String> salesQtyIdList = list.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());

        //日销量数据
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(salesQtyIdList);

        //销量去噪
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(salesQtyIdList);

        for (CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity : list) {
            CfgRuleSalesQtyDTO.ViewDetailDTO viewDetailDTO = new CfgRuleSalesQtyDTO.ViewDetailDTO();
            BeanMapperUtils.copy(cfgRuleSalesQtyEntity,viewDetailDTO);
            //默认日销量
            CfgRuleSalesFormulaEntity defaultSalesFormula = salesFormulaList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(defaultSalesFormula)) {
                CfgRuleSalesFormulaDTO.ViewDTO defaultViewDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.ViewDTO.class, defaultSalesFormula);
                viewDetailDTO.setDefaultSalesQtyDTO(defaultViewDTO);
            }
            //动态日销量
            List<CfgRuleSalesFormulaEntity> dynamicSalesFormulaList = salesFormulaList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dynamicSalesFormulaList)) {
                List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, dynamicSalesFormulaList);
                viewDetailDTO.setDynamicSalesQtyList(dynamicViewList);
            }
            //固定日销量
            List<CfgRuleSalesFormulaEntity> fixedSalesFormulaList = salesFormulaList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fixedSalesFormulaList)) {
                List<CfgRuleSalesFormulaDTO.ViewDTO> fixedViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, fixedSalesFormulaList);
                viewDetailDTO.setFixedSalesQtyList(fixedViewList);
            }
            //去噪信息
            setDenoising(cfgRuleSalesQtyEntity, salesDenoisingList, viewDetailDTO);
            //明细赋值
            if (CharSequenceUtil.equals(cfgRuleSalesQtyEntity.getType(),CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())) {
                viewDTO.setConventionalDetail(viewDetailDTO);
            } else {
                viewDTO.setNewDetail(viewDetailDTO);
            }
        }
        viewDTO.setIsCfgSame(list.get(0).getIsCfgSame());
        return viewDTO;
    }

    /**
     * 设置去噪信息
     * @param cfgRuleSalesQtyEntity 销量
     * @param salesDenoisingList 去噪信息
     * @param viewDetailDTO 返回结果
     */
    private static void setDenoising(CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity, List<CfgRuleSalesDenoisingEntity> salesDenoisingList, CfgRuleSalesQtyDTO.ViewDetailDTO viewDetailDTO) {
        List<CfgRuleSalesDenoisingEntity> denoisingList = salesDenoisingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSalesQtyId(), cfgRuleSalesQtyEntity.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(denoisingList)) {
            List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingViewList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingDTO.ViewDTO.class, denoisingList);
            viewDetailDTO.setSalesDenoisingList(salesDenoisingViewList);
        }
    }

    @Override
    public CfgRuleSalesQtyDTO.ViewDetailDTO viewDetail(String platformType, String refId) {

        //销量信息
        CfgRuleSalesQtyEntity entity = this.getRefByPlatformType(platformType,refId,"");
        //日销量数据
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(Arrays.asList(entity.getId()));
        //销量去噪
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(Arrays.asList(entity.getId()));

        CfgRuleSalesQtyDTO.ViewDetailDTO viewDetailDTO = new CfgRuleSalesQtyDTO.ViewDetailDTO();
        BeanMapperUtils.copy(entity,viewDetailDTO);
        //默认日销量
        CfgRuleSalesFormulaEntity defaultSalesFormula = salesFormulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),entity.getId())
        ).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(defaultSalesFormula)) {
            CfgRuleSalesFormulaDTO.ViewDTO defaultViewDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.ViewDTO.class, defaultSalesFormula);
            viewDetailDTO.setDefaultSalesQtyDTO(defaultViewDTO);
        }
        //动态日销量
        List<CfgRuleSalesFormulaEntity> dynamicSalesFormulaList = salesFormulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),entity.getId())
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dynamicSalesFormulaList)) {
            List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, dynamicSalesFormulaList);
            viewDetailDTO.setDynamicSalesQtyList(dynamicViewList);
        }
        //固定日销量
        List<CfgRuleSalesFormulaEntity> fixedSalesFormulaList = salesFormulaList.stream().filter(obj ->
                CharSequenceUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode()) && CharSequenceUtil.equals(obj.getSalesQtyId(),entity.getId())
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fixedSalesFormulaList)) {
            List<CfgRuleSalesFormulaDTO.ViewDTO> fixedViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, fixedSalesFormulaList);
            viewDetailDTO.setFixedSalesQtyList(fixedViewList);
        }
        //去噪信息
        setDenoising(entity, salesDenoisingList, viewDetailDTO);
        return viewDetailDTO;
    }

    @Override
    public CfgRuleSalesQtyEntity getDefaultCfgRuleSalesQty(String platformType) {
        return getOne(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery()
                .eq(CfgRuleSalesQtyEntity::getPlatformType, platformType)
                .eq(CfgRuleSalesQtyEntity::getRefId, "")
                .last("LIMIT 1")
        );
    }


    @Override
    public List<CfgRuleSalesQtyEntity> listDefaultCfgRuleSalesQty(String platformType) {
        return list(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery()
                .eq(CfgRuleSalesQtyEntity::getPlatformType, platformType)
                .eq(CfgRuleSalesQtyEntity::getRefId, "")
        );
    }

    @Override
    public CfgRuleSalesQtyEntity getDefaultByPlatformAndSkuType(String platformType, String skuType) {
        return getOne(Wrappers.<CfgRuleSalesQtyEntity>lambdaQuery().eq(CfgRuleSalesQtyEntity::getRefId, "")
                .eq(CfgRuleSalesQtyEntity::getPlatformType, platformType)
                .eq(CfgRuleSalesQtyEntity::getType, skuType)
                .last("LIMIT 1"));
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
     * @author will
     * @date 2024/8/27 9:49
     * @param updateDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO> handleSalesFormula (CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO) {
        List<CfgRuleSalesFormulaDTO.UpdateDTO> list = new ArrayList<>();
        //默认日销量
        if (ObjectUtil.isNotEmpty(updateDTO.getDefaultSalesQtyDTO())) {
            CfgRuleSalesFormulaDTO.UpdateDTO defaultDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getDefaultSalesQtyDTO());
            defaultDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()).setPriority(MathUtil.THREE);
            list.add(defaultDTO);
        }
        //动态日销量
        if (CollectionUtils.isNotEmpty(updateDTO.getDynamicSalesQtyList())) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> dynamicList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getDynamicSalesQtyList());
            dynamicList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()).setPriority(MathUtil.TWO));
            list.addAll(dynamicList);
        }
        //固定日销量
        if (CollectionUtils.isNotEmpty(updateDTO.getFixedSalesQtyList())) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> fixedList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getFixedSalesQtyList());
            fixedList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()).setPriority(MathUtil.ONE));
            list.addAll(fixedList);
        }
        return list;
    }

    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 9:29
     * @param platformType
     * @return List<CfgRuleSalesQtyEntity>
     */
    private List<CfgRuleSalesQtyEntity> getDefaultByPlatformType(String platformType, String refId, String type) {
       return lambdaQuery()
               .eq(CfgRuleSalesQtyEntity::getPlatformType,platformType)
               .eq(CharSequenceUtil.isNotBlank(refId),CfgRuleSalesQtyEntity::getRefId,refId)
               .eq(CharSequenceUtil.isBlank(refId),CfgRuleSalesQtyEntity::getRefId,"")
               .eq(CharSequenceUtil.isNotBlank(type),CfgRuleSalesQtyEntity::getType,type)
               .list();
    }

    /**
     * 查询来源查询
     * @author will
     * @date 2024/9/6 11:41
     * @param platformType
     * @param refId
     * @return CfgRuleStockUpEntity
     */
    private CfgRuleSalesQtyEntity getRefByPlatformType (String platformType, String refId, String type) {

        List<CfgRuleSalesQtyEntity> refEntityList = getDefaultByPlatformType(platformType, refId,type);
        if (CollectionUtils.isNotEmpty(refEntityList)) {
            return refEntityList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())).findFirst().orElse(new CfgRuleSalesQtyEntity());
        }
        List<CfgRuleSalesQtyEntity> defaultList = getDefaultByPlatformType(platformType, "", type);
        if (CollectionUtils.isEmpty(defaultList)) {
            return new CfgRuleSalesQtyEntity();
        }
        //查询建议明细
        List<ReplenishmentSuggestionDetailEntity> replenishmentSuggestionDetailList = replenishmentSuggestionDetailService.listByMainIdList(Arrays.asList(refId));
        if (CollectionUtils.isEmpty(replenishmentSuggestionDetailList)) {
            throw new ServiceException("补货建议明细未找到");
        }
        return defaultList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(),replenishmentSuggestionDetailList.get(0).getSkuType())).findFirst().orElse(new CfgRuleSalesQtyEntity());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity,CfgRuleSalesQtyEntity oldEntity,Boolean isCustom) {
        if (Boolean.TRUE.equals(isCustom) && ObjectUtil.isNotEmpty(oldEntity)) {
            if (ObjectUtil.isEmpty(cfgRuleSalesQtyEntity.getIsCfgSame())) {
                cfgRuleSalesQtyEntity.setIsCfgSame(oldEntity.getIsCfgSame());
            }
            if (ObjectUtil.isEmpty(cfgRuleSalesQtyEntity.getIsIgnoreOutOfStock())) {
                cfgRuleSalesQtyEntity.setIsIgnoreOutOfStock(oldEntity.getIsIgnoreOutOfStock());
            }
            if (CharSequenceUtil.isBlank(cfgRuleSalesQtyEntity.getSalesQtyType())) {
                cfgRuleSalesQtyEntity.setSalesQtyType(oldEntity.getSalesQtyType());
            }
            if (CharSequenceUtil.isBlank(cfgRuleSalesQtyEntity.getOrderType())) {
                cfgRuleSalesQtyEntity.setOrderType(oldEntity.getOrderType());
            }
        }
    }
}
