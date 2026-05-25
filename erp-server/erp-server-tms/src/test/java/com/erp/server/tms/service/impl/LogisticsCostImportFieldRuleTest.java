package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgLogisticsCostImportDetailDTO;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlOrderDirectionEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlReplaceModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlRuleTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSymbolPositionEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportFieldUnitTypeEnum;
import com.erp.model.tms.util.CfgLogisticsCostImportEtlRuleHelper;
import com.erp.server.tms.service.CfgLogisticsCostImportFieldService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 验证物流费用导入字段默认值、清洗规则和单位标准化规则。
 *
 * @author jack
 * @date 2026/05/22
 */
public class LogisticsCostImportFieldRuleTest {

    /**
     * 验证默认值仅允许受控字段使用。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void validateDefaultValueShouldLimitDefaultValueScope() throws Exception {
        CfgLogisticsCostImportServiceImpl service = new CfgLogisticsCostImportServiceImpl();

        invokeValidateDefaultValue(service, updateDetail(false, "", "g"), field("logisticsWeightUnit", "物流商重量单位"), 1);
        assertThrowsServiceException(() -> invokeValidateDefaultValue(service, updateDetail(false, "", ""), field("trackingNo", "物流商单号"), 2));
        assertThrowsServiceException(() -> invokeValidateDefaultValue(service, updateDetail(true, "", ""), field("trackingNo", "物流商单号"), 3));
        assertThrowsServiceException(() -> invokeValidateDefaultValue(service, updateDetail(false, "重量", "KG"), field("logisticsWeightUnit", "物流商重量单位"), 4));
        assertThrowsServiceException(() -> invokeValidateDefaultValue(service, updateDetail(false, "", "lb"), field("logisticsWeightUnit", "物流商重量单位"), 5));
    }

    /**
     * 验证字段基础数据返回默认值下拉元数据。
     *
     * @return 无
     * @throws Exception 字段结构缺失时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void fieldDtoShouldExposeDefaultOptionMetadata() throws Exception {
        assertNotNull(CfgLogisticsCostImportFieldDTO.ListDTO.class.getDeclaredField("queryOptionId"));
        assertNotNull(CfgLogisticsCostImportFieldDTO.ListDTO.class.getDeclaredField("optionList"));
        assertNotNull(CfgLogisticsCostImportFieldDTO.TreeDTO.class.getDeclaredField("queryOptionId"));
        assertNotNull(CfgLogisticsCostImportFieldDTO.TreeDTO.class.getDeclaredField("optionList"));
    }

    /**
     * 验证清洗规则保存时生成顺序并按顺序回显。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void etlRuleShouldStoreIndexAndParseInOrder() throws Exception {
        CfgLogisticsCostImportServiceImpl service = new CfgLogisticsCostImportServiceImpl();
        CfgLogisticsCostImportDetailDTO.UpdateDTO detail = updateDetail(false, "费用", "");
        detail.setEtlRuleList(Arrays.asList(
                rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), "sourceText", "USD", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), "targetText", "CNY"),
                rule(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(), "mode", CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), "orderDirection", CfgLogisticsCostImportEtlOrderDirectionEnum.RIGHT.getCode(), "length", 3)
        ));

        invokePrivate(service, "normalizeEtlRuleList", new Class[]{List.class}, Collections.singletonList(detail));
        assertEquals(Integer.valueOf(1), detail.getEtlRuleList().get(0).getIndex());
        assertEquals(Integer.valueOf(2), detail.getEtlRuleList().get(1).getIndex());
        assertFalse(detail.getEtlRuleListStorage().contains("\"params\""));
        assertFalse(detail.getEtlRuleListStorage().contains("replaceMode"));
        assertFalse(detail.getEtlRuleListStorage().contains("fillMode"));

        List<CfgLogisticsCostImportDetailDTO.EtlRuleDTO> parsed = CfgLogisticsCostImportEtlRuleHelper.parseStorage(detail.getEtlRuleListStorage());
        assertEquals(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(), parsed.get(0).getType());
        assertEquals(CfgLogisticsCostImportEtlSubstringModeEnum.BY_ORDER.getCode(), parsed.get(1).getMode());
    }

    /**
     * 验证导入清洗执行器支持替换、截取、正负转换和空值填充。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void cleanFieldValueShouldApplyAllRuleTypes() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        Map<Integer, String> headMap = new HashMap<>();
        headMap.put(0, "备用金额");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "88");

        assertEquals("AB-001", clean(service, "A_001", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
                "sourceText", "_", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), "targetText", "-")), rowData, headMap));
        assertEquals("001", clean(service, "AB-001", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(),
                "mode", CfgLogisticsCostImportEtlSubstringModeEnum.BY_SYMBOL.getCode(), "symbol", "-", "symbolPosition", CfgLogisticsCostImportEtlSymbolPositionEnum.AFTER.getCode())), rowData, headMap));
        assertEquals("CN", clean(service, "CN2026", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.SUBSTRING.getCode(),
                "mode", CfgLogisticsCostImportEtlSubstringModeEnum.ENGLISH.getCode())), rowData, headMap));
        assertEquals("12.5", clean(service, "-12.5", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode())), rowData, headMap));
        assertEquals("-12.5", clean(service, "12.5", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode())), rowData, headMap));
        assertEquals("custom", clean(service, "", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(),
                "mode", CfgLogisticsCostImportEtlFillModeEnum.CUSTOM.getCode(), "fillValue", "custom")), rowData, headMap));
        assertEquals("88", clean(service, "", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.FILL_EMPTY.getCode(),
                "mode", CfgLogisticsCostImportEtlFillModeEnum.FIELD.getCode(), "sourceField", "备用金额")), rowData, headMap));
    }

    /**
     * 验证唯一识别字段使用清洗后的值参与匹配参数构建。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void cleanFieldValueShouldRejectUnknownRuleType() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();

        assertThrowsServiceException(() -> clean(service, "ABC",
                detailWithRules(rule("UNKNOWN")),
                new JSONObject(), Collections.emptyMap()));
    }

    /**
     * 验证唯一识别字段使用清洗后的值参与匹配参数构建。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void uniqueKeyShouldUsePreparedValueAfterCleaning() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("detail1", "trackingNo", "物流商单号");
        detail.setIsUniqueKey(true);
        detail.setSourceField("物流单号");
        detail.setEtlRuleList(Collections.singletonList(rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
                "sourceText", "NO-", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_EMPTY.getCode())));
        Map<Integer, String> headMap = Collections.singletonMap(0, "物流单号");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "NO-ABC");
        List<JSONObject> successList = Collections.singletonList(rowData);

        invokePrivate(service, "prepareImportRowValues", new Class[]{List.class, Map.class, List.class}, Collections.singletonList(detail), headMap, successList);
        @SuppressWarnings("unchecked")
        Map<String, List<Object>> paramMap = (Map<String, List<Object>>) invokePrivate(service, "buildParamMap", new Class[]{List.class, Map.class, List.class}, Collections.singletonList(detail), headMap, successList);
        assertEquals(Collections.singletonList("ABC"), paramMap.get("trackingNo"));
    }

    /**
     * 验证明细 DTO 不保存或回显字段单位属性。
     *
     * @return 无
     * @throws Exception 字段结构异常时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void detailDtoShouldNotExposeUnitMetadata() throws Exception {
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.CommonDTO.class, "unitType"));
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.CommonDTO.class, "standardUnit"));
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.ListDTO.class, "unitType"));
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.ListDTO.class, "standardUnit"));
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.ViewDTO.class, "unitType"));
        assertFalse(hasDeclaredField(CfgLogisticsCostImportDetailDTO.ViewDTO.class, "standardUnit"));
    }

    /**
     * 验证仅 weight 且标准单位为 kg 的字段执行 g 转 kg。
     *
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    @Test
    public void weightFieldShouldConvertGramToKilogramOnlyForWeightMetadata() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        injectFieldService(service, Arrays.asList(
                fieldMeta("chargeWeight", CfgLogisticsCostImportFieldUnitTypeEnum.WEIGHT.getCode(), "kg"),
                fieldMeta("packageLength", CfgLogisticsCostImportFieldUnitTypeEnum.LENGTH.getCode(), "cm"),
                fieldMeta("feeAmount", CfgLogisticsCostImportFieldUnitTypeEnum.AMOUNT.getCode(), "USD")
        ));
        CfgLogisticsCostImportEntity main = new CfgLogisticsCostImportEntity();
        main.setBusinessType("logisticsBillCost");
        CfgLogisticsCostImportDetailEntity unit = importDetail("unit", "logisticsWeightUnit", "物流商重量单位");
        CfgLogisticsCostImportDetailEntity weight = importDetail("weight", "chargeWeight", "计费重");
        CfgLogisticsCostImportDetailEntity length = importDetail("length", "packageLength", "长");
        CfgLogisticsCostImportDetailEntity amount = importDetail("amount", "feeAmount", "费用");
        List<CfgLogisticsCostImportDetailEntity> details = Arrays.asList(unit, weight, length, amount);
        JSONObject rowData = new JSONObject();
        setPrepared(service, rowData, unit, "g");
        setPrepared(service, rowData, weight, "1500");
        setPrepared(service, rowData, length, "20");
        setPrepared(service, rowData, amount, "300");

        @SuppressWarnings("unchecked")
        List<JSONObject> validRows = (List<JSONObject>) invokePrivate(service, "standardizeImportRowWeightValues",
                new Class[]{CfgLogisticsCostImportEntity.class, List.class, List.class, List.class, Map.class},
                main, details, Collections.singletonList(rowData), new ArrayList<>(), Collections.emptyMap());
        assertEquals(1, validRows.size());
        assertEquals("kg", getPrepared(service, rowData, unit));
        assertEquals("1.5", getPrepared(service, rowData, weight));
        assertEquals("20", getPrepared(service, rowData, length));
        assertEquals("300", getPrepared(service, rowData, amount));
    }

    /**
     * 调用默认值校验私有方法。
     *
     * @param service 服务实例
     * @param detail 明细配置
     * @param field 字段配置
     * @param index 行号
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void invokeValidateDefaultValue(CfgLogisticsCostImportServiceImpl service, CfgLogisticsCostImportDetailDTO.UpdateDTO detail,
                                            CfgLogisticsCostImportFieldEntity field, int index) throws Exception {
        invokePrivate(service, "validateDefaultValue",
                new Class[]{CfgLogisticsCostImportDetailDTO.UpdateDTO.class, CfgLogisticsCostImportFieldEntity.class, int.class},
                detail, field, index);
    }

    /**
     * 断言业务异常被抛出。
     *
     * @param action 待执行逻辑
     * @return 无
     * @throws Exception 非业务异常时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void assertThrowsServiceException(ThrowingRunnable action) throws Exception {
        try {
            action.run();
            fail("Expected ServiceException");
        } catch (ServiceException expected) {
            assertTrue(expected.getMessage().length() > 0);
        }
    }

    /**
     * 执行字段清洗私有方法。
     *
     * @param service 服务实例
     * @param value 原始值
     * @param detail 明细配置
     * @param rowData 行数据
     * @param headMap 表头映射
     * @return 清洗后字段值
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String clean(ImportHistoryRecordServiceImpl service, String value, CfgLogisticsCostImportDetailEntity detail,
                         JSONObject rowData, Map<Integer, String> headMap) throws Exception {
        return (String) invokePrivate(service, "cleanFieldValue",
                new Class[]{String.class, CfgLogisticsCostImportDetailEntity.class, JSONObject.class, Map.class},
                value, detail, rowData, headMap);
    }

    /**
     * 设置导入明细预处理值。
     *
     * @param service 服务实例
     * @param rowData 行数据
     * @param detail 明细配置
     * @param value 字段值
     * @return 无
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void setPrepared(ImportHistoryRecordServiceImpl service, JSONObject rowData, CfgLogisticsCostImportDetailEntity detail, String value) throws Exception {
        invokePrivate(service, "setPreparedValue", new Class[]{JSONObject.class, CfgLogisticsCostImportDetailEntity.class, String.class}, rowData, detail, value);
    }

    /**
     * 获取导入明细预处理值。
     *
     * @param service 服务实例
     * @param rowData 行数据
     * @param detail 明细配置
     * @return 字段值
     * @throws Exception 反射调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private String getPrepared(ImportHistoryRecordServiceImpl service, JSONObject rowData, CfgLogisticsCostImportDetailEntity detail) throws Exception {
        return (String) invokePrivate(service, "getPreparedValue", new Class[]{JSONObject.class, CfgLogisticsCostImportDetailEntity.class}, rowData, detail);
    }

    /**
     * 注入字段基础数据服务代理。
     *
     * @param service 服务实例
     * @param fieldList 字段元数据
     * @return 无
     * @throws Exception 反射注入失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private void injectFieldService(ImportHistoryRecordServiceImpl service, List<CfgLogisticsCostImportFieldDTO.ListDTO> fieldList) throws Exception {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("listByBusinessType".equals(method.getName())) {
                return fieldList;
            }
            if ("toString".equals(method.getName())) {
                return "CfgLogisticsCostImportFieldServiceProxy";
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            return null;
        };
        CfgLogisticsCostImportFieldService proxy = (CfgLogisticsCostImportFieldService) Proxy.newProxyInstance(
                CfgLogisticsCostImportFieldService.class.getClassLoader(),
                new Class[]{CfgLogisticsCostImportFieldService.class},
                handler);
        Field field = ImportHistoryRecordServiceImpl.class.getDeclaredField("cfgLogisticsCostImportFieldService");
        field.setAccessible(true);
        field.set(service, proxy);
    }

    /**
     * 调用私有方法并展开业务异常。
     *
     * @param target 目标对象
     * @param methodName 方法名称
     * @param parameterTypes 参数类型
     * @param args 参数
     * @return 方法返回值
     * @throws Exception 调用失败时抛出
     * @author jack
     * @date 2026/05/22
     */
    private Object invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }
    }

    /**
     * 构造配置明细入参。
     *
     * @param uniqueKey 是否唯一键
     * @param sourceField 抬头字段
     * @param defaultValue 默认值
     * @return 配置明细入参
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private CfgLogisticsCostImportDetailDTO.UpdateDTO updateDetail(boolean uniqueKey, String sourceField, String defaultValue) {
        CfgLogisticsCostImportDetailDTO.UpdateDTO detail = new CfgLogisticsCostImportDetailDTO.UpdateDTO();
        detail.setIsUniqueKey(uniqueKey);
        detail.setSourceField(sourceField);
        detail.setDefaultValue(defaultValue);
        return detail;
    }

    /**
     * 构造字段基础配置。
     *
     * @param fieldCode 字段编码
     * @param fieldName 字段名称
     * @return 字段基础配置
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private CfgLogisticsCostImportFieldEntity field(String fieldCode, String fieldName) {
        CfgLogisticsCostImportFieldEntity field = new CfgLogisticsCostImportFieldEntity();
        field.setField(fieldCode);
        field.setFieldName(fieldName);
        return field;
    }

    /**
     * 构造导入明细配置。
     *
     * @param id 明细 ID
     * @param targetField 目标字段
     * @param targetFieldName 目标字段名称
     * @return 导入明细配置
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private CfgLogisticsCostImportDetailEntity importDetail(String id, String targetField, String targetFieldName) {
        CfgLogisticsCostImportDetailEntity detail = new CfgLogisticsCostImportDetailEntity();
        detail.setId(id);
        detail.setTargetField(targetField);
        detail.setTargetFieldName(targetFieldName);
        detail.setIsUniqueKey(false);
        return detail;
    }

    /**
     * 构造带清洗规则的导入明细。
     *
     * @param rules 清洗规则
     * @return 导入明细配置
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private CfgLogisticsCostImportDetailEntity detailWithRules(CfgLogisticsCostImportDetailDTO.EtlRuleDTO... rules) {
        CfgLogisticsCostImportDetailEntity detail = importDetail("clean", "amount", "金额");
        detail.setEtlRuleList(Arrays.asList(rules));
        return detail;
    }

    /**
     * 构造扁平字段清洗规则。
     */
    private CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule(String type, Object... keyValues) {
        CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule = new CfgLogisticsCostImportDetailDTO.EtlRuleDTO();
        rule.setType(type);
        for (int i = 0; i < keyValues.length; i += 2) {
            applyRuleField(rule, String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return rule;
    }

    private void applyRuleField(CfgLogisticsCostImportDetailDTO.EtlRuleDTO rule, String key, Object value) {
        switch (key) {
            case "mode":
                rule.setMode(String.valueOf(value));
                break;
            case "sourceText":
                rule.setSourceText(String.valueOf(value));
                break;
            case "targetText":
                rule.setTargetText(String.valueOf(value));
                break;
            case "symbol":
                rule.setSymbol(String.valueOf(value));
                break;
            case "symbolPosition":
                rule.setSymbolPosition(String.valueOf(value));
                break;
            case "orderDirection":
                rule.setOrderDirection(String.valueOf(value));
                break;
            case "length":
                rule.setLength(value instanceof Integer ? (Integer) value : Integer.valueOf(String.valueOf(value)));
                break;
            case "fillValue":
                rule.setFillValue(String.valueOf(value));
                break;
            case "sourceField":
                rule.setSourceField(String.valueOf(value));
                break;
            default:
                throw new IllegalArgumentException("unknown etl rule field: " + key);
        }
    }

    /**
     * 构造字段元数据。
     *
     * @param field 字段编码
     * @param unitType 单位属性
     * @param standardUnit 标准单位
     * @return 字段元数据
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private CfgLogisticsCostImportFieldDTO.ListDTO fieldMeta(String field, String unitType, String standardUnit) {
        CfgLogisticsCostImportFieldDTO.ListDTO dto = new CfgLogisticsCostImportFieldDTO.ListDTO();
        dto.setField(field);
        dto.setUnitType(unitType);
        dto.setStandardUnit(standardUnit);
        return dto;
    }


    /**
     * 判断类是否声明字段。
     *
     * @param clazz 类
     * @param fieldName 字段名称
     * @return 是否声明字段
     * @throws
     * @author jack
     * @date 2026/05/22
     */
    private boolean hasDeclaredField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 执行可抛异常的逻辑。
     *
     * @author jack
     * @date 2026/05/22
     */
    private interface ThrowingRunnable {

        /**
         * 执行逻辑。
         *
         * @return 无
         * @throws Exception 执行失败时抛出
         * @author jack
         * @date 2026/05/22
         */
        void run() throws Exception;
    }
}
