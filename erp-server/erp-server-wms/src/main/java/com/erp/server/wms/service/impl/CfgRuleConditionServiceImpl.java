package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.pickingstrategy.CfgConditionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.server.wms.mapper.CfgRuleConditionMapper;
import com.erp.server.wms.service.CfgConditionService;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Resource
    private SpElServer spElServer;

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgConditionService cfgConditionService;

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
        saveBatch(ruleConditionEntities);
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
        checkRuleCondition(conditionList);
        //查询规则条件
        List<CfgRuleConditionEntity> oleConditions = list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, ruleId));
        List<CfgConditionDTO.CommonDTO> cfgConditions = cfgConditionService.listByType(sourceType);
        Map<String, String> cfgConditionMap = cfgConditions.stream()
                .collect(Collectors.toMap(CfgConditionDTO.CommonDTO::getConditionField, CfgConditionDTO.CommonDTO::getConditionFieldName));
        List<String> actionIds = conditionList.stream().map(CfgRuleConditionDTO.Update::getId).collect(Collectors.toList());
        // 处理删除的数据
        if (!CollectionUtils.isEmpty(actionIds)) {
            List<String> removeIds = oleConditions.stream()
                    .map(CfgRuleConditionEntity::getId)
                    .filter(id -> !actionIds.contains(id))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(removeIds)) {
                List<Pair<String, String>> removePairList = oleConditions.stream()
                        .filter(old -> removeIds.contains(old.getId()))
                        .map(old -> Pair.create(ruleId, cfgConditionMap.getOrDefault(old.getField(), "")))
                        .collect(Collectors.toList());
                removeByIds(removeIds);
                operateLogService.batchAddModuleOperateLog("删除了一个条件字段【%s】", moduleType, removePairList, "编辑操作");
            }
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

        //添加的条件
        List<CfgRuleConditionDTO.Update> addList = conditionList.stream().filter(r -> StringUtils.isEmpty(r.getId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream()
                    .map(obj -> new Pair<>(ruleId, cfgConditionMap.getOrDefault(obj.getField(), "")))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加一个条件字段【%s】", moduleType, addPairList, "添加操作");
        }
        //修改的
        List<CfgRuleConditionEntity> updateList = ruleConditionEntities.stream().filter(r -> StringUtils.hasText(r.getId())).collect(Collectors.toList());
        for (CfgRuleConditionEntity updateItem : updateList) {
            CfgRuleConditionEntity old = oleConditions.stream().filter(r -> r.getId().equals(updateItem.getId())).findFirst().orElse(null);
            if (Objects.nonNull(old)) {
                old.setFieldName(cfgConditionMap.get(old.getField()));
                updateItem.setFieldName(cfgConditionMap.get(updateItem.getField()));
                //值没有的时候名称置空
                if (CharSequenceUtil.isBlank(updateItem.getValue())) {
                    updateItem.setName("");
                }
                operateLogService.addModuleOperateLogByObj(old, updateItem, moduleType, ruleId, CharSequenceUtil.format("修改了第【{}】条订单规则", updateItem.getIndex()));
            }
        }
        handleDataList(ruleConditionEntities);
        ApplicationContextUtils.getBean(CfgRuleConditionService.class).saveOrUpdateBatch(ruleConditionEntities);
    }

    @Override
    public List<CfgRuleConditionDTO.ConditionElementDTO> listByRuleIds(List<String> cfgRuleIds,String ruleType) {
        return baseMapper.listByRuleIds(cfgRuleIds,ruleType);
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
