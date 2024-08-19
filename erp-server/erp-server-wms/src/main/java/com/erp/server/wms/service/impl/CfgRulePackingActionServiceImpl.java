package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleActionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.entity.CfgRulePackingActionEntity;
import com.erp.model.wms.entity.CfgRulePickingEntity;
import com.erp.server.wms.mapper.CfgRulePackingActionMapper;
import com.erp.server.wms.service.CfgRulePackingActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 仓位分配规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
public class CfgRulePackingActionServiceImpl extends SuperServiceImpl<CfgRulePackingActionMapper, CfgRulePackingActionEntity> implements CfgRulePackingActionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ids) {
        remove(Wrappers.<CfgRulePackingActionEntity>lambdaQuery().in(CfgRulePackingActionEntity::getRuleId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRuleAction(String ruleId, List<CfgRuleActionDTO.Add> actions) {
        AtomicInteger index = new AtomicInteger(0);
        List<CfgRulePackingActionEntity> actionEntities = actions.stream()
                .map(action -> {
                    CfgRulePackingActionEntity entity = BeanMapperUtils.map(CfgRulePackingActionEntity.class, action);
                    entity.setRuleId(ruleId);
                    entity.setIndex(index.incrementAndGet());
                    return entity;
                }).collect(Collectors.toList());
        saveBatch(actionEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRuleAction(String ruleId, List<CfgRuleActionDTO.Update> actionList) {

        AtomicInteger index = new AtomicInteger(0);
        // 查询规则动作
        List<CfgRulePackingActionEntity> oldActions = list(Wrappers.<CfgRulePackingActionEntity>lambdaQuery()
                .eq(CfgRulePackingActionEntity::getRuleId, ruleId));
        List<String> actionIds = actionList.stream().map(CfgRuleActionDTO.Update::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(actionIds)) {
            List<String> removeIds = oldActions.stream()
                    .map(CfgRulePackingActionEntity::getId)
                    .filter(id -> !actionIds.contains(id))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(removeIds)) {
                removeByIds(removeIds);
            }
        }
        List<CfgRulePackingActionEntity> actions = actionList.stream()
                .map(action -> {
                    CfgRulePackingActionEntity entity = BeanMapperUtils.map(CfgRulePackingActionEntity.class, action);
                    entity.setRuleId(ruleId);
                    entity.setIndex(index.incrementAndGet());
                    return entity;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(actions);
    }

    @Override
    public List<CfgRulePackingActionEntity> listByRuleIds(List<String> cfgRuleIds) {
        return list(Wrappers.<CfgRulePackingActionEntity>lambdaQuery().in(CfgRulePackingActionEntity::getRuleId, cfgRuleIds));
    }

    @Override
    public List<CfgRulePickingDTO.CfgRulePickingInventoryDTO> listLocationByRule(List<CfgRulePickingEntity> rules, List<String> warehouseIds, List<String> skuIds) {
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyList();
        }
        List<String> ruleIds = rules.parallelStream().map(CfgRulePickingEntity::getId).collect(Collectors.toList());
        return baseMapper.listLocationByRule(ruleIds, warehouseIds, skuIds);
    }
}
