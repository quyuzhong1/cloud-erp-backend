package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        List<CfgRuleOrderStrategyEntity> list = this.list();
        if (CollectionUtils.isNotEmpty(list)) {
            orderStrategyEntity.setId(list.get(0).getId());
        }
        // 数据处理
        handleData(orderStrategyEntity);
        boolean save = super.saveOrUpdate(orderStrategyEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录策略（规则设置）日志数据，id：【{}】", orderStrategyEntity.getId());
        String msg = StrUtil.format("设置采购建议策略:【{}】",updateDTO.getIsSplit() ? "组合品拆分为单品,集中采购" : "单品/组合品 分开采购" );
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), orderStrategyEntity.getId(), "策略");
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleOrderStrategyDTO.ViewDTO view() {
        CfgRuleOrderStrategyDTO.ViewDTO viewDTO = new CfgRuleOrderStrategyDTO.ViewDTO();
        List<CfgRuleOrderStrategyEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(list.get(0),viewDTO);
        return viewDTO;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleOrderStrategyEntity cfgRuleOrderStrategyEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
