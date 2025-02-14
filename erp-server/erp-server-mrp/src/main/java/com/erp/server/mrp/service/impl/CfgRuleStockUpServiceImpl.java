package com.erp.server.mrp.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStockingRatioDTO;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleStockUpMapper;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;


    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = {"cache:mrp:getDefaultCfgRuleStockUp", "cache:mrp:getDefaultByPlatform"}, allEntries = true, beforeInvocation = true)
    public Boolean update(CfgRuleStockUpDTO.UpdateDTO updateDTO) {
        CfgRuleStockUpEntity cfgRuleStockUpEntity = BeanMapperUtils.map(CfgRuleStockUpEntity.class, updateDTO);
        //旧数据
        CfgRuleStockUpEntity old = this.getDefaultByPlatformType(updateDTO.getPlatform(), updateDTO.getRefId());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleStockUpEntity.setId(old.getId());
        }
        // 数据处理
        handleData(cfgRuleStockUpEntity, old, updateDTO.getIsCustom());

        boolean save = super.saveOrUpdate(cfgRuleStockUpEntity);
        if (!save) {
            throw new ServiceException("备货（规则设置）保存失败");
        }
        //常规备货系数
        cfgRuleStockingRatioService.update(updateDTO.getStockingRatioList(), cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode(), updateDTO.getIsCustom());
        //新品备货系数
        cfgRuleStockingRatioService.update(updateDTO.getNewStockingRatioList(), cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.NEW.getCode(), updateDTO.getIsCustom());

        // 记录主单操作日志
        log.info("编辑 开始记录备货（规则设置）日志数据，id：【{}】", cfgRuleStockUpEntity.getId());
        operateLogService.addModuleOperateLogByObj(ObjectUtil.isEmpty(old) ? new CfgRuleStockUpEntity() : old, cfgRuleStockUpEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleStockUpEntity.getRefId(), cfgRuleStockUpEntity.getId()), "");
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleStockUpDTO.ViewDTO view(String platform, String refId) {
        CfgRuleStockUpDTO.ViewDTO viewDTO = new CfgRuleStockUpDTO.ViewDTO();
        CfgRuleStockUpEntity oldEntity = this.getRefByPlatformType(platform, refId);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity, viewDTO);
        List<CfgRuleStockingRatioEntity> stockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(Collections.singletonList(oldEntity.getId()));
        //常规品
        List<CfgRuleStockingRatioEntity> oldList = stockingRatioList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oldList)) {
            List<CfgRuleStockingRatioDTO.ViewDTO> oldStockingRatioList = BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.ViewDTO.class, oldList);
            viewDTO.setStockingRatioList(oldStockingRatioList);
        }
        //新品
        List<CfgRuleStockingRatioEntity> newList = stockingRatioList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), CfgRuleStockingRatioTypeEnum.NEW.getCode())).collect(Collectors.toList());
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
        ApplicationContextUtils.getBean(CfgRuleStockUpServiceImpl.class).removeById(cfgRuleStockUpEntity.getId());
        //删除备货系数配置
        cfgRuleStockingRatioService.deleteByStockUpId(cfgRuleStockUpEntity.getId());
    }

    @Override
    public void customUpdate(CfgRuleStockUpDTO.CustomUpdateDTO stockUpUpdateDTO) {
        CfgRuleStockUpDTO.UpdateDTO updateDTO = BeanMapperUtils.map(CfgRuleStockUpDTO.UpdateDTO.class, stockUpUpdateDTO);
        ApplicationContextUtils.getBean(CfgRuleStockUpServiceImpl.class).update(updateDTO);
    }

    /**
     * 根据来源id查询
     *
     * @param refId
     * @return CfgRuleStockUpEntity
     * @author will
     * @date 2024/8/29 16:20
     */
    @Override
    public CfgRuleStockUpEntity getByRefId(String refId) {
        return lambdaQuery().eq(CfgRuleStockUpEntity::getRefId, refId).last("limit 1").one();
    }

    @Override
    public List<CfgRuleStockUpEntity> listByRefIdList(List<String> refIdList) {
        if (CollectionUtils.isEmpty(refIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgRuleStockUpEntity::getRefId, refIdList).list();
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:getDefaultByPlatform", keyGenerator = "myKeyGenerator")
    public CfgRuleStockUpEntity getDefaultByPlatform(String platformType) {
        return getDefaultByPlatformType(platformType, "");
    }

    /**
     * 根据平台类型查询
     *
     * @param platformType
     * @return CfgRuleStockUpEntity
     * @author will
     * @date 2024/8/24 9:30
     */
    private CfgRuleStockUpEntity getDefaultByPlatformType(String platformType, String refId) {
        return lambdaQuery().eq(CfgRuleStockUpEntity::getPlatform, platformType)
                .eq(CfgRuleStockUpEntity::getRefId, CharSequenceUtil.isBlank(refId) ? "" : refId)
                .last("limit 1")
                .one();
    }

    /**
     * 查询来源查询
     *
     * @param platformType
     * @param refId
     * @return CfgRuleStockUpEntity
     * @author will
     * @date 2024/9/6 11:41
     */
    private CfgRuleStockUpEntity getRefByPlatformType(String platformType, String refId) {
        CfgRuleStockUpEntity refEntity = getDefaultByPlatformType(platformType, refId);
        if (ObjectUtil.isNotEmpty(refEntity)) {
            return refEntity;
        }
        return getDefaultByPlatformType(platformType, "");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgRuleStockUpEntity cfgRuleStockUpEntity, CfgRuleStockUpEntity old, Boolean isCustom) {
        checkStockingRation(cfgRuleStockUpEntity);
        //自定义的需要赋值，避免生成变更日志
        if (Boolean.TRUE.equals(isCustom) && ObjectUtil.isNotEmpty(old)) {
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getPlatformSafeDays())) {
                cfgRuleStockUpEntity.setPlatformSafeDays(old.getPlatformSafeDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getOverseasSafeDays())) {
                cfgRuleStockUpEntity.setOverseasSafeDays(old.getOverseasSafeDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getStockingRatio())) {
                cfgRuleStockUpEntity.setStockingRatio(old.getStockingRatio());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getNewStockingRatio())) {
                cfgRuleStockUpEntity.setNewStockingRatio(old.getNewStockingRatio());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getIsCfgSame())) {
                cfgRuleStockUpEntity.setIsCfgSame(old.getIsCfgSame());
            }
        }

    }


    /**
     * 校验系数
     *
     * @param cfgRuleStockUpEntity 备货系数
     */
    private static void checkStockingRation(CfgRuleStockUpEntity cfgRuleStockUpEntity) {
        if (MathUtil.compareTo(cfgRuleStockUpEntity.getStockingRatio(), new BigDecimal(99)) > MathUtil.ZERO ||
                MathUtil.compareTo(cfgRuleStockUpEntity.getStockingRatio(), MathUtil.ZERO) < MathUtil.ZERO) {
            throw new ServiceException("常规品备货系数必须大于等于0，并且小于等于99");
        }
        if (MathUtil.compareTo(cfgRuleStockUpEntity.getNewStockingRatio(), new BigDecimal(99)) > MathUtil.ZERO ||
                MathUtil.compareTo(cfgRuleStockUpEntity.getNewStockingRatio(), MathUtil.ZERO) < MathUtil.ZERO) {
            throw new ServiceException("新品备货系数必须大于等于0，并且小于等于99");
        }
    }
}
