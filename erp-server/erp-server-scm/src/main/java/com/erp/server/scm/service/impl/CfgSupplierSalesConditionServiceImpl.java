package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.server.rule.SpElServer;
import com.erp.model.scm.dto.CfgConditionDTO;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.erp.server.scm.mapper.CfgSupplierSalesConditionMapper;
import com.erp.server.scm.service.CfgConditionService;
import com.erp.server.scm.service.CfgSupplierSalesConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.server.scm.service.ModuleOperateLogService;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
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
 * 销量设置条件明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@Service
public class CfgSupplierSalesConditionServiceImpl extends SuperServiceImpl<CfgSupplierSalesConditionMapper, CfgSupplierSalesConditionEntity> implements CfgSupplierSalesConditionService {

    @Resource
    private ModuleOperateLogService operateLogService;

    @Resource
    private SpElServer spElServer;
    @Resource
    private CfgConditionService cfgConditionService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveRuleCondition(String salesSettingId, List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList, String sourceType) {
        checkRuleCondition(conditionList);
        AtomicInteger index = new AtomicInteger(0);
        List<CfgSupplierSalesConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgSupplierSalesConditionEntity entity = BeanMapperUtils.map(CfgSupplierSalesConditionEntity.class, action);
                    entity.setSalesSettingId(salesSettingId);
                    entity.setSourceType(sourceType);
                    entity.setIndex(index.incrementAndGet());
                    return entity;
                }).collect(Collectors.toList());
        handleDataList(ruleConditionEntities);
        saveBatch(ruleConditionEntities);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRuleCondition(String salesSettingId, List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList, String moduleType, String sourceType) {
        if(CollUtil.isNotEmpty(conditionList)){
            checkRuleCondition(conditionList);
        }
        //查询规则条件
        List<CfgSupplierSalesConditionEntity> oleConditions = lambdaQuery()
                .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, salesSettingId)
                .eq(CfgSupplierSalesConditionEntity::getSourceType, sourceType)
                .list();
        List<CfgConditionDTO.CommonDTO> cfgConditions = cfgConditionService.listByType(sourceType);
        Map<String, String> cfgConditionMap = cfgConditions.stream()
                .collect(Collectors.toMap(CfgConditionDTO.CommonDTO::getConditionField, CfgConditionDTO.CommonDTO::getConditionFieldName));
        List<String> actionIds = conditionList.stream().map(CfgSupplierSalesConditionDTO.ConditionDTO::getId).collect(Collectors.toList());
        // 处理删除的数据
        List<String> removeIds = oleConditions.stream()
                .map(CfgSupplierSalesConditionEntity::getId)
                .filter(id -> !actionIds.contains(id))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(removeIds)) {
            List<Pair<String, String>> removePairList = oleConditions.stream()
                    .filter(old -> removeIds.contains(old.getId()))
                    .map(old -> Pair.create(salesSettingId, cfgConditionMap.getOrDefault(old.getField(), "")))
                    .collect(Collectors.toList());
            removeByIds(removeIds);
            operateLogService.batchAddModuleOperateLog("删除了一个条件字段【%s】", moduleType, removePairList, "编辑操作");
        }
        AtomicInteger index = new AtomicInteger(0);
        List<CfgSupplierSalesConditionEntity> ruleConditionEntities = conditionList.stream()
                .map(action -> {
                    CfgSupplierSalesConditionEntity entity = BeanMapperUtils.map(CfgSupplierSalesConditionEntity.class, action);
                    entity.setIndex(index.incrementAndGet());
                    entity.setSourceType(sourceType);
                    entity.setSalesSettingId(salesSettingId);
                    return entity;
                }).collect(Collectors.toList());

        //添加的条件
        List<CfgSupplierSalesConditionDTO.ConditionDTO> addList = conditionList.stream().filter(r -> StringUtils.isEmpty(r.getId())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream()
                    .map(obj -> new Pair<>(salesSettingId, cfgConditionMap.getOrDefault(obj.getField(), "")))
                    .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加一个条件字段【%s】", moduleType, addPairList, "添加操作");
        }
        //修改的
        List<CfgSupplierSalesConditionEntity> updateList = ruleConditionEntities.stream().filter(r -> StringUtils.hasText(r.getId())).collect(Collectors.toList());
        for (CfgSupplierSalesConditionEntity updateItem : updateList) {
            CfgSupplierSalesConditionEntity old = oleConditions.stream().filter(r -> r.getId().equals(updateItem.getId())).findFirst().orElse(null);
            if (Objects.nonNull(old)) {
                old.setFieldName(cfgConditionMap.get(old.getField()));
                updateItem.setFieldName(cfgConditionMap.get(updateItem.getField()));
                //值没有的时候名称置空
                if (CharSequenceUtil.isBlank(updateItem.getValue())) {
                    updateItem.setName("");
                }
                operateLogService.addModuleOperateLogByObj(old, updateItem, moduleType, salesSettingId,"", CharSequenceUtil.format("修改了第【{}】条订单规则", updateItem.getIndex()));
            }
        }
        handleDataList(ruleConditionEntities);
        ApplicationContextUtils.getBean(CfgSupplierSalesConditionService.class).saveOrUpdateBatch(ruleConditionEntities);
    }

    /**
     * 校验规则条件是否合法
     *
     * @param conditionList 规则条件集合
     */
    private void checkRuleCondition(List<? extends CfgSupplierSalesConditionDTO.ConditionDTO> conditionList) {
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

    /**
     * 新增修改处理数据
     */
    private void handleDataList(List<CfgSupplierSalesConditionEntity> ruleConditionList) {
        for (CfgSupplierSalesConditionEntity item : ruleConditionList) {
            String compare = item.getCompare();
            if (RuleCompareEnum.IS_NULL.getCode().equals(compare) || RuleCompareEnum.NOT_NULL.getCode().equals(compare) || ObjectUtil.isEmpty(item.getValue())) {
                item.setValue("");
            }
        }
    }

}
