package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleOrderStrategyDTO;
import com.erp.model.mrp.entity.CfgRuleOrderStrategyEntity;
import com.erp.server.mrp.mapper.CfgRuleOrderStrategyMapper;
import com.erp.server.mrp.service.CfgRuleOrderStrategyService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
        CfgRuleOrderStrategyEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "策略（规则设置）"));
        CfgRuleOrderStrategyEntity cfgRuleOrderStrategyEntity =  BeanMapperUtils.map(CfgRuleOrderStrategyEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleOrderStrategyEntity);
        log.info("编辑 开始修改策略（规则设置）数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleOrderStrategyEntity);
        if(!save) {
            throw new ServiceException("策略（规则设置）保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录策略（规则设置）日志数据，id：【{}】", cfgRuleOrderStrategyEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleOrderStrategyEntity.getId(), "策略（规则设置）");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleOrderStrategyEntity, null, cfgRuleOrderStrategyEntity.getId(), msg);
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
        return null;
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
