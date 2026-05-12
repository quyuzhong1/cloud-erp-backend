package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.erp.model.tms.enums.CfgDeclareRuleReceiverTypeEnum;
import com.erp.model.tms.enums.CfgDeclareRuleSenderTypeEnum;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.server.tms.mapper.CfgDeclareRuleMapper;
import com.erp.server.tms.service.CfgConditionService;
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CfgDeclareRuleServiceImpl extends SuperServiceImpl<CfgDeclareRuleMapper, CfgDeclareRuleEntity> implements CfgDeclareRuleService {

    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String RULE_TYPE = "ruleType";
    private static final String CONDITION_VALUE_TYPE_STRING = "String";

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgDeclareRuleConditionService cfgDeclareRuleConditionService;
    @Resource
    private CfgConditionService cfgConditionService;
    @Resource
    private CommonService commonService;
    @Resource
    private SysFeign sysFeign;
    @Resource
    private SpElServer spElServer;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(CfgDeclareRuleDTO.SaveListDTO dto) {
        List<CfgDeclareRuleDTO.SaveDTO> saveList = Optional.ofNullable(dto.getList()).orElse(Collections.emptyList());
        List<CfgDeclareRuleEntity> existingRules = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, dto.getRuleType())
                .list();
        Map<String, CfgDeclareRuleEntity> existingRuleMap = existingRules.stream()
                .collect(Collectors.toMap(CfgDeclareRuleEntity::getId, item -> item, (left, right) -> left));

        validateBatchSaveRequest(dto.getRuleType(), saveList, existingRuleMap);
        Set<String> uniqueConditionFields = listUniqueConditionFields(dto.getRuleType());
        validateSaveListUniqueConditions(dto.getRuleType(), saveList, uniqueConditionFields);

        List<CfgDeclareRuleEntity> saveEntities = new ArrayList<>(saveList.size());
        for (int i = 0; i < saveList.size(); i++) {
            CfgDeclareRuleEntity entity = BeanMapperUtils.map(CfgDeclareRuleEntity.class, saveList.get(i));
            entity.setRuleType(dto.getRuleType());
            entity.setIndex(i);
            saveEntities.add(entity);
        }
        fillCompanyNames(saveEntities);

        List<String> deleteRuleIds = getDeleteRuleIds(existingRules, saveList);
        removeRules(deleteRuleIds, existingRuleMap);

        for (int i = 0; i < saveList.size(); i++) {
            persistRule(saveEntities.get(i), saveList.get(i), existingRuleMap);
        }
        return Boolean.TRUE;
    }

    @Override
    public CfgDeclareRuleDTO.SaveListDTO paging(CfgDeclareRuleDTO.ListParamDTO dto) {
        return buildSaveListResult(dto.getRuleType(), this.baseMapper.paging(dto));
    }

    @Override
    public List<BaseDropDownDTO.Tree> dropDownList(String type, String name) {
        if (SENDER.equals(type)) {
            return Collections.singletonList(buildDropDown(
                    type,
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
        }

        if (RECEIVER.equals(type)) {
            List<BaseDropDownDTO.Tree> result = new ArrayList<>(2);
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getName(),
                    Collections.emptyList()));
            return result;
        }

        throw new ServiceException("type must be sender or receiver");
    }

    /**
     * 按规则类型和入参条件匹配一条启用状态的报关规则。
     *
     * <p>匹配逻辑由规则条件配置驱动：先根据 ruleType 读取该类型下需要参与匹配的字段，
     * 再从 paramMap 中取出对应字段值构造匹配数据，最后逐条规则执行条件表达式匹配。
     * 如果多条规则同时命中，返回主表 index 最小的规则，用于表达前端列表顺序对应的优先级。</p>
     *
     * @param paramMap 条件参数集合，必须包含 ruleType；其它 key 与规则条件配置的 conditionField 对齐
     * @return index 最小的命中规则；参数不完整、无条件配置或无命中规则时返回 null
     */
    @Override
    public CfgDeclareRuleEntity listMatchedRule(Map<String, String> paramMap) {
        if (paramMap == null || paramMap.isEmpty() || StrUtil.isBlank(paramMap.get(RULE_TYPE))) {
            return null;
        }
        String ruleType = paramMap.get(RULE_TYPE);

        // 规则条件配置决定本次需要从 paramMap 中读取哪些业务字段参与匹配。
        List<CfgConditionDTO.CommonDTO> conditionList = cfgConditionService.listByType(ruleType);
        if (CollUtil.isEmpty(conditionList)) {
            return null;
        }

        // 多值字段会被展开成多组匹配数据，保证同一条规则能覆盖所有入参取值。
        List<Map<String, Object>> matchDataList = buildMatchDataList(paramMap, conditionList);
        Map<String, String> valueTypeMap = conditionList.stream()
                .filter(item -> StrUtil.isNotBlank(item.getConditionField()))
                .collect(Collectors.toMap(CfgConditionDTO.CommonDTO::getConditionField,
                        item -> StrUtil.blankToDefault(item.getValueType(), CONDITION_VALUE_TYPE_STRING),
                        (left, right) -> left,
                        HashMap::new));

        // 只取启用规则参与匹配；先按 index 升序读取，保持规则优先级与前端列表顺序一致。
        List<CfgDeclareRuleEntity> ruleList = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, ruleType)
                .eq(CfgDeclareRuleEntity::getDisabled, Boolean.FALSE)
                .orderByAsc(CfgDeclareRuleEntity::getIndex)
                .orderByDesc(CfgDeclareRuleEntity::getId)
                .list();
        if (CollUtil.isEmpty(ruleList)) {
            return null;
        }

        List<String> ruleIdList = ruleList.stream()
                .map(CfgDeclareRuleEntity::getId)
                .collect(Collectors.toList());
        // 一次性加载候选规则的条件明细，避免逐条规则查询数据库。
        Map<String, List<CfgDeclareRuleConditionEntity>> ruleConditionMap = cfgDeclareRuleConditionService.lambdaQuery()
                .in(CfgDeclareRuleConditionEntity::getRuleId, ruleIdList)
                .orderByAsc(CfgDeclareRuleConditionEntity::getIndex)
                .list()
                .stream()
                .collect(Collectors.groupingBy(CfgDeclareRuleConditionEntity::getRuleId,
                        LinkedHashMap::new, Collectors.toList()));

        // 多条规则命中时，只返回 index 最小的一条；未命中则返回 null。
        return ruleList.stream()
                .filter(rule -> matchDeclareRule(rule, ruleConditionMap, matchDataList, valueTypeMap))
                .min(Comparator.comparing(CfgDeclareRuleEntity::getIndex,
                        Comparator.nullsLast(Integer::compareTo)))
                .orElse(null);
    }

    void validateBatchSaveRequest(String ruleType,
                                  List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                  Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgDeclareRuleDTO.SaveDTO item : saveList) {
            if (!StrUtil.equals(ruleType, item.getRuleType())) {
                throw new ServiceException("request ruleType does not match item ruleType");
            }
            if (CollUtil.isEmpty(item.getDetailList())) {
                throw new ServiceException("detailList can not be empty");
            }
            if (StrUtil.isNotBlank(item.getId())) {
                CfgDeclareRuleEntity existingRule = existingRuleMap.get(item.getId());
                if (existingRule == null) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "CfgDeclareRule");
                }
                if (!StrUtil.equals(ruleType, existingRule.getRuleType())) {
                    throw new ServiceException("cross-ruleType update is not allowed");
                }
            }

            String uniqueKey = buildUniqueKey(item.getRuleType(), item.getSenderId(), item.getReceiverId());
            if (!uniqueKeySet.add(uniqueKey)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE);
            }
        }
    }

    List<String> getDeleteRuleIds(Collection<CfgDeclareRuleEntity> existingRules, List<CfgDeclareRuleDTO.SaveDTO> saveList) {
        Set<String> keepIdSet = saveList.stream()
                .map(CfgDeclareRuleDTO.SaveDTO::getId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        return existingRules.stream()
                .map(CfgDeclareRuleEntity::getId)
                .filter(id -> !keepIdSet.contains(id))
                .collect(Collectors.toList());
    }

    /**
     * 校验批量保存后的报关规则条件单值唯一。
     *
     * <p>add 接口按本次提交列表覆盖同 ruleType 下旧规则，只校验提交后的最终规则列表，
     * 避免待删除旧规则造成重复误判。</p>
     *
     * @param ruleType 规则类型
     * @param saveList 本次提交的规则列表
     * @param uniqueConditionFields 需要校验单值唯一的条件字段
     */
    private void validateSaveListUniqueConditions(String ruleType,
                                                  List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                                  Set<String> uniqueConditionFields) {
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgDeclareRuleDTO.SaveDTO saveDTO : saveList) {
            addDtoConditionUniqueKeys(ruleType, saveDTO.getDetailList(), uniqueConditionFields, uniqueKeySet);
        }
    }

    /**
     * 校验修改后的报关规则条件单值唯一。
     *
     * <p>update 只校验当前规则新条件与其它规则是否冲突，不拦截其它历史规则之间已存在的重复数据。</p>
     *
     * @param entity 当前规则主表数据
     * @param newDetails 当前规则新的条件明细
     * @param uniqueConditionFields 需要校验单值唯一的条件字段
     */
    private void validateUpdateUniqueConditions(CfgDeclareRuleEntity entity,
                                                List<CfgDeclareRuleConditionEntity> newDetails,
                                                Set<String> uniqueConditionFields) {
        if (CollUtil.isEmpty(uniqueConditionFields)) {
            return;
        }
        Set<String> uniqueKeySet = new HashSet<>();
        addEntityConditionUniqueKeys(entity.getRuleType(), newDetails, uniqueConditionFields, uniqueKeySet);

        List<String> otherRuleIds = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, entity.getRuleType())
                .ne(CfgDeclareRuleEntity::getId, entity.getId())
                .list()
                .stream()
                .map(CfgDeclareRuleEntity::getId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(otherRuleIds)) {
            return;
        }

        List<CfgDeclareRuleConditionEntity> otherDetails = cfgDeclareRuleConditionService.lambdaQuery()
                .in(CfgDeclareRuleConditionEntity::getRuleId, otherRuleIds)
                .in(CfgDeclareRuleConditionEntity::getField, uniqueConditionFields)
                .list();
        checkEntityConditionUniqueKeys(entity.getRuleType(), otherDetails, uniqueConditionFields, uniqueKeySet);
    }

    private void addDtoConditionUniqueKeys(String ruleType,
                                           List<? extends CfgDeclareRuleConditionDTO.CommonDTO> detailList,
                                           Set<String> uniqueConditionFields,
                                           Set<String> uniqueKeySet) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionDTO.CommonDTO detail : detailList) {
            addConditionUniqueKeys(ruleType, detail.getField(), detail.getValue(), uniqueConditionFields, uniqueKeySet);
        }
    }

    private void addEntityConditionUniqueKeys(String ruleType,
                                              List<CfgDeclareRuleConditionEntity> detailList,
                                              Set<String> uniqueConditionFields,
                                              Set<String> uniqueKeySet) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionEntity detail : detailList) {
            addConditionUniqueKeys(ruleType, detail.getField(), detail.getValue(), uniqueConditionFields, uniqueKeySet);
        }
    }

    private void checkEntityConditionUniqueKeys(String ruleType,
                                                List<CfgDeclareRuleConditionEntity> detailList,
                                                Set<String> uniqueConditionFields,
                                                Set<String> uniqueKeySet) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionEntity detail : detailList) {
            checkConditionUniqueKeys(ruleType, detail.getField(), detail.getValue(), uniqueConditionFields, uniqueKeySet);
        }
    }

    private Set<String> listUniqueConditionFields(String ruleType) {
        List<CfgConditionDTO.CommonDTO> conditionList = cfgConditionService.listByType(ruleType);
        if (CollUtil.isEmpty(conditionList)) {
            return Collections.emptySet();
        }
        // 以报关规则条件配置为唯一校验来源，避免字段范围在代码中写死后与配置不一致。
        return conditionList.stream()
                .map(CfgConditionDTO.CommonDTO::getConditionField)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private void addConditionUniqueKeys(String ruleType, String field, String value,
                                        Set<String> uniqueConditionFields, Set<String> uniqueKeySet) {
        handleConditionUniqueKeys(ruleType, field, value, uniqueConditionFields, uniqueKeySet, true);
    }

    private void checkConditionUniqueKeys(String ruleType, String field, String value,
                                          Set<String> uniqueConditionFields, Set<String> uniqueKeySet) {
        handleConditionUniqueKeys(ruleType, field, value, uniqueConditionFields, uniqueKeySet, false);
    }

    private void handleConditionUniqueKeys(String ruleType, String field, String value,
                                           Set<String> uniqueConditionFields,
                                           Set<String> uniqueKeySet, boolean addKey) {
        String conditionField = StringUtils.trimToEmpty(field);
        if (!uniqueConditionFields.contains(conditionField)) {
            return;
        }
        // value 支持英文逗号聚合多个取值，需要展开后按单值判断是否与其它规则交叉。
        List<String> valueList = splitMatchValues(value);
        for (String itemValue : valueList) {
            String uniqueKey = buildConditionUniqueKey(ruleType, conditionField, itemValue);
            boolean duplicate = addKey ? !uniqueKeySet.add(uniqueKey) : uniqueKeySet.contains(uniqueKey);
            if (duplicate) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_CONDITION_DUPLICATE, conditionField, itemValue);
            }
        }
    }

    /**
     * 构建报关规则匹配数据列表。
     *
     * <p>批量报关时同一字段可能按英文逗号聚合多个取值，需要展开为多组单值匹配数据，
     * 后续按全部展开结果校验同一条规则是否覆盖本次请求。</p>
     *
     * @param paramMap 条件参数集合
     * @param conditionList 规则条件配置
     * @return 规则匹配数据列表
     */
    List<Map<String, Object>> buildMatchDataList(Map<String, String> paramMap, List<CfgConditionDTO.CommonDTO> conditionList) {
        List<Map<String, Object>> matchDataList = new ArrayList<>();
        matchDataList.add(new HashMap<>());
        for (CfgConditionDTO.CommonDTO condition : conditionList) {
            String conditionField = condition.getConditionField();
            if (StrUtil.isBlank(conditionField)) {
                continue;
            }
            List<String> valueList = splitMatchValues(paramMap.get(conditionField));
            matchDataList = appendMatchDataList(matchDataList, conditionField, valueList);
        }
        for (Map<String, Object> matchData : matchDataList) {
            matchData.put("detailList", Collections.singletonList(new HashMap<>(matchData)));
        }
        return matchDataList;
    }

    List<String> splitMatchValues(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        String[] splitValues = value.split(",");
        List<String> valueList = new ArrayList<>(splitValues.length);
        Set<String> uniqueValueSet = new HashSet<>();
        for (String splitValue : splitValues) {
            String trimValue = splitValue.trim();
            if (StringUtils.isNotBlank(trimValue) && uniqueValueSet.add(trimValue)) {
                valueList.add(trimValue);
            }
        }
        return valueList;
    }

    private List<Map<String, Object>> appendMatchDataList(List<Map<String, Object>> sourceList,
                                                          String conditionField,
                                                          List<String> valueList) {
        if (CollUtil.isEmpty(valueList)) {
            return sourceList;
        }
        List<Map<String, Object>> resultList = new ArrayList<>(sourceList.size() * valueList.size());
        for (Map<String, Object> source : sourceList) {
            for (String value : valueList) {
                Map<String, Object> matchData = new HashMap<>(source);
                matchData.put(conditionField, value);
                resultList.add(matchData);
            }
        }
        return resultList;
    }

    boolean matchDeclareRule(CfgDeclareRuleEntity rule,
                             Map<String, List<CfgDeclareRuleConditionEntity>> ruleConditionMap,
                             List<Map<String, Object>> matchDataList,
                             Map<String, String> valueTypeMap) {
        List<CfgDeclareRuleConditionEntity> conditionList = ruleConditionMap.get(rule.getId());
        if (CollUtil.isEmpty(conditionList)) {
            return false;
        }
        List<ConditionElement> conditionElementList = conditionList.stream()
                .sorted(Comparator.comparing(CfgDeclareRuleConditionEntity::getIndex,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(condition -> buildConditionElement(condition, valueTypeMap))
                .collect(Collectors.toList());
        // 多值入参必须全部被同一条规则覆盖，避免仅匹配部分仓库或组织就返回规则。
        return matchDataList.stream()
                .allMatch(matchData -> Boolean.TRUE.equals(spElServer.matchExpressionByConditionList(
                        conditionElementList, new HashMap<>(matchData), "")));
    }

    ConditionElement buildConditionElement(CfgDeclareRuleConditionEntity condition, Map<String, String> valueTypeMap) {
        ConditionElement element = new ConditionElement();
        element.setLeftBracket(condition.getLeftBracket());
        element.setField(condition.getField());
        element.setCompare(condition.getCompare());
        element.setValue(condition.getValue());
        element.setRightBracket(condition.getRightBracket());
        element.setLogic(condition.getLogic());
        element.setValueType(valueTypeMap.getOrDefault(condition.getField(), CONDITION_VALUE_TYPE_STRING));
        return element;
    }

    List<CfgDeclareRuleConditionEntity> buildConditionEntities(String ruleId,
                                                               List<? extends CfgDeclareRuleConditionDTO.CommonDTO> detailList,
                                                               boolean keepId) {
        if (CollUtil.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        List<CfgDeclareRuleConditionEntity> entities = new ArrayList<>(detailList.size());
        for (int i = 0; i < detailList.size(); i++) {
            CfgDeclareRuleConditionEntity entity = BeanMapperUtils.map(CfgDeclareRuleConditionEntity.class, detailList.get(i));
            entity.setRuleId(ruleId);
            entity.setIndex(i);
            if (!keepId) {
                entity.setId(null);
            }
            entities.add(entity);
        }
        return entities;
    }

    void applyCompanyNames(List<CfgDeclareRuleEntity> ruleList, Map<String, String> companyMap) {
        if (CollUtil.isEmpty(ruleList) || CollUtil.isEmpty(companyMap)) {
            return;
        }
        for (CfgDeclareRuleEntity entity : ruleList) {
            if (StrUtil.equals(entity.getSenderType(), CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode())
                    && companyMap.containsKey(entity.getSenderId())) {
                entity.setSenderName(companyMap.get(entity.getSenderId()));
            }
            if (StrUtil.equals(entity.getReceiverType(), CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode())
                    && companyMap.containsKey(entity.getReceiverId())) {
                entity.setReceiverName(companyMap.get(entity.getReceiverId()));
            }
        }
    }

    CfgDeclareRuleDTO.SaveListDTO buildSaveListResult(String ruleType, List<CfgDeclareRuleDTO.ListDTO> rowList) {
        CfgDeclareRuleDTO.SaveListDTO result = new CfgDeclareRuleDTO.SaveListDTO();
        result.setRuleType(ruleType);
        if (CollUtil.isEmpty(rowList)) {
            result.setList(Collections.emptyList());
            return result;
        }

        Map<String, CfgDeclareRuleDTO.SaveDTO> ruleMap = new LinkedHashMap<>();
        for (CfgDeclareRuleDTO.ListDTO row : rowList) {
            CfgDeclareRuleDTO.SaveDTO ruleDTO = ruleMap.computeIfAbsent(row.getId(), key -> {
                CfgDeclareRuleDTO.SaveDTO item = BeanMapperUtils.map(CfgDeclareRuleDTO.SaveDTO.class, row);
                item.setId(row.getId());
                item.setRuleType(StrUtil.isNotBlank(row.getRuleType()) ? row.getRuleType() : ruleType);
                item.setIndex(row.getRuleIndex());
                item.setDetailList(new ArrayList<>());
                return item;
            });

            if (StrUtil.isNotBlank(row.getDetailId())) {
                CfgDeclareRuleConditionDTO.SaveDTO detailDTO = BeanMapperUtils.map(CfgDeclareRuleConditionDTO.SaveDTO.class, row);
                detailDTO.setId(row.getDetailId());
                detailDTO.setRuleId(row.getRuleId());
                ruleDTO.getDetailList().add(detailDTO);
            }
        }
        List<CfgDeclareRuleDTO.SaveDTO> saveList = new ArrayList<>(ruleMap.values());
        saveList.sort(Comparator.comparing(CfgDeclareRuleDTO.SaveDTO::getIndex,
                Comparator.nullsLast(Integer::compareTo)));
        result.setList(saveList);
        return result;
    }

    private void persistRule(CfgDeclareRuleEntity entity,
                             CfgDeclareRuleDTO.SaveDTO saveDTO,
                             Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        CfgDeclareRuleEntity old = StrUtil.isNotBlank(entity.getId()) ? existingRuleMap.get(entity.getId()) : null;
        if (old != null) {
            boolean updated = super.updateById(entity);
            if (!updated) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_SAVE_FAILED);
            }
            refreshRuleConditions(entity.getId(), saveDTO.getDetailList());
            addUpdateLog(old, entity);
            return;
        }

        boolean saved = super.save(entity);
        if (!saved) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_SAVE_FAILED);
        }
        refreshRuleConditions(entity.getId(), saveDTO.getDetailList());
        addCreateLog(entity);
    }

    private void refreshRuleConditions(String ruleId, List<CfgDeclareRuleConditionDTO.SaveDTO> detailList) {
        cfgDeclareRuleConditionService.lambdaUpdate()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, ruleId)
                .remove();
        List<CfgDeclareRuleConditionEntity> conditions = buildConditionEntities(ruleId, detailList, false);
        if (CollUtil.isNotEmpty(conditions)) {
            cfgDeclareRuleConditionService.saveBatch(conditions);
        }
    }

    private void saveRuleConditions(String ruleId, List<CfgDeclareRuleConditionDTO.AddDTO> detailList) {
        List<CfgDeclareRuleConditionEntity> conditions = buildConditionEntities(ruleId, detailList, false);
        if (CollUtil.isNotEmpty(conditions)) {
            cfgDeclareRuleConditionService.saveBatch(conditions);
        }
    }

    private void removeRules(List<String> deleteRuleIds, Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        if (CollUtil.isEmpty(deleteRuleIds)) {
            return;
        }
        cfgDeclareRuleConditionService.lambdaUpdate()
                .in(CfgDeclareRuleConditionEntity::getRuleId, deleteRuleIds)
                .remove();
        super.removeByIds(deleteRuleIds);
        deleteRuleIds.stream()
                .map(existingRuleMap::get)
                .filter(ObjectUtil::isNotNull)
                .forEach(this::addDeleteLog);
    }

    private void validateUniqueWithDb(CfgDeclareRuleEntity entity) {
        if (!ObjectUtil.isAllNotEmpty(entity.getRuleType(), entity.getSenderId(), entity.getReceiverId())) {
            return;
        }
        CfgDeclareRuleEntity exist = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, entity.getRuleType())
                .eq(CfgDeclareRuleEntity::getSenderId, entity.getSenderId())
                .eq(CfgDeclareRuleEntity::getReceiverId, entity.getReceiverId())
                .ne(StrUtil.isNotBlank(entity.getId()), CfgDeclareRuleEntity::getId, entity.getId())
                .last("LIMIT 1")
                .one();
        if (exist != null) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE);
        }
    }

    private void fillCompanyNames(List<CfgDeclareRuleEntity> ruleList) {
        if (CollUtil.isEmpty(ruleList)) {
            return;
        }
        Set<String> companyIds = new HashSet<>();
        for (CfgDeclareRuleEntity entity : ruleList) {
            if (StrUtil.isNotBlank(entity.getSenderId())
                    && StrUtil.equals(entity.getSenderType(), CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode())) {
                companyIds.add(entity.getSenderId());
            }
            if (StrUtil.isNotBlank(entity.getReceiverId())
                    && StrUtil.equals(entity.getReceiverType(), CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode())) {
                companyIds.add(entity.getReceiverId());
            }
        }
        if (CollUtil.isEmpty(companyIds)) {
            return;
        }

        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList("");
        if (companyResult == null || !companyResult.isSuccess() || CollUtil.isEmpty(companyResult.getData())) {
            return;
        }

        Map<String, String> companyMap = companyResult.getData().stream()
                .filter(company -> companyIds.contains(company.getId()))
                .collect(Collectors.toMap(SysAccountingCompanyDTO.ListDTO::getId,
                        SysAccountingCompanyDTO.ListDTO::getCompanyName,
                        (left, right) -> left,
                        HashMap::new));
        applyCompanyNames(ruleList, companyMap);
    }

    private String buildUniqueKey(String ruleType, String senderId, String receiverId) {
        return StrUtil.join("|", ruleType, senderId, receiverId);
    }

    private String buildConditionUniqueKey(String ruleType, String field, String value) {
        return StrUtil.join("|", ruleType, field, value);
    }

    private void addCreateLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("User [{}] created cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "create");
    }

    private void addUpdateLog(CfgDeclareRuleEntity oldEntity, CfgDeclareRuleEntity newEntity) {
        String msg = StrUtil.format("User [{}] updated cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), newEntity.getId());
        operateLogService.addModuleOperateLogByObj(oldEntity, newEntity,
                ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), newEntity.getId(), msg);
    }

    private void addDeleteLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("User [{}] deleted cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "delete");
    }

    private void fillList(List<CfgDeclareRuleDTO.ListDTO> list) {
        for (CfgDeclareRuleDTO.ListDTO data : list) {
            if (StrUtil.isNotEmpty(data.getRuleType())) {
                if ("fmDeclareBill".equals(data.getRuleType())) {
                    data.setRuleTypeName("\u5934\u7a0b\u62a5\u5173\u5355");
                } else if ("b2bDeclareBill".equals(data.getRuleType())) {
                    data.setRuleTypeName("B2B\u62a5\u5173\u5355");
                }
            }
            data.setSenderTypeName(CfgDeclareRuleSenderTypeEnum.getName(data.getSenderType()));
            data.setReceiverTypeName(CfgDeclareRuleReceiverTypeEnum.getName(data.getReceiverType()));
        }
    }

    private BaseDropDownDTO.Tree buildDropDown(String type,
                                               String code,
                                               String value,
                                               List<BaseDropDownDTO.ChildTree> childTreeList) {
        BaseDropDownDTO.Tree tree = new BaseDropDownDTO.Tree();
        tree.setType(type);
        tree.setCode(code);
        tree.setValue(value);
        tree.setDisabled(Boolean.FALSE);
        tree.setChildTreeList(childTreeList);
        return tree;
    }

    private List<BaseDropDownDTO.ChildTree> accountingCompanyChildList(String name) {
        com.common.core.controller.vo.ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList(name);
        if (companyResult == null || !companyResult.isSuccess()) {
            throw new ServiceException("load accounting company list failed");
        }
        return Optional.ofNullable(companyResult.getData())
                .orElse(Collections.emptyList())
                .stream()
                .map(company -> BaseDropDownDTO.ChildTree.builder()
                        .code(company.getId())
                        .value(company.getCompanyName())
                        .disabled(company.getDisabled())
                        .build())
                .collect(Collectors.toList());
    }
}
