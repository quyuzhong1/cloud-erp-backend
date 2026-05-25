package com.erp.model.tms.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlOrderDirectionEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlReplaceModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlRuleTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSymbolPositionEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 物流费用导入字段清洗规则：扁平 DTO 与存储 JSON 的转换、校验。
 *
 * @author jack
 * @date 2026/05/25
 */
public final class CfgLogisticsCostImportEtlRuleHelper {

    public static final String EMPTY_ETL_RULE_LIST = "{\"data\":[]}";

    private CfgLogisticsCostImportEtlRuleHelper() {
    }

    public static Map<String, Object> resolveParams(CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO) {
        if (Objects.isNull(ruleDTO) || StringUtils.isBlank(ruleDTO.getType())) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = new LinkedHashMap<>();
        String type = ruleDTO.getType();
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), type)) {
            putIfNotBlank(params, "sourceText", ruleDTO.getSourceText());
            putIfNotBlank(params, "mode", ruleDTO.getMode());
            putIfNotBlank(params, "targetText", ruleDTO.getTargetText());
            return params;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), type)) {
            putIfNotBlank(params, "mode", ruleDTO.getMode());
            putIfNotBlank(params, "symbol", ruleDTO.getSymbol());
            putIfNotBlank(params, "symbolPosition", ruleDTO.getSymbolPosition());
            putIfNotBlank(params, "orderDirection", ruleDTO.getOrderDirection());
            if (Objects.nonNull(ruleDTO.getLength())) {
                params.put("length", ruleDTO.getLength());
            }
            return params;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(), type)) {
            putIfNotBlank(params, "mode", ruleDTO.getMode());
            putIfNotBlank(params, "fillValue", ruleDTO.getFillValue());
            putIfNotBlank(params, "sourceField", ruleDTO.getSourceField());
            return params;
        }
        return params;
    }

    public static void normalizeIndexes(List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList) {
        if (Objects.isNull(ruleList)) {
            return;
        }
        int ruleIndex = 1;
        for (CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO : ruleList) {
            ruleDTO.setIndex(ruleIndex++);
        }
    }

    public static String buildStorage(List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList) {
        Map<String, Object> storage = new HashMap<>(1);
        storage.put("data", Optional.ofNullable(ruleList).orElse(Collections.emptyList()));
        return JSON.toJSONString(storage);
    }

    public static List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> parseStorage(String etlRuleListStorage) {
        if (StringUtils.isBlank(etlRuleListStorage)) {
            return Collections.emptyList();
        }
        try {
            JSONObject jsonObject = JSON.parseObject(etlRuleListStorage);
            JSONArray data = jsonObject.getJSONArray("data");
            if (Objects.isNull(data)) {
                return Collections.emptyList();
            }
            List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList =
                    data.toJavaList(CfgLogisticsCostImportDetailDTO.EtlRuleDTO.class);
            ruleList.sort(Comparator.comparing(rule -> Optional.ofNullable(rule.getIndex()).orElse(0)));
            return ruleList;
        } catch (Exception e) {
            throw new ServiceException("字段清洗规则数据格式错误");
        }
    }

    public static void validateEtlRule(CfgLogisticsCostImportDetailDTO.EtlRuleDTO ruleDTO, int rowIndex, int ruleIndex) {
        if (Objects.isNull(ruleDTO) || StringUtils.isBlank(ruleDTO.getType())) {
            throw new ServiceException(prefix(rowIndex, ruleIndex) + "字段清洗规则类型不能为空");
        }
        Map<String, Object> params = resolveParams(ruleDTO);
        String type = ruleDTO.getType();
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), type)) {
            validateReplaceRule(params, rowIndex, ruleIndex);
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), type)) {
            validateSubstringRule(params, rowIndex, ruleIndex);
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode(), type)
                || Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode(), type)) {
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(), type)) {
            validateFillEmptyRule(params, rowIndex, ruleIndex);
            return;
        }
        throw new ServiceException(prefix(rowIndex, ruleIndex) + "字段清洗规则类型不合法");
    }

    public static String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return Objects.isNull(value) ? "" : String.valueOf(value);
    }

    public static Integer getIntegerParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void validateReplaceRule(Map<String, Object> params, int rowIndex, int ruleIndex) {
        String mode = getStringParam(params, "mode");
        if (StringUtils.isBlank(getStringParam(params, "sourceText"))) {
            throw new ServiceException(prefix(rowIndex, ruleIndex) + "字符替换规则原字符不能为空");
        }
        if (!Objects.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), mode)
                && !Objects.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_EMPTY.getCode(), mode)) {
            throw new ServiceException(prefix(rowIndex, ruleIndex) + "字符替换方式不合法");
        }
        if (Objects.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), mode)
                && StringUtils.isBlank(getStringParam(params, "targetText"))) {
            throw new ServiceException(prefix(rowIndex, ruleIndex) + "字符替换目标字符不能为空");
        }
    }

    private static void validateSubstringRule(Map<String, Object> params, int rowIndex, int ruleIndex) {
        String mode = getStringParam(params, "mode");
        if (Objects.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_SYMBOL.getCode(), mode)) {
            String symbolPosition = getStringParam(params, "symbolPosition");
            if (!Objects.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.BEFORE.getCode(), symbolPosition)
                    && !Objects.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.AFTER.getCode(), symbolPosition)) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "符号截取位置不合法");
            }
            if (StringUtils.isBlank(getStringParam(params, "symbol"))) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "字段截取符号不能为空");
            }
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), mode)) {
            String orderDirection = getStringParam(params, "orderDirection");
            if (!Objects.equals(CfgLogisticsCostImportEtlOrderDirectionEnum.LEFT.getCode(), orderDirection)
                    && !Objects.equals(CfgLogisticsCostImportEtlOrderDirectionEnum.RIGHT.getCode(), orderDirection)) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "顺序截取方向不合法");
            }
            Integer length = getIntegerParam(params, "length");
            if (Objects.isNull(length) || length <= 0) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "顺序截取长度必须大于0");
            }
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlSubstringModeEnum.CHINESE.getCode(), mode)
                || Objects.equals(CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode(), mode)) {
            return;
        }
        throw new ServiceException(prefix(rowIndex, ruleIndex) + "字段截取模式不合法");
    }

    private static void validateFillEmptyRule(Map<String, Object> params, int rowIndex, int ruleIndex) {
        String mode = getStringParam(params, "mode");
        if (Objects.equals(CfgLogisticsCostImportEtlFillModeEnum.CUSTOM.getCode(), mode)) {
            if (StringUtils.isBlank(getStringParam(params, "fillValue"))) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "自定义填充值不能为空");
            }
            return;
        }
        if (Objects.equals(CfgLogisticsCostImportEtlFillModeEnum.FIELD.getCode(), mode)) {
            if (StringUtils.isBlank(getStringParam(params, "sourceField"))) {
                throw new ServiceException(prefix(rowIndex, ruleIndex) + "字段取值表头不能为空");
            }
            return;
        }
        throw new ServiceException(prefix(rowIndex, ruleIndex) + "为空填充方式不合法");
    }

    private static String prefix(int rowIndex, int ruleIndex) {
        if (rowIndex > 0) {
            return "第" + rowIndex + "行第" + ruleIndex + "条";
        }
        return "第" + ruleIndex + "条";
    }

    private static void putIfNotBlank(Map<String, Object> params, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            params.put(key, value);
        }
    }
}
