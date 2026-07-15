package com.erp.server.tms.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.common.business.enums.UnitEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.enums.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 物流费用/对账导入共用的行级字段取值：Excel 映射、默认值、ETL 清洗、重量单位标准化。
 */
@Slf4j
public final class LogisticsCostImportRowValueHelper {

    private static final String COST_ITEM_FIELD = "costItem";
    private static final String ACTUAL_AMOUNT_FIELD = "actualAmount";
    private static final String ESTIMATED_AMOUNT_FIELD = "estimatedAmount";
    private static final String LOGISTICS_WEIGHT_UNIT_FIELD = "logisticsWeightUnit";
    private static final String BILLING_WEIGHT_LOGISTICS_FIELD = "billingWeightLogistics";
    private static final String THIRD_ACTUAL_WEIGHT_FIELD = "thirdActualWeight";

    private static final Pattern NON_CHINESE_PATTERN = Pattern.compile("[^\\u4e00-\\u9fa5]");
    private static final Pattern NON_ENGLISH_PATTERN = Pattern.compile("[^A-Za-z]");

    private LogisticsCostImportRowValueHelper() {
    }

    /**
     * 将 Excel 行转为 JSONObject（key = 列索引字符串）。
     */
    public static JSONObject toRowData(Map<Integer, String> row) {
        JSONObject rowData = new JSONObject();
        if (row == null) {
            return rowData;
        }
        row.forEach((index, value) -> rowData.set(String.valueOf(index), value));
        return rowData;
    }

    /**
     * 按费用配置准备导入行字段值（映射、默认值、ETL 清洗）。
     */
    public static void prepareImportRowValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                              Map<Integer, String> headMap,
                                              List<JSONObject> successList) {
        if (CollUtil.isEmpty(cfgImportDetailList) || CollUtil.isEmpty(successList)) {
            return;
        }
        int nextVirtualIndex = headMap.keySet().stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(-1) + 1;
        boolean isVertical = isVerticalCostItem(cfgImportDetailList);
        for (CfgLogisticsCostImportDetailEntity detail : cfgImportDetailList) {
            if (isVertical && isVerticalAmountDetail(detail)) {
                if (CharSequenceUtil.isNotBlank(detail.getSourceField())) {
                    Integer mappingIndex = getMapKey(headMap, detail.getSourceField());
                    if (ObjectUtil.isNotNull(mappingIndex)) {
                        detail.setMappingIndex(mappingIndex);
                    }
                }
                continue;
            }
            if (CharSequenceUtil.isBlank(detail.getSourceField())) {
                if (CharSequenceUtil.isBlank(detail.getDefaultValue())) {
                    continue;
                }
                if (ObjectUtil.isNull(detail.getMappingIndex())) {
                    detail.setMappingIndex(nextVirtualIndex++);
                }
                for (JSONObject rowData : successList) {
                    String cleanedValue = cleanFieldValue(detail.getDefaultValue(), detail, rowData, headMap);
                    setPreparedValue(rowData, detail, cleanedValue);
                }
                continue;
            }

            Integer mappingIndex = getMapKey(headMap, detail.getSourceField());
            if (ObjectUtil.isEmpty(mappingIndex)) {
                detail.setMappingIndex(null);
                if (Boolean.TRUE.equals(detail.getIsUniqueKey())) {
                    throw new ServiceException("唯一识别字段未匹配到 Excel 抬头：" + detail.getSourceField());
                }
                continue;
            }
            detail.setMappingIndex(mappingIndex);
            String mappingKey = mappingIndex.toString();
            if (isVertical && isVerticalCostItemDetail(detail)) {
                for (JSONObject rowData : successList) {
                    Object rawValue = rowData.get(mappingKey);
                    String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
                    setPreparedValue(rowData, detail, resolvedValue);
                    if (!CharSequenceUtil.equals(detail.getSourceDetailField(), resolvedValue)) {
                        continue;
                    }
                    prepareVerticalAmountValue(cfgImportDetailList, ACTUAL_AMOUNT_FIELD, detail, rowData, headMap);
                    prepareVerticalAmountValue(cfgImportDetailList, ESTIMATED_AMOUNT_FIELD, detail, rowData, headMap);
                }
                continue;
            }
            for (JSONObject rowData : successList) {
                Object rawValue = rowData.get(mappingKey);
                String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
                String cleanedValue = cleanFieldValue(resolvedValue, detail, rowData, headMap);
                setPreparedValue(rowData, detail, cleanedValue);
            }
        }
    }

    /**
     * 重量单位为 g 时，将计费重/实重换算为 kg 并回写单位。
     */
    public static void standardizeImportRowWeightValues(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                        List<JSONObject> successList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        Optional<CfgLogisticsCostImportDetailEntity> unitDetailOpt = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), LOGISTICS_WEIGHT_UNIT_FIELD))
                .findFirst();
        if (!unitDetailOpt.isPresent()) {
            return;
        }
        CfgLogisticsCostImportDetailEntity unitDetail = unitDetailOpt.get();
        List<CfgLogisticsCostImportDetailEntity> weightDetails = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), BILLING_WEIGHT_LOGISTICS_FIELD)
                        || CharSequenceUtil.equals(detail.getTargetField(), THIRD_ACTUAL_WEIGHT_FIELD))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(weightDetails)) {
            return;
        }
        for (JSONObject rowData : successList) {
            String unit = getPreparedValue(rowData, unitDetail);
            if (!UnitEnum.WeightUnitEnum.G.getCode().equalsIgnoreCase(unit)) {
                continue;
            }
            Map<CfgLogisticsCostImportDetailEntity, String> convertedWeightMap = new HashMap<>();
            boolean convertFailed = false;
            for (CfgLogisticsCostImportDetailEntity weightDetail : weightDetails) {
                String weightValue = getPreparedValue(rowData, weightDetail);
                if (CharSequenceUtil.isBlank(weightValue)) {
                    continue;
                }
                try {
                    BigDecimal kgValue = new BigDecimal(weightValue).divide(new BigDecimal("1000"), 4, RoundingMode.DOWN);
                    convertedWeightMap.put(weightDetail, kgValue.stripTrailingZeros().toPlainString());
                } catch (NumberFormatException e) {
                    log.warn("物流商重量值无法转换为 KG：{}", weightValue);
                    convertFailed = true;
                }
            }
            if (convertFailed) {
                continue;
            }
            convertedWeightMap.forEach((weightDetail, value) -> setPreparedValue(rowData, weightDetail, value));
            setPreparedValue(rowData, unitDetail, UnitEnum.WeightUnitEnum.KG.getCode());
        }
    }

    public static String getPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail) {
        // 横向多费用列 targetField 均为 costItem，且 setPreparedValue 会覆盖同一公共键；
        // 费用金额只认 mappingIndex 对应列，禁止回退 costItem，避免列缺失或单元格为空时误读其它费用列金额。
        if (CharSequenceUtil.equals(COST_ITEM_FIELD, detail.getTargetField())) {
            if (ObjectUtil.isNull(detail.getMappingIndex())) {
                return "";
            }
            Object costValue = rowData.get(detail.getMappingIndex().toString());
            return ObjectUtil.isEmpty(costValue) ? "" : String.valueOf(costValue);
        }
        Object value = ObjectUtil.isNotNull(detail.getMappingIndex())
                ? rowData.get(detail.getMappingIndex().toString()) : null;
        if (ObjectUtil.isEmpty(value) && CharSequenceUtil.isNotBlank(detail.getTargetField())) {
            value = rowData.get(detail.getTargetField());
        }
        if (ObjectUtil.isEmpty(value)) {
            return "";
        }
        return String.valueOf(value);
    }

    public static Integer getMapKey(Map<Integer, String> headMap, String targetValue) {
        if (headMap == null || CharSequenceUtil.isBlank(targetValue)) {
            return null;
        }
        String cleanedTarget = targetValue.trim();
        for (Integer key : headMap.keySet()) {
            String value = headMap.get(key);
            if (value == null) {
                continue;
            }
            String cleanedValue = value.trim().replaceAll("\n", " ");
            if (cleanedValue.equals(cleanedTarget)) {
                return key;
            }
        }
        return null;
    }

    public static String cleanFieldValue(String value,
                                         CfgLogisticsCostImportDetailEntity detail,
                                         JSONObject rowData,
                                         Map<Integer, String> headMap) {
        String result = ObjectUtil.isEmpty(value) ? "" : String.valueOf(value);
        for (CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule : getSortedEtlRuleList(detail)) {
            String type = rule.getType();
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), type)) {
                result = applyReplaceRule(result, rule);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), type)) {
                result = applySubstringRule(result, rule);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode(), type)) {
                result = toSignedNumberText(result, false);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode(), type)) {
                result = toSignedNumberText(result, true);
                continue;
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(), type)) {
                result = applyFillEmptyRule(result, rule, rowData, headMap);
            }
        }
        return result;
    }

    public static boolean isVerticalCostItem(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList) {
        return cfgImportDetailList.stream()
                .anyMatch(obj -> CharSequenceUtil.equals(obj.getTargetField(), COST_ITEM_FIELD)
                        && CharSequenceUtil.isNotBlank(obj.getSourceDetailField()));
    }

    private static boolean isVerticalCostItemDetail(CfgLogisticsCostImportDetailEntity detail) {
        return CharSequenceUtil.equals(detail.getTargetField(), COST_ITEM_FIELD)
                && CharSequenceUtil.isNotBlank(detail.getSourceDetailField());
    }

    private static boolean isVerticalAmountDetail(CfgLogisticsCostImportDetailEntity detail) {
        return CharSequenceUtil.equals(detail.getTargetField(), ACTUAL_AMOUNT_FIELD)
                || CharSequenceUtil.equals(detail.getTargetField(), ESTIMATED_AMOUNT_FIELD);
    }

    private static void prepareVerticalAmountValue(List<CfgLogisticsCostImportDetailEntity> cfgImportDetailList,
                                                   String amountTargetField,
                                                   CfgLogisticsCostImportDetailEntity costItemDetail,
                                                   JSONObject rowData,
                                                   Map<Integer, String> headMap) {
        Optional<CfgLogisticsCostImportDetailEntity> amountDetailOpt = cfgImportDetailList.stream()
                .filter(detail -> CharSequenceUtil.equals(detail.getTargetField(), amountTargetField))
                .findFirst();
        if (!amountDetailOpt.isPresent()) {
            return;
        }
        CfgLogisticsCostImportDetailEntity amountDetail = amountDetailOpt.get();
        Integer mappingIndex = amountDetail.getMappingIndex();
        if (ObjectUtil.isNull(mappingIndex)) {
            if (CharSequenceUtil.isBlank(amountDetail.getSourceField())) {
                return;
            }
            mappingIndex = getMapKey(headMap, amountDetail.getSourceField());
            if (ObjectUtil.isNull(mappingIndex)) {
                return;
            }
            amountDetail.setMappingIndex(mappingIndex);
        }
        Object rawValue = rowData.get(mappingIndex.toString());
        String resolvedValue = ObjectUtil.isEmpty(rawValue) ? "" : String.valueOf(rawValue);
        String cleanedValue = cleanFieldValue(resolvedValue, costItemDetail, rowData, headMap);
        setPreparedValue(rowData, amountDetail, cleanedValue);
    }

    public static void setPreparedValue(JSONObject rowData, CfgLogisticsCostImportDetailEntity detail, String value) {
        String text = ObjectUtil.isEmpty(value) ? "" : value;
        if (ObjectUtil.isNotNull(detail.getMappingIndex())) {
            rowData.set(detail.getMappingIndex().toString(), text);
        }
        if (CharSequenceUtil.isNotBlank(detail.getTargetField())) {
            rowData.set(detail.getTargetField(), text);
        }
    }

    private static List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> getSortedEtlRuleList(
            CfgLogisticsCostImportDetailEntity detail) {
        List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> ruleList = detail.getEtlRuleList();
        if (CollUtil.isEmpty(ruleList)) {
            ruleList = CfgLogisticsCostImportEtlRuleHelper.parseStorage(detail.getEtlRuleListStorage());
            detail.setEtlRuleList(ruleList);
        }
        if (CollUtil.isEmpty(ruleList)) {
            return Collections.emptyList();
        }
        return ruleList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(rule -> Optional.ofNullable(rule.getIndex()).orElse(0)))
                .collect(Collectors.toList());
    }

    private static String applyReplaceRule(String value, CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule) {
        if (CharSequenceUtil.isBlank(rule.getSourceText())) {
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_EMPTY.getCode(), rule.getMode())) {
            return value.replace(rule.getSourceText(), "");
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), rule.getMode())) {
            return value.replace(rule.getSourceText(), rule.getTargetText() == null ? "" : rule.getTargetText());
        }
        return value;
    }

    private static String applySubstringRule(String value, CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        String mode = rule.getMode();
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_SYMBOL.getCode(), mode)) {
            String symbol = rule.getSymbol();
            if (CharSequenceUtil.isBlank(symbol) || !value.contains(symbol)) {
                return value;
            }
            int index = value.indexOf(symbol);
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.BEFORE.getCode(), rule.getSymbolPosition())) {
                return value.substring(0, index);
            }
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSymbolPositionEnum.AFTER.getCode(), rule.getSymbolPosition())) {
                return value.substring(index + symbol.length());
            }
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), mode)) {
            Integer length = rule.getLength();
            if (ObjectUtil.isNull(length) || length <= 0) {
                return value;
            }
            int safeLength = Math.min(length, value.length());
            if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlOrderDirectionEnum.RIGHT.getCode(), rule.getOrderDirection())) {
                return value.substring(value.length() - safeLength);
            }
            return value.substring(0, safeLength);
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.CHINESE.getCode(), mode)) {
            return NON_CHINESE_PATTERN.matcher(value).replaceAll("");
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode(), mode)) {
            return NON_ENGLISH_PATTERN.matcher(value).replaceAll("");
        }
        return value;
    }

    private static String applyFillEmptyRule(String value,
                                             CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule,
                                             JSONObject rowData,
                                             Map<Integer, String> headMap) {
        if (CharSequenceUtil.isNotBlank(value)) {
            return value;
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlFillModeEnum.CUSTOM.getCode(), rule.getMode())) {
            return rule.getFillValue() == null ? "" : rule.getFillValue();
        }
        if (CharSequenceUtil.equals(CfgLogisticsCostImportEtlFillModeEnum.FIELD.getCode(), rule.getMode())) {
            Integer fieldIndex = getMapKey(headMap, rule.getSourceField());
            if (ObjectUtil.isNull(fieldIndex)) {
                return value;
            }
            Object fieldValue = rowData.get(fieldIndex.toString());
            return ObjectUtil.isEmpty(fieldValue) ? "" : String.valueOf(fieldValue);
        }
        return value;
    }

    private static String toSignedNumberText(String value, boolean negative) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }
        try {
            BigDecimal number = new BigDecimal(value.trim());
            BigDecimal signedNumber = negative ? number.abs().negate() : number.abs();
            return signedNumber.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
