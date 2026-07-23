package com.erp.server.tms.service.impl;

import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.service.TmsCfgCostService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class LogisticsBillCostConfirmAmountTest {

    private LogisticsBillCostServiceImpl service;
    private TmsCfgCostService tmsCfgCostService;

    @Before
    public void setUp() {
        service = new LogisticsBillCostServiceImpl();
        tmsCfgCostService = Mockito.mock(TmsCfgCostService.class);
        ReflectionTestUtils.setField(service, "tmsCfgCostService", tmsCfgCostService);
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_someCategoryAmountNotZero_returnsNull() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Arrays.asList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode()),
                costDetail(logisticsCostId, "cfg-declare", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        when(tmsCfgCostService.listByIds(Arrays.asList("cfg-shipping", "cfg-declare"))).thenReturn(Arrays.asList(
                cfgCost("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode()),
                cfgCost("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertNull(actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_allExistingCategoryAmountsZero_returnsAllZeroMessage() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Arrays.asList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode()),
                costDetail(logisticsCostId, "cfg-declare", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        when(tmsCfgCostService.listByIds(Arrays.asList("cfg-shipping", "cfg-declare"))).thenReturn(Arrays.asList(
                cfgCost("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode()),
                cfgCost("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertEquals("账单确认时当前单据费用分类实际金额不能全部等于0", actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_estimateConfirmStatus_allExistingCategoryAmountsZero_returnsAllZeroMessage() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.singletonList(
                costDetail(logisticsCostId, "cfg-declare", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ESTIMATED.getCode())
        ));
        when(tmsCfgCostService.listByIds(Collections.singletonList("cfg-declare"))).thenReturn(Collections.singletonList(
                cfgCost("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(), existingDetailMap);

        assertEquals("暂估确认时当前单据费用分类暂估金额不能全部等于0", actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_allExistingCategoriesPositive_returnsNull() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Arrays.asList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.ONE, LogisticsBillCostTypeEnum.ACTUAL.getCode()),
                costDetail(logisticsCostId, "cfg-declare", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        when(tmsCfgCostService.listByIds(Arrays.asList("cfg-shipping", "cfg-declare"))).thenReturn(Arrays.asList(
                cfgCost("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode()),
                cfgCost("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertNull(actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_noExistingCategory_returnsNull() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.emptyList());

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertNull(actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_importDetailOverridesExistingAmount_returnsAllZeroMessage() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.singletonList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        List<UpdateDTO> importList = Collections.singletonList(
                updateDetail("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode(), BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        );

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, importList,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertEquals("账单确认时当前单据费用分类实际金额不能全部等于0", actualMsg);
    }

    @Test
    public void validateImportConfirmAmountMsg_nonConfirmStatus_returnsNull() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.singletonList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.TO_BE_CONFIRM.getCode(), existingDetailMap);

        assertNull(actualMsg);
    }

    private TmsCostDetailEntity costDetail(String mainId, String cfgCostId, BigDecimal costValue, String type) {
        TmsCostDetailEntity entity = new TmsCostDetailEntity();
        entity.setMainId(mainId);
        entity.setCfgCostId(cfgCostId);
        entity.setCostValue(costValue);
        entity.setType(type);
        return entity;
    }

    private UpdateDTO updateDetail(String cfgCostId, String dictCostCategory, BigDecimal costValue, String type) {
        UpdateDTO dto = new UpdateDTO();
        dto.setCfgCostId(cfgCostId);
        dto.setDictCostCategory(dictCostCategory);
        dto.setCostValue(costValue);
        dto.setType(type);
        return dto;
    }

    private TmsCfgCostEntity cfgCost(String id, String dictCostCategory) {
        TmsCfgCostEntity entity = new TmsCfgCostEntity();
        entity.setId(id);
        entity.setDictCostCategory(dictCostCategory);
        return entity;
    }
}
