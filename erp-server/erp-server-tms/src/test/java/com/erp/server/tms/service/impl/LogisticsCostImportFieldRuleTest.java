package com.erp.server.tms.service.impl;

import cn.hutool.json.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.ImportHistoryRecordExcelDTO;
import com.erp.model.tms.enums.CfgLogisticsCostImportCfgTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportIdentifyTypeEnum;
import com.erp.model.tms.entity.CfgLogisticsCostImportEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportDetailEntity;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlOrderDirectionEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlReplaceModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlRuleTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportImportTypeEnum;
import com.erp.model.tms.enums.LogisticsBillCostCheckStatusEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.model.tms.enums.logisticsPayTypeEnum;
import com.erp.model.tms.enums.CfgLogisticsCostImportEtlSymbolPositionEnum;
import com.erp.server.tms.util.CfgLogisticsCostImportEtlRuleHelper;
import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

        assertEquals("A-001", clean(service, "A_001", detailWithRules(rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
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

    @Test
    public void matchesCostImportConfigShouldFilterBySupplierWhenIdentifyNoSupplier() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO_SUPPLIER.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        LogisticsBillDTO.LogisticsBillVo matched = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        LogisticsBillDTO.LogisticsBillVo other = billVoWithSupplier("bill-2", "detail-2", "supplier-b");

        assertTrue((Boolean) invokePrivate(service, "matchesCostImportConfig",
                new Class[]{CfgLogisticsCostImportEntity.class, LogisticsBillDTO.LogisticsBillVo.class}, config, matched));
        assertFalse((Boolean) invokePrivate(service, "matchesCostImportConfig",
                new Class[]{CfgLogisticsCostImportEntity.class, LogisticsBillDTO.LogisticsBillVo.class}, config, other));
    }

    @Test
    public void matchesCostImportConfigShouldSkipSupplierFilterWhenIdentifyNo() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        LogisticsBillDTO.LogisticsBillVo other = billVoWithSupplier("bill-2", "detail-2", "supplier-b");

        assertTrue((Boolean) invokePrivate(service, "matchesCostImportConfig",
                new Class[]{CfgLogisticsCostImportEntity.class, LogisticsBillDTO.LogisticsBillVo.class}, config, other));
    }

    @Test
    public void validateDuplicateImportConfigShouldRejectSameRecognitionName() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportEntity first = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        first.setName("模板A");
        first.setSheetName("sheet1");
        first.setCostType("excel");
        CfgLogisticsCostImportEntity second = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO_SUPPLIER.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        second.setName("模板A");
        second.setSheetName("sheet1");
        second.setCostType("excel");

        assertThrowsServiceException(() -> invokePrivate(service, "validateDuplicateImportConfig",
                new Class[]{List.class}, Arrays.asList(first, second)));
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldOnlyUpdateWhenGroupHasToBeConfirm() throws Exception {
        // 分组内混有待确认与已确认时，仅待确认进更新批次，已确认（跨月）不进入 addOld 批次。
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo toConfirm1 = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        LogisticsBillDTO.LogisticsBillVo toConfirm2 = billVoWithSupplier("bill-2", "detail-2", "supplier-b");
        LogisticsBillDTO.LogisticsBillVo confirmedOtherMonth = billVoWithSupplier("bill-3", "detail-3", "supplier-c");
        List<LogisticsBillCostEntity> costList = Arrays.asList(
                billCost("cost-1", "detail-1", payType, ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-05"),
                billCost("cost-2", "detail-2", payType, ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-05"),
                billCost("cost-3", "detail-3", payType, ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-04"));
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode()
                + "," + CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Arrays.asList(toConfirm1, toConfirm2, confirmedOtherMonth), costList, payType, "2026-05", config,
                updateBillList, addOldBillList);

        assertEquals(2, updateBillList.size());
        assertTrue(addOldBillList.isEmpty());
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldAddOldWhenNoToBeConfirm() throws Exception {
        // 无待确认时，已确认上月单 + 本月导入 → 仅 addOld 批次。
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        List<LogisticsBillCostEntity> costList = Collections.singletonList(
                billCost("cost-1", "detail-1", payType, ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-04"));
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Collections.singletonList(billVo), costList, payType, "2026-05", config,
                updateBillList, addOldBillList);

        assertTrue(updateBillList.isEmpty());
        assertEquals(1, addOldBillList.size());
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldAddOldForSameMonthConfirmedWhenNoToBeConfirm() throws Exception {
        // 全组无待确认时，同月已确认也进 addOld 批次，由下游 checkCostImportData 拦截重复新增。
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        List<LogisticsBillCostEntity> costList = Collections.singletonList(
                billCost("cost-1", "detail-1", payType, ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-05"));
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Collections.singletonList(billVo), costList, payType, "2026-05", config,
                updateBillList, addOldBillList);

        assertTrue(updateBillList.isEmpty());
        assertEquals(1, addOldBillList.size());
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldAddOldWhenDifferentPayType() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String importPayType = logisticsPayTypeEnum.REFUND.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        LogisticsBillCostEntity payCost = billCost("cost-1", "detail-1", logisticsPayTypeEnum.PAY.getCode(),
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        payCost.setLogisticsBillId("bill-1");
        payCost.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Collections.singletonList(billVo), Collections.singletonList(payCost), importPayType, "2026-06", config,
                updateBillList, addOldBillList);

        assertTrue(updateBillList.isEmpty());
        assertEquals(1, addOldBillList.size());
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldNotUpdatePendingCostFromDifferentMonth() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        LogisticsBillCostEntity pendingJuly = billCost("cost-july", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-07");
        pendingJuly.setLogisticsBillId("bill-1");
        LogisticsBillCostEntity confirmedJune = billCost("cost-june", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        confirmedJune.setLogisticsBillId("bill-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode()
                + "," + CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Collections.singletonList(billVo), Arrays.asList(pendingJuly, confirmedJune), payType, "2026-06", config,
                updateBillList, addOldBillList);

        assertTrue(updateBillList.isEmpty());
        assertEquals(1, addOldBillList.size());
    }

    @Test
    public void checkCostImportDataShouldBlockAddOldWhenConfirmedSameMonthAndPayType() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity confirmedJune = billCost("cost-1", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        confirmedJune.setLogisticsBillId("bill-1");
        confirmedJune.setTrackNo("track-1");
        LogisticsBillCostEntity confirmedMay = billCost("cost-2", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-05");
        confirmedMay.setLogisticsBillId("bill-1");
        confirmedMay.setTrackNo("track-1");
        ImportHistoryRecordExcelDTO excelDTO = new ImportHistoryRecordExcelDTO();
        excelDTO.setPayType(payType);
        excelDTO.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<String> errorMsgList = new ArrayList<>();

        invokePrivate(service, "checkCostImportData",
                new Class[]{ImportHistoryRecordExcelDTO.class, List.class, LogisticsBillDTO.LogisticsBillVo.class,
                        CfgLogisticsCostImportEntity.class, List.class},
                excelDTO, Arrays.asList(confirmedJune, confirmedMay), billVo, config, errorMsgList);

        assertTrue(errorMsgList.stream().anyMatch(msg -> msg.contains("已存在相同对账月份和付款类型的物流费用单")));
    }

    @Test
    public void checkCostImportDataShouldBlockAddOldWhenConfirmedSameMonthExcludedFromEntityList() throws Exception {
        // 已确认 2026-06 费用单 trackNo 与命中物流单不一致时，旧 size==1 分支只看 2026-05 单会放行；ERP-18299 按 detailId 全量拦截。
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity confirmedJune = billCost("cost-1", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        confirmedJune.setLogisticsBillId("bill-1");
        confirmedJune.setTrackNo("track-other");
        LogisticsBillCostEntity confirmedMay = billCost("cost-2", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-05");
        confirmedMay.setLogisticsBillId("bill-1");
        confirmedMay.setTrackNo("track-1");
        ImportHistoryRecordExcelDTO excelDTO = new ImportHistoryRecordExcelDTO();
        excelDTO.setPayType(payType);
        excelDTO.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<String> errorMsgList = new ArrayList<>();

        invokePrivate(service, "checkCostImportData",
                new Class[]{ImportHistoryRecordExcelDTO.class, List.class, LogisticsBillDTO.LogisticsBillVo.class,
                        CfgLogisticsCostImportEntity.class, List.class},
                excelDTO, Arrays.asList(confirmedJune, confirmedMay), billVo, config, errorMsgList);

        assertTrue(errorMsgList.stream().anyMatch(msg -> msg.contains("已存在相同对账月份和付款类型的物流费用单")));
    }

    @Test
    public void checkCostImportDataShouldBlockAddOldWhenSamePayTypeAndNullExistingMonth() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity existing = billCost("cost-1", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), null);
        existing.setLogisticsBillId("bill-1");
        existing.setTrackNo("track-1");
        ImportHistoryRecordExcelDTO excelDTO = new ImportHistoryRecordExcelDTO();
        excelDTO.setPayType(payType);
        excelDTO.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<String> errorMsgList = new ArrayList<>();

        invokePrivate(service, "checkCostImportData",
                new Class[]{ImportHistoryRecordExcelDTO.class, List.class, LogisticsBillDTO.LogisticsBillVo.class,
                        CfgLogisticsCostImportEntity.class, List.class},
                excelDTO, Collections.singletonList(existing), billVo, config, errorMsgList);

        assertTrue(errorMsgList.stream().anyMatch(msg -> msg.contains("已存在相同对账月份和付款类型的物流费用单")));
    }

    @Test
    public void checkCostImportDataShouldBlockCrossMonthPendingUpdateWhenTargetMonthExists() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity confirmedJune = billCost("cost-june", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        confirmedJune.setLogisticsBillId("bill-1");
        LogisticsBillCostEntity pendingJuly = billCost("cost-july", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-07");
        pendingJuly.setLogisticsBillId("bill-1");
        ImportHistoryRecordExcelDTO excelDTO = new ImportHistoryRecordExcelDTO();
        excelDTO.setPayType(payType);
        excelDTO.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode()
                + "," + CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<String> errorMsgList = new ArrayList<>();

        invokePrivate(service, "checkCostImportData",
                new Class[]{ImportHistoryRecordExcelDTO.class, List.class, LogisticsBillDTO.LogisticsBillVo.class,
                        CfgLogisticsCostImportEntity.class, List.class},
                excelDTO, Arrays.asList(confirmedJune, pendingJuly), billVo, config, errorMsgList);

        assertTrue(errorMsgList.stream().anyMatch(msg -> msg.contains("相同对账月份和付款类型")));
    }

    @Test
    public void checkCostImportDataShouldBlockUpdateWhenTargetMonthAlreadyHasMultipleCosts() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVo("bill-1", "detail-1", null, "track-1", "so-1");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity pendingJune = billCost("cost-pending", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-06");
        pendingJune.setLogisticsBillId("bill-1");
        LogisticsBillCostEntity confirmedJune = billCost("cost-confirmed", "detail-1", payType,
                ReconciliationStatusEnum.CONFIRMED.getCode(), "2026-06");
        confirmedJune.setLogisticsBillId("bill-1");
        ImportHistoryRecordExcelDTO excelDTO = new ImportHistoryRecordExcelDTO();
        excelDTO.setPayType(payType);
        excelDTO.setTrackNo("track-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode());
        List<String> errorMsgList = new ArrayList<>();

        invokePrivate(service, "checkCostImportData",
                new Class[]{ImportHistoryRecordExcelDTO.class, List.class, LogisticsBillDTO.LogisticsBillVo.class,
                        CfgLogisticsCostImportEntity.class, List.class},
                excelDTO, Arrays.asList(pendingJune, confirmedJune), billVo, config, errorMsgList);

        assertTrue(errorMsgList.stream().anyMatch(msg -> msg.contains("不支持更新")));
    }

    @Test
    public void findMatchedLogisticsBillCostShouldSelectPendingCostFromTargetMonth() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity pendingJuly = billCost("cost-july", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-07");
        LogisticsBillCostEntity pendingJune = billCost("cost-june", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-06");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");

        LogisticsBillCostEntity matched = (LogisticsBillCostEntity) invokePrivate(service, "findMatchedLogisticsBillCost",
                new Class[]{List.class, LogisticsBillDTO.LogisticsBillVo.class, String.class, String.class,
                        CfgLogisticsCostImportEntity.class},
                Arrays.asList(pendingJuly, pendingJune), billVo, payType,
                CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode(), config);

        assertEquals("cost-june", matched.getId());
    }

    @Test
    public void findMatchedLogisticsBillCostShouldAllowEmptyMonthPendingUpdate() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity pendingEmpty = billCost("cost-empty", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), null);
        LogisticsBillCostEntity pendingJuly = billCost("cost-july", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-07");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");

        LogisticsBillCostEntity matched = (LogisticsBillCostEntity) invokePrivate(service, "findMatchedLogisticsBillCost",
                new Class[]{List.class, LogisticsBillDTO.LogisticsBillVo.class, String.class, String.class,
                        CfgLogisticsCostImportEntity.class},
                Arrays.asList(pendingJuly, pendingEmpty), billVo, payType,
                CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode(), config);

        assertEquals("cost-empty", matched.getId());
    }

    @Test
    public void findMatchedLogisticsBillCostShouldPreferExactMonthOverEmptyMonth() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        billVo.setReconciliationMonth("2026-06");
        LogisticsBillCostEntity pendingEmpty = billCost("cost-empty", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), null);
        LogisticsBillCostEntity pendingJune = billCost("cost-june", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), "2026-06");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");

        LogisticsBillCostEntity matched = (LogisticsBillCostEntity) invokePrivate(service, "findMatchedLogisticsBillCost",
                new Class[]{List.class, LogisticsBillDTO.LogisticsBillVo.class, String.class, String.class,
                        CfgLogisticsCostImportEntity.class},
                Arrays.asList(pendingEmpty, pendingJune), billVo, payType,
                CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode(), config);

        assertEquals("cost-june", matched.getId());
    }

    @Test
    public void splitIdentifyNoProcessBillsShouldUpdateEmptyMonthPendingForTargetMonth() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        LogisticsBillCostEntity pendingEmpty = billCost("cost-empty", "detail-1", payType,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), null);
        pendingEmpty.setLogisticsBillId("bill-1");
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");
        config.setImportType(CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode()
                + "," + CfgLogisticsCostImportImportTypeEnum.IMPORT_ADD_OLD.getCode());
        List<LogisticsBillDTO.LogisticsBillVo> updateBillList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> addOldBillList = new ArrayList<>();

        invokePrivate(service, "splitIdentifyNoProcessBills",
                new Class[]{List.class, List.class, String.class, String.class, CfgLogisticsCostImportEntity.class, List.class, List.class},
                Collections.singletonList(billVo), Collections.singletonList(pendingEmpty), payType, "2026-06", config,
                updateBillList, addOldBillList);

        assertEquals(1, updateBillList.size());
        assertTrue(addOldBillList.isEmpty());
    }

    @Test
    public void findMatchedLogisticsBillCostShouldSkipEstimateConfirmForIdentifyNo() throws Exception {
        // identify_no + IMPORT_UPDATE 不匹配暂估确认，避免误更新不可分摊状态的费用单。
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        String payType = logisticsPayTypeEnum.PAY.getCode();
        LogisticsBillDTO.LogisticsBillVo billVo = billVoWithSupplier("bill-1", "detail-1", "supplier-a");
        LogisticsBillCostEntity estimateCost = billCost("cost-1", "detail-1", payType,
                ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(), "2026-05");
        estimateCost.setCheckStatus(LogisticsBillCostCheckStatusEnum.CHECKING.getCode());
        CfgLogisticsCostImportEntity config = costImportConfig(
                CfgLogisticsCostImportIdentifyTypeEnum.IDENTIFY_NO.getCode(),
                CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),
                "supplier-a");

        LogisticsBillCostEntity matched = (LogisticsBillCostEntity) invokePrivate(service, "findMatchedLogisticsBillCost",
                new Class[]{List.class, LogisticsBillDTO.LogisticsBillVo.class, String.class, String.class, CfgLogisticsCostImportEntity.class},
                Collections.singletonList(estimateCost), billVo, payType,
                CfgLogisticsCostImportImportTypeEnum.IMPORT_UPDATE.getCode(), config);

        assertNull(matched);
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

    @Test
    public void prepareImportRowValuesShouldNotFallbackToDefaultForSourceField() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("source", "trackingNo", "物流商单号");
        detail.setSourceField("物流单号");
        detail.setDefaultValue("DEFAULT-NO");
        Map<Integer, String> headMap = Collections.singletonMap(0, "物流单号");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "");

        invokePrivate(service, "prepareImportRowValues", new Class[]{List.class, Map.class, List.class}, Collections.singletonList(detail), headMap, Collections.singletonList(rowData));

        assertEquals("", getPrepared(service, rowData, detail));
        assertEquals("", rowData.getStr("trackingNo"));
    }

    @Test
    public void prepareImportRowValuesShouldEnrichDefaultOnlyVirtualField() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("virtual", "logisticsWeightUnit", "物流商重量单位");
        detail.setDefaultValue("g");
        detail.setEtlRuleList(Collections.singletonList(rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
                "sourceText", "g", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), "targetText", "kg")));
        JSONObject rowData = new JSONObject();

        invokePrivate(service, "prepareImportRowValues", new Class[]{List.class, Map.class, List.class}, Collections.singletonList(detail), Collections.emptyMap(), Collections.singletonList(rowData));

        assertEquals("kg", getPrepared(service, rowData, detail));
        assertEquals("kg", rowData.getStr("logisticsWeightUnit"));
    }

    @Test
    public void uniqueKeyShouldRejectDefaultOnlyVirtualField() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("unique", "trackingNo", "物流商单号");
        detail.setIsUniqueKey(true);
        detail.setDefaultValue("DEFAULT-NO");

        assertThrowsServiceException(() -> invokePrivate(service, "extractUniqueKeyList", new Class[]{List.class}, Collections.singletonList(detail)));
    }

    @Test
    public void prepareImportRowValuesShouldApplyRulesByIndexOrder() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("ordered", "trackingNo", "物流商单号");
        detail.setSourceField("物流单号");
        CfgLogisticsCostImportDetailDTO.EtlRuleDTO second = rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
                "sourceText", "B", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), "targetText", "C");
        second.setIndex(2);
        CfgLogisticsCostImportDetailDTO.EtlRuleDTO first = rule(CfgLogisticsCostImportEtlRuleTypeEnum.REPLACE.getCode(),
                "sourceText", "A", "mode", CfgLogisticsCostImportEtlReplaceModeEnum.REPLACE_TO.getCode(), "targetText", "B");
        first.setIndex(1);
        detail.setEtlRuleList(Arrays.asList(second, first));
        Map<Integer, String> headMap = Collections.singletonMap(0, "物流单号");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "A-001");

        invokePrivate(service, "prepareImportRowValues", new Class[]{List.class, Map.class, List.class}, Collections.singletonList(detail), headMap, Collections.singletonList(rowData));

        assertEquals("C-001", getPrepared(service, rowData, detail));
    }

    @Test
    public void prepareImportRowValuesShouldCleanVerticalAmountsByCostItemRule() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity costItem = importDetail("costItem", "costItem", "费用项");
        costItem.setSourceField("费用明细");
        costItem.setSourceDetailField("自提附加费_B2C");
        costItem.setEtlRuleList(Collections.singletonList(rule(CfgLogisticsCostImportEtlRuleTypeEnum.TO_NEGATIVE.getCode())));
        CfgLogisticsCostImportDetailEntity actualAmount = importDetail("actualAmount", "actualAmount", "实际金额");
        actualAmount.setSourceField("本期计算");
        actualAmount.setEtlRuleList(Collections.singletonList(rule(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode())));
        CfgLogisticsCostImportDetailEntity estimatedAmount = importDetail("estimatedAmount", "estimatedAmount", "预估金额");
        estimatedAmount.setSourceField("本期费用小计");
        estimatedAmount.setEtlRuleList(Collections.singletonList(rule(CfgLogisticsCostImportEtlRuleTypeEnum.TO_POSITIVE.getCode())));
        List<CfgLogisticsCostImportDetailEntity> details = Arrays.asList(costItem, actualAmount, estimatedAmount);
        Map<Integer, String> headMap = new HashMap<>();
        headMap.put(0, "费用明细");
        headMap.put(1, "本期计算");
        headMap.put(2, "本期费用小计");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "自提附加费_B2C");
        rowData.set("1", "0.50");
        rowData.set("2", "0.50");

        invokePrivate(service, "prepareImportRowValues", new Class[]{List.class, Map.class, List.class}, details, headMap, Collections.singletonList(rowData));

        assertEquals("自提附加费_B2C", getPrepared(service, rowData, costItem));
        assertEquals("-0.5", getPrepared(service, rowData, actualAmount));
        assertEquals("-0.5", getPrepared(service, rowData, estimatedAmount));
    }

    @Test
    public void cleanFileShouldProjectCleanedColumns() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity detail = importDetail("amount", "actualAmount", "实际金额");
        detail.setSourceField("金额");
        detail.setMappingIndex(0);
        JSONObject rowData = new JSONObject();
        rowData.set("0", "USD 88");
        setPrepared(service, rowData, detail, "88");

        invokePrivate(service, "projectCleanFileRows", new Class[]{List.class, List.class},
                Collections.singletonList(detail), Collections.singletonList(rowData));

        assertEquals("88", rowData.getStr("0"));
    }

    @Test
    public void cleanFileCleanedModeShouldProjectCleanedAndVirtualValues() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity amount = importDetail("amount", "actualAmount", "实际金额");
        amount.setSourceField("金额");
        amount.setMappingIndex(0);
        CfgLogisticsCostImportDetailEntity unit = importDetail("unit", "logisticsWeightUnit", "物流商重量单位");
        unit.setDefaultValue("kg");
        List<CfgLogisticsCostImportDetailEntity> details = Arrays.asList(amount, unit);
        List<String> headList = new ArrayList<>(Arrays.asList("金额", "匹配结果", "错误信息"));
        Map<Integer, String> headMap = new HashMap<>();
        headMap.put(0, "金额");
        headMap.put(1, "匹配结果");
        headMap.put(2, "错误信息");
        JSONObject rowData = new JSONObject();
        rowData.set("0", "USD 88");
        rowData.set("2", "");
        setPrepared(service, rowData, amount, "88");
        setPrepared(service, rowData, unit, "kg");

        invokePrivate(service, "prepareCleanFileHeaders", new Class[]{List.class, List.class, Map.class},
                details, headList, headMap);
        invokePrivate(service, "prepareCleanFileHeaders", new Class[]{List.class, List.class, Map.class},
                details, headList, headMap);
        invokePrivate(service, "projectCleanFileRows", new Class[]{List.class, List.class},
                details, Collections.singletonList(rowData));

        assertEquals(Arrays.asList("金额", "物流商重量单位", "匹配结果", "错误信息"), headList);
        assertEquals("88", rowData.getStr("0"));
        assertEquals("kg", rowData.getStr("1"));
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
    public void weightFieldShouldConvertGramToKilogramOnlyForConfiguredTargetFields() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity unit = importDetail("unit", "logisticsWeightUnit", "物流商重量单位");
        CfgLogisticsCostImportDetailEntity billingWeight = importDetail("billingWeight", "billingWeightLogistics", "计费重(物流商)");
        CfgLogisticsCostImportDetailEntity actualWeight = importDetail("actualWeight", "thirdActualWeight", "实重(物流商)");
        CfgLogisticsCostImportDetailEntity amount = importDetail("amount", "feeAmount", "费用");
        List<CfgLogisticsCostImportDetailEntity> details = Arrays.asList(unit, billingWeight, actualWeight, amount);
        JSONObject rowData = new JSONObject();
        setPrepared(service, rowData, unit, "g");
        setPrepared(service, rowData, billingWeight, "1500");
        setPrepared(service, rowData, actualWeight, "800");
        setPrepared(service, rowData, amount, "300");

        List<JSONObject> validRows = Collections.singletonList(rowData);
        invokePrivate(service, "standardizeImportRowWeightValues",
                new Class[]{List.class, List.class},
                details, validRows);
        assertEquals(1, validRows.size());
        assertEquals("kg", getPrepared(service, rowData, unit));
        assertEquals("1.5", getPrepared(service, rowData, billingWeight));
        assertEquals("0.8", getPrepared(service, rowData, actualWeight));
        assertEquals("300", getPrepared(service, rowData, amount));
    }

    @Test
    public void parseOptionalDecimalFieldShouldRejectInvalidWeightFormat() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        List<String> errorMsgList = new ArrayList<>();

        BigDecimal valid = (BigDecimal) invokePrivate(service, "parseOptionalDecimalField",
                new Class[]{String.class, String.class, List.class},
                "1,000.5", "计费重", errorMsgList);
        assertBigDecimalEquals(new BigDecimal("1000.5"), valid);
        assertTrue(errorMsgList.isEmpty());

        BigDecimal blank = (BigDecimal) invokePrivate(service, "parseOptionalDecimalField",
                new Class[]{String.class, String.class, List.class},
                "  ", "计费重", errorMsgList);
        assertNull(blank);

        BigDecimal invalid = (BigDecimal) invokePrivate(service, "parseOptionalDecimalField",
                new Class[]{String.class, String.class, List.class},
                "1000kg", "计费重", errorMsgList);
        assertNull(invalid);
        assertEquals(Collections.singletonList("计费重格式不正确"), errorMsgList);
    }

    @Test
    public void platformCodeGroupKeyShouldUseMatchedLogisticsBillSet() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity platformCode = importDetail("platformCode", "platformCode", "平台订单号");
        platformCode.setIsUniqueKey(true);
        JSONObject firstRow = new JSONObject();
        JSONObject secondRow = new JSONObject();
        setPrepared(service, firstRow, platformCode, "6200001");
        setPrepared(service, secondRow, platformCode, "6200002");
        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult = new ImportHistoryRecordDTO.PreQueryResultDTO(
                Collections.singletonList(billVo("B1", "D1", "6200001,6200002", null, null)),
                Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());

        String firstGroupKey = (String) invokePrivate(service, "buildImportRowGroupKey",
                new Class[]{JSONObject.class, List.class, ImportHistoryRecordDTO.PreQueryResultDTO.class, CfgLogisticsCostImportEntity.class},
                firstRow, Collections.singletonList(platformCode), preQueryResult, new CfgLogisticsCostImportEntity());
        String secondGroupKey = (String) invokePrivate(service, "buildImportRowGroupKey",
                new Class[]{JSONObject.class, List.class, ImportHistoryRecordDTO.PreQueryResultDTO.class, CfgLogisticsCostImportEntity.class},
                secondRow, Collections.singletonList(platformCode), preQueryResult, new CfgLogisticsCostImportEntity());

        assertEquals(firstGroupKey, secondGroupKey);
        assertTrue(firstGroupKey.contains("D1"));
    }

    @Test
    public void nonPlatformGroupShouldResolveMultipleBillsForAllocation() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        CfgLogisticsCostImportDetailEntity trackNo = importDetail("trackNo", "trackNo", "物流单号");
        trackNo.setIsUniqueKey(true);
        JSONObject firstRow = new JSONObject();
        JSONObject secondRow = new JSONObject();
        setPrepared(service, firstRow, trackNo, "TN001");
        setPrepared(service, secondRow, trackNo, "TN001");
        ImportHistoryRecordDTO.PreQueryResultDTO preQueryResult = new ImportHistoryRecordDTO.PreQueryResultDTO(
                Arrays.asList(
                        billVo("B1", "D1", null, "TN001", null),
                        billVo("B2", "D2", null, "TN001", null)
                ),
                Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        List<String> errorMsgList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<LogisticsBillDTO.LogisticsBillVo> matchedList = (List<LogisticsBillDTO.LogisticsBillVo>) invokePrivate(service,
                "resolveGroupMatchedLogisticsBillVoList",
                new Class[]{List.class, List.class, ImportHistoryRecordDTO.PreQueryResultDTO.class, CfgLogisticsCostImportEntity.class, List.class},
                Collections.singletonList(trackNo), Arrays.asList(firstRow, secondRow), preQueryResult, new CfgLogisticsCostImportEntity(), errorMsgList);

        assertTrue(errorMsgList.isEmpty());
        assertEquals(2, matchedList.size());
    }

    @Test
    public void mergeTmsCostDetailShouldSumSameCurrencyOnly() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();

        @SuppressWarnings("unchecked")
        List<TmsCostDetailDTO.UpdateDTO> mergeList = (List<TmsCostDetailDTO.UpdateDTO>) invokePrivate(service, "mergeTmsCostDetail",
                new Class[]{List.class},
                Arrays.asList(costDetail("C1", "actual", "USD", "1.25"),
                        costDetail("C1", "actual", "USD", "2.75"),
                        costDetail("C1", "actual", "CNY", "5")));

        assertEquals(2, mergeList.size());
        assertBigDecimalEquals(new BigDecimal("4.00"), findCostValue(mergeList, "USD"));
        assertBigDecimalEquals(new BigDecimal("5"), findCostValue(mergeList, "CNY"));
    }

    @Test
    public void buildOrderWeightMapShouldCollectErrorsWhenPreloadMissingAnyBill() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        Map<String, BigDecimal> preQueryOrderWeightMap = new HashMap<>();
        preQueryOrderWeightMap.put("D2", new BigDecimal("10"));
        Map<String, List<String>> preQueryOrderWeightErrorMap = new HashMap<>();
        preQueryOrderWeightErrorMap.put("D1", Collections.singletonList("出库单CK001存在SKU无法获取重量用于分摊：SKU-A"));
        List<String> errorMsgList = new ArrayList<>();
        LogisticsBillDTO.LogisticsBillVo bill1 = billVo("B1", "D1", null, null, null);
        bill1.setOutstockId("OS1");
        bill1.setOutstockCode("CK001");
        LogisticsBillDTO.LogisticsBillVo bill2 = billVo("B2", "D2", null, null, null);
        bill2.setOutstockId("OS2");
        bill2.setOutstockCode("CK002");

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> weightMap = (Map<String, BigDecimal>) invokePrivate(service, "buildOrderWeightMap",
                new Class[]{List.class, List.class, Map.class, Map.class},
                Arrays.asList(bill1, bill2),
                errorMsgList, preQueryOrderWeightMap, preQueryOrderWeightErrorMap);

        assertFalse(errorMsgList.isEmpty());
        assertEquals(1, weightMap.size());
        assertBigDecimalEquals(new BigDecimal("10"), weightMap.get("D2"));
    }

    @Test
    public void allocateCostDetailMapShouldSplitByOrderWeight() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        Map<String, BigDecimal> weightMap = new HashMap<>();
        weightMap.put("D1", new BigDecimal("1"));
        weightMap.put("D2", new BigDecimal("3"));
        List<String> errorMsgList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Map<String, List<TmsCostDetailDTO.UpdateDTO>> allocateMap = (Map<String, List<TmsCostDetailDTO.UpdateDTO>>) invokePrivate(service,
                "allocateCostDetailMap",
                new Class[]{List.class, List.class, Map.class, List.class},
                Collections.singletonList(costDetail("C1", "actual", "CNY", "100")),
                Arrays.asList(billVo("B1", "D1", null, null, null), billVo("B2", "D2", null, null, null)),
                weightMap, errorMsgList);

        assertTrue(errorMsgList.isEmpty());
        assertBigDecimalEquals(new BigDecimal("25"), allocateMap.get("D1").get(0).getCostValue());
        assertBigDecimalEquals(new BigDecimal("75"), allocateMap.get("D2").get(0).getCostValue());
    }

    @Test
    public void allocateValueByWeightShouldSplitWithLastBillAbsorbingRemainder() throws Exception {
        ImportHistoryRecordServiceImpl service = new ImportHistoryRecordServiceImpl();
        Map<String, BigDecimal> weightMap = new HashMap<>();
        weightMap.put("D1", new BigDecimal("1"));
        weightMap.put("D2", new BigDecimal("3"));
        List<LogisticsBillDTO.LogisticsBillVo> billGroup = Arrays.asList(
                billVo("B1", "D1", null, null, null),
                billVo("B2", "D2", null, null, null));
        BigDecimal totalValue = new BigDecimal("2000");

        BigDecimal first = (BigDecimal) invokePrivate(service, "allocateValueByWeight",
                new Class[]{BigDecimal.class, List.class, Map.class, int.class, BigDecimal.class},
                totalValue, billGroup, weightMap, 0, BigDecimal.ZERO);
        BigDecimal second = (BigDecimal) invokePrivate(service, "allocateValueByWeight",
                new Class[]{BigDecimal.class, List.class, Map.class, int.class, BigDecimal.class},
                totalValue, billGroup, weightMap, 1, first);

        assertBigDecimalEquals(new BigDecimal("500"), first);
        assertBigDecimalEquals(new BigDecimal("1500"), second);
        assertBigDecimalEquals(totalValue, first.add(second));
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
                new Class[]{CfgLogisticsCostImportDetailDTO.UpdateDTO.class, CfgLogisticsCostImportFieldEntity.class, int.class, java.util.Set.class},
                detail, field, index, Collections.emptySet());
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

    private LogisticsBillDTO.LogisticsBillVo billVoWithSupplier(String id, String detailId, String logisticsSupplierId) {
        LogisticsBillDTO.LogisticsBillVo billVo = new LogisticsBillDTO.LogisticsBillVo();
        billVo.setId(id);
        billVo.setDetailId(detailId);
        billVo.setLogisticsSupplierId(logisticsSupplierId);
        return billVo;
    }

    private CfgLogisticsCostImportEntity costImportConfig(String identifyType, String cfgType, String dictPlatform) {
        CfgLogisticsCostImportEntity entity = new CfgLogisticsCostImportEntity();
        entity.setIdentifyType(identifyType);
        entity.setCfgType(cfgType);
        entity.setDictPlatform(dictPlatform);
        return entity;
    }

    private LogisticsBillCostEntity billCost(String id, String detailId, String payType, String reconciliationStatus, String reconciliationMonth) {
        LogisticsBillCostEntity entity = new LogisticsBillCostEntity();
        entity.setId(id);
        entity.setLogisticsBillDetailId(detailId);
        entity.setPayType(payType);
        entity.setReconciliationStatus(reconciliationStatus);
        entity.setReconciliationMonth(reconciliationMonth);
        return entity;
    }

    private LogisticsBillDTO.LogisticsBillVo billVo(String id, String detailId, String platformCode, String trackNo, String sourceCode) {
        LogisticsBillDTO.LogisticsBillVo billVo = new LogisticsBillDTO.LogisticsBillVo();
        billVo.setId(id);
        billVo.setDetailId(detailId);
        billVo.setPlatformCode(platformCode);
        billVo.setTrackNo(trackNo);
        billVo.setSourceCode(sourceCode);
        return billVo;
    }

    private TmsCostDetailDTO.UpdateDTO costDetail(String cfgCostId, String type, String currency, String costValue) {
        TmsCostDetailDTO.UpdateDTO updateDTO = new TmsCostDetailDTO.UpdateDTO();
        updateDTO.setCfgCostId(cfgCostId);
        updateDTO.setType(type);
        updateDTO.setCurrency(currency);
        updateDTO.setCostValue(new BigDecimal(costValue));
        return updateDTO;
    }

    private BigDecimal findCostValue(List<TmsCostDetailDTO.UpdateDTO> mergeList, String currency) {
        return mergeList.stream()
                .filter(updateDTO -> currency.equals(updateDTO.getCurrency()))
                .map(TmsCostDetailDTO.UpdateDTO::getCostValue)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
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
