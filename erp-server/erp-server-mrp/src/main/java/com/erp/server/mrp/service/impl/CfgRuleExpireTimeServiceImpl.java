package com.erp.server.mrp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleExpireTimeDTO;
import com.erp.model.mrp.dto.CfgRuleLogisticsDTO;
import com.erp.model.mrp.dto.CfgRuleOverseasInstockDaysDTO;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleExpireTimeMapper;
import com.erp.server.mrp.service.CfgRuleExpireTimeService;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleOverseasInstockDaysService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 时效配置表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
@Service
@Slf4j
public class CfgRuleExpireTimeServiceImpl extends SuperServiceImpl<CfgRuleExpireTimeMapper, CfgRuleExpireTimeEntity> implements CfgRuleExpireTimeService {

    @Resource
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    @Resource
    private CfgRuleOverseasInstockDaysService cfgRuleOverseasInstockDaysService;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public void update(CfgRuleExpireTimeDTO.UpdateDTO dto) {
        CfgRuleExpireTimeEntity cfgRuleExpireTime = BeanMapperUtils.map(CfgRuleExpireTimeEntity.class, dto);
        //旧数据
        CfgRuleExpireTimeEntity old = this.getDefaultByPlatformType(dto.getRefId());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleExpireTime.setId(old.getId());
        }
        // 数据处理
        handleData(cfgRuleExpireTime, old, dto.getIsCustom());

        boolean save = super.saveOrUpdate(cfgRuleExpireTime);
        if (!save) {
            throw new ServiceException("时效（规则设置）保存失败");
        }

        //物流信息
        cfgRuleLogisticsService.update(dto.getOverseasCfgLogisticsList(), cfgRuleExpireTime, dto.getIsBatch(), true);
        cfgRuleLogisticsService.update(dto.getPlatformCfgLogisticsList(), cfgRuleExpireTime, dto.getIsBatch(), false);
        //海外入库时间
        cfgRuleOverseasInstockDaysService.update(dto.getOverseasInstockDaysList(), cfgRuleExpireTime);

        // 记录主单操作日志
        log.info("编辑 开始记录时效（规则设置）日志数据，id：【{}】", cfgRuleExpireTime.getId());
        operateLogService.addModuleOperateLogByObj(ObjectUtil.isEmpty(old) ? new CfgRuleExpireTimeEntity() : old, cfgRuleExpireTime, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), CharSequenceUtil.blankToDefault(cfgRuleExpireTime.getRefId(), cfgRuleExpireTime.getId()), "");
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgRuleExpireTimeEntity cfgRuleStockUpEntity, CfgRuleExpireTimeEntity old, Boolean isCustom) {
        //自定义的需要赋值，避免生成变更日志
        if (Boolean.TRUE.equals(isCustom) && ObjectUtil.isNotEmpty(old)) {
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
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getPlatformInstockDays())) {
                cfgRuleStockUpEntity.setPlatformInstockDays(old.getPlatformInstockDays());
            }
            if (ObjectUtil.isEmpty(cfgRuleStockUpEntity.getOverseasInstockDays())) {
                cfgRuleStockUpEntity.setOverseasInstockDays(old.getOverseasInstockDays());
            }

        }

    }

    @Override
    public CfgRuleExpireTimeDTO.ViewDTO view() {

        CfgRuleExpireTimeDTO.ViewDTO viewDTO = new CfgRuleExpireTimeDTO.ViewDTO();
        CfgRuleExpireTimeEntity oldEntity = this.getRefByPlatformType("");
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity, viewDTO);
        //物流配置信息
        List<CfgRuleLogisticsDTO.ViewDTO> logisticsViewList = cfgRuleLogisticsService.listViewByExpireTimeIdList(Collections.singletonList(oldEntity.getId()));
        List<CfgRuleLogisticsDTO.ViewDTO> platformCfgLogisticsList = logisticsViewList.stream()
                .filter(v -> CfgRulePlatformTypeEnum.AMAZON.getCode().equals(v.getPlatformType()))
                .collect(Collectors.toList());
        viewDTO.setPlatformCfgLogisticsList(platformCfgLogisticsList);
        List<CfgRuleLogisticsDTO.ViewDTO> overseasCfgLogisticsList = logisticsViewList.stream()
                .filter(v -> CfgRulePlatformTypeEnum.OVERSEAS.getCode().equals(v.getPlatformType()))
                .collect(Collectors.toList());
        viewDTO.setOverseasCfgLogisticsList(overseasCfgLogisticsList);
        List<CfgRuleOverseasInstockDaysDTO.ViewDTO> overseasInstockDaysList = cfgRuleOverseasInstockDaysService.listViewByExpireTimeIdList(Collections.singletonList(oldEntity.getId()));
        viewDTO.setOverseasInstockDaysList(overseasInstockDaysList);
        return viewDTO;
    }

    @Override
    public CfgRuleExpireTimeEntity getByRefId(String refId) {
        return getOne(Wrappers.<CfgRuleExpireTimeEntity>lambdaQuery().eq(CfgRuleExpireTimeEntity::getRefId, refId).last("LIMIT 1"));
    }

    @Override
    public void deleteByRefId(String refId) {
        CfgRuleExpireTimeEntity cfgRuleExpireTime = getByRefId(refId);
        if (ObjectUtil.isEmpty(cfgRuleExpireTime)) {
            return;
        }
        ApplicationContextUtils.getBean(CfgRuleExpireTimeServiceImpl.class).removeById(cfgRuleExpireTime.getId());
        //删除物流信息配置
        cfgRuleLogisticsService.deleteByExpireTimeId(cfgRuleExpireTime.getId());
    }

    @Override
    public List<CfgRuleExpireTimeEntity> listByRefIdList(List<String> refIdList) {
        return list(Wrappers.<CfgRuleExpireTimeEntity>lambdaQuery().in(CfgRuleExpireTimeEntity::getRefId, refIdList));
    }


    private CfgRuleExpireTimeEntity getRefByPlatformType(String refId) {
        CfgRuleExpireTimeEntity refEntity = getDefaultByPlatformType(refId);
        if (ObjectUtil.isNotEmpty(refEntity)) {
            return refEntity;
        }
        return getDefaultByPlatformType("");
    }

    private CfgRuleExpireTimeEntity getDefaultByPlatformType(String refId) {
        return getOne(Wrappers.<CfgRuleExpireTimeEntity>lambdaQuery().eq(CfgRuleExpireTimeEntity::getRefId, CharSequenceUtil.isNotBlank(refId) ? refId : "")
                .last("limit 1")
        );
    }
}
