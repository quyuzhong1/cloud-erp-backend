package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.server.rule.SpElServer;
import com.erp.model.sys.dto.CfgConditionDTO;
import com.erp.model.sys.dto.CfgRuleConditionDTO;
import com.erp.model.sys.entity.CfgRuleConditionEntity;
import com.erp.server.sys.mapper.CfgRuleConditionMapper;
import com.erp.server.sys.service.CfgConditionService;
import com.erp.server.sys.service.CfgRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * <p>
 * 规则条件表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@Service
public class CfgRuleConditionServiceImpl extends SuperServiceImpl<CfgRuleConditionMapper, CfgRuleConditionEntity> implements CfgRuleConditionService {

    @Resource
    private SpElServer spElServer;

    @Resource
    private OperateLogService operateLogService;
    @Lazy
    @Resource
    private CfgRuleConditionService service;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRuleIds(List<String> ids) {
        remove(Wrappers.<CfgRuleConditionEntity>lambdaQuery().in(CfgRuleConditionEntity::getRuleId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRuleCondition(String ruleId, List<CfgRuleConditionDTO.Add> conditionList, String sourceType) {
        checkRuleCondition(conditionList);
        AtomicInteger index = new AtomicInteger(0);
        List<CfgRuleConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgRuleConditionEntity entity = BeanMapperUtils.map(CfgRuleConditionEntity.class, action);
                    entity.setRuleId(ruleId);
                    entity.setSourceType(sourceType);
                    entity.setIndex(index.incrementAndGet());
                    return entity;
                }).collect(Collectors.toList());
        handleDataList(ruleConditionEntities);
        service.saveBatch(ruleConditionEntities);
    }

    /**
     * 校验规则条件是否合法
     *
     * @param conditionList 规则条件集合
     */
    private void checkRuleCondition(List<? extends CfgRuleConditionDTO.Common> conditionList) {
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO splElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = splElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRuleCondition(String ruleId, List<CfgRuleConditionDTO.Update> conditionList, String moduleType, String sourceType) {
        if(CollUtil.isNotEmpty(conditionList)){
            checkRuleCondition(conditionList);
        }
        //查询规则条件
        List<CfgRuleConditionEntity> oleConditions = list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, ruleId));
        List<String> actionIds = conditionList.stream().map(CfgRuleConditionDTO.Update::getId).collect(Collectors.toList());
        // 处理删除的数据
        List<String> removeIds = oleConditions.stream()
                .map(CfgRuleConditionEntity::getId)
                .filter(id -> !actionIds.contains(id))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(removeIds)) {
            removeByIds(removeIds);
        }

        AtomicInteger index = new AtomicInteger(0);
        List<CfgRuleConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgRuleConditionEntity entity = BeanMapperUtils.map(CfgRuleConditionEntity.class, action);
                    entity.setIndex(index.incrementAndGet());
                    entity.setSourceType(sourceType);
                    entity.setRuleId(ruleId);
                    return entity;
                }).collect(Collectors.toList());
        handleDataList(ruleConditionEntities);
        ApplicationContextUtils.getBean(CfgRuleConditionService.class).saveOrUpdateBatch(ruleConditionEntities);
    }

    @Override
    public List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleIds(List<String> cfgRuleIds, String ruleType) {
        return baseMapper.listByRuleIds(cfgRuleIds,ruleType);
    }

    @Override
    public List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleType(String ruleType) {
        return baseMapper.listByRuleType(ruleType);
    }


    /**
     * 新增修改处理数据
     */
    private void handleDataList(List<CfgRuleConditionEntity> ruleConditionList) {
        for (CfgRuleConditionEntity item : ruleConditionList) {
            String compare = item.getCompare();
            if (RuleCompareEnum.IS_NULL.getCode().equals(compare) || RuleCompareEnum.NOT_NULL.getCode().equals(compare) || ObjectUtil.isEmpty(item.getValue())) {
                item.setValue("");
            }
        }
    }
}
