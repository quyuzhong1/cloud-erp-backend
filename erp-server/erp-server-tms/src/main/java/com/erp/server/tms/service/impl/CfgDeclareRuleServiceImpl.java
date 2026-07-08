package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.entity.ConditionElement;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.tms.dto.CfgConditionDTO;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
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
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 报关规则主表服务实现
 */
@Slf4j
@Service
public class CfgDeclareRuleServiceImpl extends SuperServiceImpl<CfgDeclareRuleMapper, CfgDeclareRuleEntity> implements CfgDeclareRuleService {

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
    @Resource
    private CfgDeclareRuleServiceImpl service;

    /**
     * 批量保存报关规则。
     *
     * <p>业务规则：</p>
     * <ul>
     * <li>按 ruleType 全量覆盖，未出现在本次提交列表中的旧规则会被删除</li>
     * <li>同一 ruleType 下 senderId + receiverId 不可重复</li>
     * <li>配置为单值唯一的条件字段不可跨规则重复</li>
     * </ul>
     *
     * @param dto 规则类型及规则主从数据列表
     */
    @Override
    public Boolean add(CfgDeclareRuleDTO.SaveListDTO dto) {
        List<CfgDeclareRuleDTO.SaveDTO> saveList = Optional.ofNullable(dto.getList()).orElse(Collections.emptyList());
        List<CfgDeclareRuleEntity> existingRules = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, dto.getRuleType())
                .list();
        Map<String, CfgDeclareRuleEntity> existingRuleMap = existingRules.stream()
                .collect(Collectors.toMap(CfgDeclareRuleEntity::getId, item -> item, (left, right) -> left));

        validateBatchSaveRequest(dto.getRuleType(), saveList, existingRuleMap);
        List<CfgConditionDTO.CommonDTO> conditionConfigList = cfgConditionService.listByType(dto.getRuleType());
        Set<String> uniqueConditionFields = listUniqueConditionFields(conditionConfigList);
        Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap = buildConditionConfigMap(conditionConfigList);
        validateSaveListUniqueConditions(dto.getRuleType(), saveList, uniqueConditionFields, conditionConfigMap);

        List<CfgDeclareRuleEntity> saveEntities = new ArrayList<>(saveList.size());
        for (int i = 0; i < saveList.size(); i++) {
            CfgDeclareRuleEntity entity = BeanMapperUtils.map(CfgDeclareRuleEntity.class, saveList.get(i));
            entity.setRuleType(dto.getRuleType());
            entity.setIndex(i);
            saveEntities.add(entity);
        }
        fillCompanyNames(saveEntities);

        List<String> deleteRuleIds = getDeleteRuleIds(existingRules, saveList);
        service.addInGlobalTx(saveList, saveEntities, existingRuleMap, deleteRuleIds);
        return Boolean.TRUE;
    }

    /**
     * 在全局事务内完成报关规则本地写库。
     *
     * <p>调用方已完成 Feign 读取、规则校验、公司名称补齐和实体构建，本方法仅处理本地规则主从表删除、保存及操作日志。</p>
     *
     * @param saveList 本次提交的规则 DTO 列表
     * @param saveEntities 已构建并补齐名称的规则实体列表
     * @param existingRuleMap 已存在规则映射，用于更新和删除日志
     * @param deleteRuleIds 本次需删除的旧规则 ID 列表
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public void addInGlobalTx(List<CfgDeclareRuleDTO.SaveDTO> saveList,
                              List<CfgDeclareRuleEntity> saveEntities,
                              Map<String, CfgDeclareRuleEntity> existingRuleMap,
                              List<String> deleteRuleIds) {
        removeRules(deleteRuleIds, existingRuleMap);

        for (int i = 0; i < saveList.size(); i++) {
            persistRule(saveEntities.get(i), saveList.get(i), existingRuleMap);
        }
    }

    /**
     * 按规则类型查询报关规则，并组装为主从结构的保存 DTO。
     *
     * @param dto 查询参数，含 ruleType
     */
    @Override
    public CfgDeclareRuleDTO.SaveListDTO paging(CfgDeclareRuleDTO.ListParamDTO dto) {
        return buildSaveListResult(dto.getRuleType(), this.baseMapper.paging(dto));
    }

    /**
     * 获取报关规则发件人/收件人下拉数据。
     *
     * <p>头程报关单返回核算公司发件人；B2B 报关单返回核算公司收件人和客户收件人两类节点。</p>
     *
     * @param type 单据来源类型：fmDeclareBill / b2bDeclareBill
     * @param isShowCustomerId B2B 场景是否展示客户子节点
     * @param name 核算公司或客户名称模糊筛选
     */
    @Override
    public List<BaseDropDownDTO.Tree> dropDownList(String type,Boolean isShowCustomerId, String name) {
        if (SourceTypeEnum.FM_DECLARE_BILL.getCode().equals(type)) {
            return Collections.singletonList(buildDropDown(
                    type,
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
        }

        if (SourceTypeEnum.B2B_DECLARE_BILL.getCode().equals(type)) {
            if(Objects.isNull(isShowCustomerId) ) isShowCustomerId = false;
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
                    customerChildList(isShowCustomerId,name)));
            return result;
        }
        throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_TYPE_INVALID);
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

    @Override
    public String resolveConsistentReceiverType(String declareBillType,
                                                List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList,
                                                BiFunction<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>, Map<String, String>> paramMapBuilder,
                                                ApiError ruleNotFoundError,
                                                ApiError receiverTypeConflictError) {
        if (CollUtil.isEmpty(sourceDetailList)) {
            return "";
        }
        Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> sourceGroupMap = groupSourceDetailsBySourceId(sourceDetailList);
        Set<String> receiverTypeSet = new LinkedHashSet<>();
        for (List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails : sourceGroupMap.values()) {
            CfgDeclareRuleEntity matchedRule = listMatchedRule(paramMapBuilder.apply(declareBillType, sourceDetails));
            if (matchedRule == null) {
                throw new ServiceException(ruleNotFoundError, resolveSourceCode(sourceDetails));
            }
            if (StrUtil.isNotBlank(matchedRule.getReceiverType())) {
                receiverTypeSet.add(matchedRule.getReceiverType());
            }
        }
        if (receiverTypeSet.size() > 1) {
            throw new ServiceException(receiverTypeConflictError);
        }
        return receiverTypeSet.stream().findFirst().orElse("");
    }

    private String resolveSourceCode(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails) {
        if (CollUtil.isEmpty(sourceDetails)) {
            return "";
        }
        return sourceDetails.stream()
                .filter(Objects::nonNull)
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceCode)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse("");
    }

    private Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> groupSourceDetailsBySourceId(
            List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList) {
        Map<String, List<TmsDeclareBillDTO.SourceDeliveryDetailDTO>> sourceGroupMap = new LinkedHashMap<>();
        int anonymousIndex = 0;
        for (TmsDeclareBillDTO.SourceDeliveryDetailDTO sourceDetail : sourceDetailList) {
            if (sourceDetail == null) {
                continue;
            }
            String groupKey = StrUtil.blankToDefault(sourceDetail.getSourceId(), "__anonymous_" + (anonymousIndex++));
            sourceGroupMap.computeIfAbsent(groupKey, key -> new ArrayList<>()).add(sourceDetail);
        }
        return sourceGroupMap;
    }

    /**
     * 校验批量保存请求的基础合法性。
     *
     * <p>校验 ruleType 一致性、明细非空、更新单据存在性，以及 senderId + receiverId 在本次提交内不重复。</p>
     */
    void validateBatchSaveRequest(String ruleType,
                                  List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                  Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgDeclareRuleDTO.SaveDTO item : saveList) {
            if (!StrUtil.equals(ruleType, item.getRuleType())) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_TYPE_MISMATCH);
            }
            if (CollUtil.isEmpty(item.getDetailList())) {
                throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "detailList");
            }
            if (StrUtil.isNotBlank(item.getId())) {
                CfgDeclareRuleEntity existingRule = existingRuleMap.get(item.getId());
                if (existingRule == null) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "CfgDeclareRule");
                }
                if (!StrUtil.equals(ruleType, existingRule.getRuleType())) {
                    throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_CROSS_TYPE_UPDATE_FORBIDDEN);
                }
            }

            String uniqueKey = buildUniqueKey(item.getRuleType(), item.getSenderId(), item.getReceiverId());
            if (!uniqueKeySet.add(uniqueKey)) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_SENDER_RECEIVER_DUPLICATE);
            }
        }
    }

    /**
     * 计算本次批量保存后需要删除的旧规则 ID。
     *
     * <p>不在本次提交 id 列表中的已有规则视为被前端移除，需要级联删除条件明细。</p>
     */
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
     * 校验批量保存后的报关规则条件组合唯一。
     *
     * <p>add 接口按本次提交列表覆盖同 ruleType 下旧规则，只校验提交后的最终规则列表，
     * 避免待删除旧规则造成重复误判。重复判定维度：条件选项 + 比较符 + 条件值。</p>
     *
     * @param ruleType 规则类型
     * @param saveList 本次提交的规则列表
     * @param uniqueConditionFields 需要校验单值唯一的条件字段
     */
    private void validateSaveListUniqueConditions(String ruleType,
                                                  List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                                  Set<String> uniqueConditionFields,
                                                  Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap) {
        Map<String, Map<String, String>> conditionValueNameMap =
                buildConditionValueNameMap(collectDtoConditionValueMap(saveList, uniqueConditionFields));
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgDeclareRuleDTO.SaveDTO saveDTO : saveList) {
            addDtoConditionUniqueKeys(ruleType, saveDTO.getDetailList(), uniqueConditionFields, uniqueKeySet,
                    conditionConfigMap, conditionValueNameMap);
        }
    }


    /**
     * 将 DTO 条件明细中的单值唯一字段写入唯一键集合。
     *
     * <p>用于批量保存场景，发现重复时直接抛出业务异常。</p>
     */
    private void addDtoConditionUniqueKeys(String ruleType,
                                           List<? extends CfgDeclareRuleConditionDTO.CommonDTO> detailList,
                                           Set<String> uniqueConditionFields,
                                           Set<String> uniqueKeySet,
                                           Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                           Map<String, Map<String, String>> conditionValueNameMap) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionDTO.CommonDTO detail : detailList) {
            addConditionUniqueKeys(ruleType, detail.getField(), detail.getCompare(), detail.getValue(),
                    uniqueConditionFields, uniqueKeySet, conditionConfigMap, conditionValueNameMap);
        }
    }

    /**
     * 将实体条件明细中的单值唯一字段写入唯一键集合。
     *
     * <p>用于修改场景，先收集当前规则新条件，再与其它规则比对。</p>
     */
    private void addEntityConditionUniqueKeys(String ruleType,
                                              List<CfgDeclareRuleConditionEntity> detailList,
                                              Set<String> uniqueConditionFields,
                                              Set<String> uniqueKeySet,
                                              Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                              Map<String, Map<String, String>> conditionValueNameMap) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionEntity detail : detailList) {
            addConditionUniqueKeys(ruleType, detail.getField(), detail.getCompare(), detail.getValue(),
                    uniqueConditionFields, uniqueKeySet, conditionConfigMap, conditionValueNameMap);
        }
    }

    /**
     * 校验其它规则条件明细是否与当前唯一键集合冲突。
     */
    private void checkEntityConditionUniqueKeys(String ruleType,
                                                List<CfgDeclareRuleConditionEntity> detailList,
                                                Set<String> uniqueConditionFields,
                                                Set<String> uniqueKeySet,
                                                Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                                Map<String, Map<String, String>> conditionValueNameMap) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionEntity detail : detailList) {
            checkConditionUniqueKeys(ruleType, detail.getField(), detail.getCompare(), detail.getValue(),
                    uniqueConditionFields, uniqueKeySet, conditionConfigMap, conditionValueNameMap);
        }
    }

    /**
     * 读取规则类型下需要校验单值唯一的条件字段集合。
     *
     * <p>以报关规则条件配置为唯一来源，避免字段范围在代码中写死。</p>
     */
    private Set<String> listUniqueConditionFields(List<CfgConditionDTO.CommonDTO> conditionList) {
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

    /**
     * 将条件配置列表转换为 conditionField -> 配置 的映射，便于后续解析展示名称。
     */
    private Map<String, CfgConditionDTO.CommonDTO> buildConditionConfigMap(List<CfgConditionDTO.CommonDTO> conditionList) {
        if (CollUtil.isEmpty(conditionList)) {
            return Collections.emptyMap();
        }
        return conditionList.stream()
                .filter(item -> StrUtil.isNotBlank(item.getConditionField()))
                .collect(Collectors.toMap(item -> item.getConditionField().trim(),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private void addConditionUniqueKeys(String ruleType, String field, String compare, String value,
                                        Set<String> uniqueConditionFields, Set<String> uniqueKeySet,
                                        Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                        Map<String, Map<String, String>> conditionValueNameMap) {
        handleConditionUniqueKeys(ruleType, field, compare, value, uniqueConditionFields, uniqueKeySet, true,
                conditionConfigMap, conditionValueNameMap);
    }

    private void checkConditionUniqueKeys(String ruleType, String field, String compare, String value,
                                          Set<String> uniqueConditionFields, Set<String> uniqueKeySet,
                                          Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                          Map<String, Map<String, String>> conditionValueNameMap) {
        handleConditionUniqueKeys(ruleType, field, compare, value, uniqueConditionFields, uniqueKeySet, false,
                conditionConfigMap, conditionValueNameMap);
    }

    private void handleConditionUniqueKeys(String ruleType, String field, String compare, String value,
                                           Set<String> uniqueConditionFields,
                                           Set<String> uniqueKeySet, boolean addKey,
                                           Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap,
                                           Map<String, Map<String, String>> conditionValueNameMap) {
        String conditionField = StringUtils.trimToEmpty(field);
        if (!uniqueConditionFields.contains(conditionField)) {
            return;
        }
        String conditionCompare = StringUtils.trimToEmpty(compare);
        // value 支持英文逗号聚合多个取值，需要展开后按单值判断是否与其它规则交叉。
        List<String> valueList = splitMatchValues(value);
        for (String itemValue : valueList) {
            String uniqueKey = buildConditionUniqueKey(ruleType, conditionField, conditionCompare, itemValue);
            boolean duplicate = addKey ? !uniqueKeySet.add(uniqueKey) : uniqueKeySet.contains(uniqueKey);
            if (duplicate) {
                throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_CONDITION_DUPLICATE,
                        resolveConditionFieldDisplayName(conditionField, conditionConfigMap),
                        resolveConditionValueDisplayName(conditionField, itemValue, conditionValueNameMap));
            }
        }
    }

    private String resolveConditionFieldDisplayName(String conditionField,
                                                    Map<String, CfgConditionDTO.CommonDTO> conditionConfigMap) {
        CfgConditionDTO.CommonDTO config = conditionConfigMap.get(conditionField);
        if (config != null && StrUtil.isNotBlank(config.getConditionFieldName())) {
            return config.getConditionFieldName();
        }
        return conditionField;
    }

    private String resolveConditionValueDisplayName(String conditionField,
                                                    String itemValue,
                                                    Map<String, Map<String, String>> conditionValueNameMap) {
        if (StrUtil.isBlank(itemValue)) {
            return itemValue;
        }
        Map<String, String> valueNameMap = conditionValueNameMap.get(conditionField);
        if (CollUtil.isEmpty(valueNameMap)) {
            return itemValue;
        }
        return StrUtil.blankToDefault(valueNameMap.get(itemValue), itemValue);
    }

    /**
     * 从本次提交的保存 DTO 中收集唯一性校验字段值，用于在重复校验报错前一次性预加载显示名称。
     */
    private Map<String, Set<String>> collectDtoConditionValueMap(List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                                                 Set<String> uniqueConditionFields) {
        Map<String, Set<String>> conditionValueMap = new HashMap<>();
        if (CollUtil.isEmpty(saveList)) {
            return conditionValueMap;
        }
        for (CfgDeclareRuleDTO.SaveDTO saveDTO : saveList) {
            collectCommonConditionValues(saveDTO.getDetailList(), uniqueConditionFields, conditionValueMap);
        }
        return conditionValueMap;
    }

    /**
     * 从已持久化的条件实体中收集唯一性校验字段值，用于更新场景的冲突校验。
     */
    private Map<String, Set<String>> collectEntityConditionValueMap(List<CfgDeclareRuleConditionEntity> detailList,
                                                                    Set<String> uniqueConditionFields) {
        Map<String, Set<String>> conditionValueMap = new HashMap<>();
        if (CollUtil.isEmpty(detailList)) {
            return conditionValueMap;
        }
        for (CfgDeclareRuleConditionEntity detail : detailList) {
            collectConditionValue(detail.getField(), detail.getValue(), uniqueConditionFields, conditionValueMap);
        }
        return conditionValueMap;
    }

    /**
     * 将 DTO 明细中的条件值追加到共享的「字段 -> 值集合」映射中。
     */
    private void collectCommonConditionValues(List<? extends CfgDeclareRuleConditionDTO.CommonDTO> detailList,
                                              Set<String> uniqueConditionFields,
                                              Map<String, Set<String>> conditionValueMap) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        for (CfgDeclareRuleConditionDTO.CommonDTO detail : detailList) {
            collectConditionValue(detail.getField(), detail.getValue(), uniqueConditionFields, conditionValueMap);
        }
    }

    /**
     * 展开英文逗号分隔的条件值，并仅记录参与唯一性校验的字段。
     */
    private void collectConditionValue(String field,
                                       String value,
                                       Set<String> uniqueConditionFields,
                                       Map<String, Set<String>> conditionValueMap) {
        String conditionField = StringUtils.trimToEmpty(field);
        if (!uniqueConditionFields.contains(conditionField)) {
            return;
        }
        List<String> valueList = splitMatchValues(value);
        if (CollUtil.isEmpty(valueList)) {
            return;
        }
        conditionValueMap.computeIfAbsent(conditionField, key -> new HashSet<>()).addAll(valueList);
    }

    /**
     * 合并已收集的条件值，并保持字段维度下的值去重。
     */
    private void mergeConditionValueMap(Map<String, Set<String>> target, Map<String, Set<String>> source) {
        if (CollUtil.isEmpty(source)) {
            return;
        }
        source.forEach((field, valueSet) -> {
            if (CollUtil.isNotEmpty(valueSet)) {
                target.computeIfAbsent(field, key -> new HashSet<>()).addAll(valueSet);
            }
        });
    }

    /**
     * 构建重复校验错误提示所需的「字段 -> 值 -> 显示名称」映射。
     *
     * <p>远程查询统一集中在这里执行，避免在逐条件值重复校验循环内调用 Feign。</p>
     */
    private Map<String, Map<String, String>> buildConditionValueNameMap(Map<String, Set<String>> conditionValueMap) {
        if (CollUtil.isEmpty(conditionValueMap)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> conditionValueNameMap = new HashMap<>();
        loadCompanyNameMap(conditionValueMap, conditionValueNameMap);
        loadCountryNameMap(conditionValueMap, conditionValueNameMap);
        loadWarehouseNameMap(conditionValueMap, conditionValueNameMap);
        return conditionValueNameMap;
    }

    /**
     * 为销售组织条件值加载核算公司名称。
     */
    private void loadCompanyNameMap(Map<String, Set<String>> conditionValueMap,
                                    Map<String, Map<String, String>> conditionValueNameMap) {
        Set<String> companyIds = conditionValueMap.get("salesOrgId");
        if (CollUtil.isEmpty(companyIds)) {
            return;
        }
        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList("");
        if (companyResult == null || !companyResult.isSuccess() || CollUtil.isEmpty(companyResult.getData())) {
            return;
        }
        Map<String, String> companyNameMap = companyResult.getData().stream()
                .filter(Objects::nonNull)
                .filter(company -> companyIds.contains(company.getId()))
                .filter(company -> StrUtil.isNotBlank(company.getCompanyName()))
                .collect(Collectors.toMap(SysAccountingCompanyDTO.ListDTO::getId,
                        SysAccountingCompanyDTO.ListDTO::getCompanyName,
                        (left, right) -> left,
                        HashMap::new));
        if (CollUtil.isNotEmpty(companyNameMap)) {
            conditionValueNameMap.put("salesOrgId", companyNameMap);
        }
    }

    /**
     * 为国家编码条件值加载国家中文名称。
     */
    private void loadCountryNameMap(Map<String, Set<String>> conditionValueMap,
                                    Map<String, Map<String, String>> conditionValueNameMap) {
        Set<String> countryIds = conditionValueMap.get("countryCode");
        if (CollUtil.isEmpty(countryIds)) {
            return;
        }
        ApiResult<List<DictCountryDTO.ListDTO>> countryResult = sysFeign.countryList();
        if (countryResult == null || !countryResult.isSuccess() || CollUtil.isEmpty(countryResult.getData())) {
            return;
        }
        Map<String, String> countryNameMap = countryResult.getData().stream()
                .filter(Objects::nonNull)
                .filter(country -> countryIds.contains(country.getId()))
                .filter(country -> StrUtil.isNotBlank(country.getNameCn()))
                .collect(Collectors.toMap(DictCountryDTO.ListDTO::getId,
                        DictCountryDTO.ListDTO::getNameCn,
                        (left, right) -> left,
                        HashMap::new));
        if (CollUtil.isNotEmpty(countryNameMap)) {
            conditionValueNameMap.put("countryCode", countryNameMap);
        }
    }

    /**
     * 为所有以 {@code WarehouseId} 结尾的唯一性校验字段加载仓库名称。
     */
    private void loadWarehouseNameMap(Map<String, Set<String>> conditionValueMap,
                                      Map<String, Map<String, String>> conditionValueNameMap) {
        Set<String> warehouseIds = conditionValueMap.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(entry.getKey(), "WarehouseId"))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(warehouseIds)) {
            return;
        }
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, new ArrayList<>(warehouseIds));
        if (CollUtil.isEmpty(warehouseList)) {
            return;
        }
        Map<String, String> warehouseNameMap = warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(warehouse -> StrUtil.isNotBlank(warehouse.getId()))
                .filter(warehouse -> StrUtil.isNotBlank(warehouse.getName()))
                .collect(Collectors.toMap(WarehouseEntity::getId,
                        WarehouseEntity::getName,
                        (left, right) -> left,
                        HashMap::new));
        if (CollUtil.isEmpty(warehouseNameMap)) {
            return;
        }
        conditionValueMap.keySet().stream()
                .filter(field -> StrUtil.endWithIgnoreCase(field, "WarehouseId"))
                .forEach(field -> conditionValueNameMap.put(field, warehouseNameMap));
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

    private String buildConditionUniqueKey(String ruleType, String field, String compare, String value) {
        return StrUtil.join("|", ruleType, field, compare, value);
    }

    private void addCreateLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("用户 [{}] 创建报关规则 [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "create");
    }

    private void addUpdateLog(CfgDeclareRuleEntity oldEntity, CfgDeclareRuleEntity newEntity) {
        String msg = StrUtil.format("用户 [{}] 更新报关规则 [{}]",
                UserContext.getDefaultLoginUser().getUserName(), newEntity.getId());
        operateLogService.addModuleOperateLogByObj(oldEntity, newEntity,
                ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), newEntity.getId(), msg);
    }

    private void addDeleteLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("用户 [{}] 删除报关规则 [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "delete");
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
        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyListAll(name);
        if (companyResult == null || !companyResult.isSuccess()) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_RULE_ACCOUNTING_COMPANY_LOAD_FAILED);
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

    private List<BaseDropDownDTO.ChildTree> customerChildList(Boolean isShowCustomerId , String name) {
        if(!isShowCustomerId){
            return  Collections.emptyList();
        }
        FeignBuilder feignBuilder = FeignQuery.create(CustomerInfoEntity.class)
                .like(StrUtil.isNotBlank(name), CustomerInfoEntity::getName, name);
        List<CustomerInfoEntity> list = feignBuilder.list();
        return list.stream()
                .sorted(Comparator.comparing(CustomerInfoEntity::getDisabled))
                .map(e -> BaseDropDownDTO.ChildTree.builder()
                        .code(e.getId())
                        .value(e.getName())
                        .disabled(e.getDisabled())
                        .build())
                .collect(Collectors.toList());
    }

}
