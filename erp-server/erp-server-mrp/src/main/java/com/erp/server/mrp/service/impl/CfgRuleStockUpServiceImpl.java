package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleStockUpMapper;
import com.erp.server.mrp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 备货（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleStockUpServiceImpl extends SuperServiceImpl<CfgRuleStockUpMapper, CfgRuleStockUpEntity> implements CfgRuleStockUpService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    @Autowired
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;

    @Autowired
    private CfgRuleLogisticsDetailService cfgRuleLogisticsDetailService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleStockUpDTO.UpdateDTO updateDTO) {
        CfgRuleStockUpEntity cfgRuleStockUpEntity =  BeanMapperUtils.map(CfgRuleStockUpEntity.class, updateDTO);
        //旧数据
        CfgRuleStockUpEntity old = this.getDefaultByPlatformType(updateDTO.getPlatformType(),updateDTO.getRefId());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleStockUpEntity.setId(old.getId());
        }
        // 数据处理
        handleData(cfgRuleStockUpEntity,old,updateDTO.getIsCustom());

        boolean save = super.saveOrUpdate(cfgRuleStockUpEntity);
        if(!save) {
            throw new ServiceException("备货（规则设置）保存失败");
        }
        //物流信息
        cfgRuleLogisticsService.update(updateDTO.getCfgLogisticsList(),cfgRuleStockUpEntity.getId(),updateDTO.getIsCustom());

        //常规备货系数
        cfgRuleStockingRatioService.update(updateDTO.getStockingRatioList(),cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode(),updateDTO.getIsCustom());
        //新品备货系数
        cfgRuleStockingRatioService.update(updateDTO.getNewStockingRatioList(),cfgRuleStockUpEntity.getId(),CfgRuleStockingRatioTypeEnum.NEW.getCode(),updateDTO.getIsCustom());

        // 记录主单操作日志
        log.info("编辑 开始记录备货（规则设置）日志数据，id：【{}】", cfgRuleStockUpEntity.getId());
        String msg = StrUtil.format("编辑【{}】 ",  "备货设置");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleStockUpEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(),StrUtil.blankToDefault(cfgRuleStockUpEntity.getRefId(),cfgRuleStockUpEntity.getId()), msg);
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleStockUpDTO.ViewDTO  view(String platformType,String refId) {
        CfgRuleStockUpDTO.ViewDTO viewDTO = new CfgRuleStockUpDTO.ViewDTO();
        CfgRuleStockUpEntity oldEntity = this.getRefByPlatformType(platformType,refId);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);

        //物流配置信息
        List<CfgRuleLogisticsDTO.ViewDTO> logisticsViewList = cfgRuleLogisticsService.listViewByStockUpIdList(Arrays.asList(oldEntity.getId()));
        if (CollectionUtils.isNotEmpty(logisticsViewList)) {
            viewDTO.setCfgLogisticsList(logisticsViewList);
        }
        List<CfgRuleStockingRatioEntity> stockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(Arrays.asList(oldEntity.getId()));
        //常规品
        List<CfgRuleStockingRatioEntity> oldList = stockingRatioList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oldList)) {
            List<CfgRuleStockingRatioDTO.ViewDTO> oldStockingRatioList = BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.ViewDTO.class, oldList);
            viewDTO.setStockingRatioList(oldStockingRatioList);
        }
        //新品
        List<CfgRuleStockingRatioEntity> newList = stockingRatioList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleStockingRatioTypeEnum.NEW.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(newList)) {
            List<CfgRuleStockingRatioDTO.ViewDTO> oldStockingRatioList = BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.ViewDTO.class, newList);
            viewDTO.setNewStockingRatioList(oldStockingRatioList);
        }
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRefId(String refId) {
        CfgRuleStockUpEntity cfgRuleStockUpEntity = getByRefId(refId);
        if (ObjectUtil.isEmpty(cfgRuleStockUpEntity)) {
            return;
        }
        this.removeById(cfgRuleStockUpEntity.getId());

        //删除物流信息配置
        cfgRuleLogisticsService.deleteByStockUpId(cfgRuleStockUpEntity.getId());

        //删除备货系数配置
        cfgRuleStockingRatioService.deleteByStockUpId(cfgRuleStockUpEntity.getId());
    }

    @Override
    public void customUpdate(CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO) {
        CfgRuleStockUpDTO.UpdateDTO updateDTO = BeanMapperUtils.map(CfgRuleStockUpDTO.UpdateDTO.class, stockUpUpdateDTO);
        this.update(updateDTO);
    }

    /**
     * 根据来源id查询
     * @author will
     * @date 2024/8/29 16:20
     * @param refId
     * @return CfgRuleStockUpEntity
     */
    @Override
    public CfgRuleStockUpEntity getByRefId(String refId) {
        return lambdaQuery().eq(CfgRuleStockUpEntity::getRefId,refId).last("limit 1").one();
    }

    @Override
    public List<CfgRuleStockUpEntity> listByRefIdList(List<String> refIdList) {
        if (CollectionUtils.isEmpty(refIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(CfgRuleStockUpEntity::getRefId,refIdList).list();
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:getDefaultCfgRuleStockUp",keyGenerator = "myKeyGenerator")
    public CfgRuleStockUpEntity getDefaultCfgRuleStockUp(String platformType) {
        return getDefaultByPlatformType(platformType,"");
    }

    @Override
    public List<CfgRuleStockUpEntity> getDefaultCfgRuleStockUp() {
        return list(Wrappers.<CfgRuleStockUpEntity>lambdaQuery().eq(CfgRuleStockUpEntity::getRefId,""));
    }

    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 9:30
     * @param platformType
     * @return CfgRuleStockUpEntity
     */
    private CfgRuleStockUpEntity getDefaultByPlatformType (String platformType,String refId) {
        return lambdaQuery().eq(CfgRuleStockUpEntity::getPlatformType,platformType)
                .eq(StrUtil.isNotBlank(refId),CfgRuleStockUpEntity::getRefId,refId)
                .eq(StrUtil.isBlank(refId),CfgRuleStockUpEntity::getRefId,"")
                .last("limit 1")
                .one();
    }

    /**
     * 查询来源查询
     * @author will
     * @date 2024/9/6 11:41
     * @param platformType
     * @param refId
     * @return CfgRuleStockUpEntity
     */
    private CfgRuleStockUpEntity getRefByPlatformType (String platformType,String refId) {
        CfgRuleStockUpEntity refEntity = getDefaultByPlatformType(platformType, refId);
        if (ObjectUtil.isNotEmpty(refEntity)) {
            return refEntity;
        }
        return getDefaultByPlatformType(platformType,"");
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleStockUpEntity cfgRuleStockUpEntity,CfgRuleStockUpEntity old,Boolean isCustom) {
        if (MathUtil.compareTo(cfgRuleStockUpEntity.getStockingRatio(),new BigDecimal(99)) > MathUtil.ZERO ||
                MathUtil.compareTo(cfgRuleStockUpEntity.getStockingRatio(),MathUtil.ZERO) < MathUtil.ZERO) {
            throw new ServiceException("常规品备货系数必须大于等于0，并且小于等于99");
        }
        if (MathUtil.compareTo(cfgRuleStockUpEntity.getNewStockingRatio(),new BigDecimal(99)) > MathUtil.ZERO ||
                MathUtil.compareTo(cfgRuleStockUpEntity.getNewStockingRatio(),MathUtil.ZERO) < MathUtil.ZERO) {
            throw new ServiceException("新品备货系数必须大于等于0，并且小于等于99");
        }
        //自定义的需要赋值，避免生成变更日志
        if (isCustom) {
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getPurchaseApproveDays())) {
                cfgRuleStockUpEntity.setPurchaseApproveDays(old.getPurchaseApproveDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getProductionDays())) {
                cfgRuleStockUpEntity.setProductionDays(old.getProductionDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getSupplierDeliveryDays())) {
                cfgRuleStockUpEntity.setSupplierDeliveryDays(old.getSupplierDeliveryDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getQcDays())) {
                cfgRuleStockUpEntity.setQcDays(old.getQcDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getPurchaseCycleDays())) {
                cfgRuleStockUpEntity.setPurchaseCycleDays(old.getPurchaseCycleDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getSafeDays())) {
                cfgRuleStockUpEntity.setSafeDays(old.getSafeDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getInstockDays())) {
                cfgRuleStockUpEntity.setInstockDays(old.getInstockDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getStockingRatio())) {
                cfgRuleStockUpEntity.setStockingRatio(old.getStockingRatio());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getNewStockingRatio())) {
                cfgRuleStockUpEntity.setNewStockingRatio(old.getNewStockingRatio());
            }
        }

    }
}
