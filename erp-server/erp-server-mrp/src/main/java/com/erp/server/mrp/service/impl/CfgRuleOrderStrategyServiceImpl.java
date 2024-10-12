package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleOrderStrategyDTO;
import com.erp.model.mrp.entity.CfgRuleOrderStrategyEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleOrderStrategyMapper;
import com.erp.server.mrp.service.CfgRuleOrderStrategyService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * <p>
 * 策略（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-10-12
 */
@Slf4j
@Service
public class CfgRuleOrderStrategyServiceImpl extends SuperServiceImpl<CfgRuleOrderStrategyMapper, CfgRuleOrderStrategyEntity> implements CfgRuleOrderStrategyService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleOrderStrategyDTO.UpdateDTO updateDTO) {
        CfgRuleOrderStrategyEntity orderStrategyEntity =  BeanMapperUtils.map(CfgRuleOrderStrategyEntity.class, updateDTO);
        //旧数据
        CfgRuleOrderStrategyEntity old = this.getByPlatformType(updateDTO.getPlatformType());
        if (ObjectUtil.isNotEmpty(old)) {
            orderStrategyEntity.setId(old.getId());
        }
        // 数据处理
        handleData(orderStrategyEntity);
        boolean save = super.saveOrUpdate(orderStrategyEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录策略（规则设置）日志数据，id：【{}】", orderStrategyEntity.getId());
        operateLogService.addModuleOperateLogByObj(old, orderStrategyEntity, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), orderStrategyEntity.getId(), "");
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleOrderStrategyDTO.ViewDTO view(String platformType) {
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = new CfgRuleOrderStrategyDTO.ViewDTO();
        CfgRuleOrderStrategyEntity oldEntity = this.getByPlatformType(platformType);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);
        return viewDTO;
    }

    @Override
    public CfgRuleOrderStrategyEntity getByPlatformType (String platformType) {
        return lambdaQuery().eq(CfgRuleOrderStrategyEntity::getPlatformType,platformType)
                .last("limit 1")
                .one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleOrderStrategyEntity cfgRuleOrderStrategyEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
