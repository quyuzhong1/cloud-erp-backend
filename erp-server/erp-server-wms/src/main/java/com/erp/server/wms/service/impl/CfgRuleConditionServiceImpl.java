package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.server.wms.mapper.CfgRuleConditionMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-05-28
 */
@Service
public class CfgRuleConditionServiceImpl extends SuperServiceImpl<CfgRuleConditionMapper, CfgRuleConditionEntity> implements CfgRuleConditionService {

    @Reference
    private SpElServer spElServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ids) {
        remove(Wrappers.<CfgRuleConditionEntity>lambdaQuery().in(CfgRuleConditionEntity::getRuleId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRuleCondition(String ruleId, List<CfgRuleConditionDTO.Add> conditionList) {
        checkRuleCondition(conditionList);
        AtomicInteger index = new AtomicInteger(0);
        List<CfgRuleConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgRuleConditionEntity entity = BeanMapperUtils.map(CfgRuleConditionEntity.class, action);
                    entity.setRuleId(ruleId);
                    entity.setIndex(index.incrementAndGet());
                    return entity;
                }).collect(Collectors.toList());
        saveBatch(ruleConditionEntities);
    }

    /**
     * 校验规则条件是否合法
     * @param conditionList 规则条件集合
     */
    private void checkRuleCondition(List<? extends CfgRuleConditionDTO.Common> conditionList) {
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(),"")).collect(Collectors.toList());
        SpElExpressionDTO splElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = splElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRuleCondition(String ruleId, List<CfgRuleConditionDTO.Update> conditionList) {
        checkRuleCondition(conditionList);
        //查询规则条件
        List<CfgRuleConditionEntity> oleConditions = list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, ruleId));
        List<String> actionIds = conditionList.stream().map(CfgRuleConditionDTO.Update::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(actionIds)) {
            List<String> removeIds = oleConditions.stream()
                    .map(CfgRuleConditionEntity::getId)
                    .filter(id -> !actionIds.contains(id))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(removeIds)) {
                removeByIds(removeIds);
            }
        }
        AtomicInteger index = new AtomicInteger(0);
        List<CfgRuleConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgRuleConditionEntity entity = BeanMapperUtils.map(CfgRuleConditionEntity.class, action);
                    entity.setIndex(index.incrementAndGet());
                    entity.setRuleId(ruleId);
                    return entity;
                }).collect(Collectors.toList());
        saveOrUpdateBatch(ruleConditionEntities);
    }
}
