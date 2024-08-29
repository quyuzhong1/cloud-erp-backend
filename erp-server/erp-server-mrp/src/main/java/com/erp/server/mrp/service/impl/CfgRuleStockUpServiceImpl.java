package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
        CfgRuleStockUpEntity old = super.getById(updateDTO.getId());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleStockUpEntity.setId(old.getId());
        }
        // 数据处理
        handleData(cfgRuleStockUpEntity);

        boolean save = super.saveOrUpdate(cfgRuleStockUpEntity);
        if(!save) {
            throw new ServiceException("备货（规则设置）保存失败");
        }
        //物流信息
        cfgRuleLogisticsService.update(updateDTO.getCfgLogisticsList(),cfgRuleStockUpEntity.getId());

        //常规备货系数
        cfgRuleStockingRatioService.update(updateDTO.getStockingRatioList(),cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
        //新品备货系数
        cfgRuleStockingRatioService.update(updateDTO.getNewStockingRatioList(),cfgRuleStockUpEntity.getId(),CfgRuleStockingRatioTypeEnum.NEW.getCode());

        // 记录主单操作日志
        log.info("编辑 开始记录备货（规则设置）日志数据，id：【{}】", cfgRuleStockUpEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleStockUpEntity.getId(), "备货（规则设置）");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleStockUpEntity, ModuleTypeEnum.CFG_RULE_COMMON.getCode(), cfgRuleStockUpEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleStockUpDTO.ViewDTO  view(String platformType) {
        CfgRuleStockUpDTO.ViewDTO viewDTO = new CfgRuleStockUpDTO.ViewDTO();
        CfgRuleStockUpEntity oldEntity = this.getByPlatformType(platformType);
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
        List<CfgRuleStockingRatioEntity> newList = stockingRatioList.stream().filter(obj -> StrUtil.equals(obj.getType(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(newList)) {
            List<CfgRuleStockingRatioDTO.ViewDTO> oldStockingRatioList = BeanMapperUtils.copyList(CfgRuleStockingRatioDTO.ViewDTO.class, newList);
            viewDTO.setNewStockingRatioList(oldStockingRatioList);
        }
        return viewDTO;
    }

    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 9:30
     * @param platformType
     * @return CfgRuleStockUpEntity
     */
    private CfgRuleStockUpEntity getByPlatformType (String platformType) {
        return lambdaQuery().eq(CfgRuleStockUpEntity::getPlatformType,platformType).last("limit 1").one();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleStockUpEntity cfgRuleStockUpEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
