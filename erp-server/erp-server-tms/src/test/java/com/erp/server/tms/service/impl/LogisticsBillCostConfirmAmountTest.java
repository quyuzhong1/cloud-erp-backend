package com.erp.server.tms.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.dto.TmsCostDetailDTO.UpdateDTO;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.tms.enums.LogisticsBillCostTypeEnum;
import com.erp.model.tms.enums.ReconciliationStatusEnum;
import com.erp.server.tms.service.TmsCfgCostService;
import com.erp.server.tms.service.TmsCostDetailService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class LogisticsBillCostConfirmAmountTest {

    private LogisticsBillCostServiceImpl service;
    private TmsCfgCostService tmsCfgCostService;
    private TmsCostDetailService tmsCostDetailService;

    @Before
    public void setUp() {
        service = new LogisticsBillCostServiceImpl();
        tmsCfgCostService = Mockito.mock(TmsCfgCostService.class);
        tmsCostDetailService = Mockito.mock(TmsCostDetailService.class);
        ReflectionTestUtils.setField(service, "tmsCfgCostService", tmsCfgCostService);
        ReflectionTestUtils.setField(service, "tmsCostDetailService", tmsCostDetailService);
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
    public void validateImportConfirmAmountMsg_confirmedStatus_emptyCostDetails_returnsCostDetailEmptyMessage() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.emptyList());

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertEquals("费用明细为空", actualMsg);
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

    @Test
    public void validateConfirmAmount_confirmedStatus_multipleBills_prefetchesCfgCategoryOnce() {
        List<TmsCostDetailEntity> detailList = Arrays.asList(
                costDetail("cost-1", "cfg-shipping", BigDecimal.ONE, LogisticsBillCostTypeEnum.ACTUAL.getCode()),
                costDetail("cost-2", "cfg-declare", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        );
        mockCostDetailLambdaQuery(detailList);
        when(tmsCfgCostService.listByIds(Arrays.asList("cfg-shipping", "cfg-declare"))).thenReturn(Arrays.asList(
                cfgCost("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode()),
                cfgCost("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode())
        ));

        ReflectionTestUtils.invokeMethod(service, "validateConfirmAmount",
                Arrays.asList("cost-1", "cost-2"), ReconciliationStatusEnum.CONFIRMED.getCode());

        verify(tmsCfgCostService, times(1)).listByIds(Arrays.asList("cfg-shipping", "cfg-declare"));
    }

    @Test
    public void validateConfirmAmount_confirmedStatus_cfgCostMissingInBatchCache_doesNotFallbackQueryPerBill() {
        List<TmsCostDetailEntity> detailList = Arrays.asList(
                costDetail("cost-1", "cfg-shipping", BigDecimal.ONE, LogisticsBillCostTypeEnum.ACTUAL.getCode()),
                costDetail("cost-2", "cfg-declare", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        );
        mockCostDetailLambdaQuery(detailList);
        when(tmsCfgCostService.listByIds(Arrays.asList("cfg-shipping", "cfg-declare"))).thenReturn(Collections.emptyList());

        ReflectionTestUtils.invokeMethod(service, "validateConfirmAmount",
                Arrays.asList("cost-1", "cost-2"), ReconciliationStatusEnum.CONFIRMED.getCode());

        verify(tmsCfgCostService, times(1)).listByIds(Arrays.asList("cfg-shipping", "cfg-declare"));
    }

    @Test
    public void validateConfirmAmount_confirmedStatus_emptyCostDetails_throwsCostDetailEmptyException() {
        mockCostDetailLambdaQuery(Collections.emptyList());

        try {
            ReflectionTestUtils.invokeMethod(service, "validateConfirmAmount",
                    Collections.singletonList("cost-1"), ReconciliationStatusEnum.CONFIRMED.getCode());
            fail("Expected ServiceException");
        } catch (ServiceException expected) {
            assertEquals("费用明细为空", expected.getMsg());
        }
    }

    @Test
    public void validateImportConfirmAmountMsg_confirmedStatus_noBatchCache_fallsBackToCfgCostQuery() {
        String logisticsCostId = "cost-1";
        Map<String, List<TmsCostDetailEntity>> existingDetailMap = Collections.singletonMap(logisticsCostId, Collections.singletonList(
                costDetail(logisticsCostId, "cfg-shipping", BigDecimal.ONE, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        when(tmsCfgCostService.listByIds(Collections.singletonList("cfg-shipping"))).thenReturn(Collections.singletonList(
                cfgCost("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode())
        ));

        String actualMsg = service.validateImportConfirmAmountMsg(logisticsCostId, null,
                ReconciliationStatusEnum.CONFIRMED.getCode(), existingDetailMap);

        assertNull(actualMsg);
        verify(tmsCfgCostService, times(1)).listByIds(Collections.singletonList("cfg-shipping"));
    }

    @Test
    public void appendImportConfirmAmountErrors_confirmStatus_usesBatchCfgCategoryCache() {
        LogisticsBillDTO.LogisticsBillVo firstBillVo = new LogisticsBillDTO.LogisticsBillVo();
        firstBillVo.setId("bill-1");
        firstBillVo.setDetailId("detail-1");
        LogisticsBillCostEntity firstBillCost = new LogisticsBillCostEntity();
        firstBillCost.setId("cost-1");

        LogisticsBillDTO.LogisticsBillVo secondBillVo = new LogisticsBillDTO.LogisticsBillVo();
        secondBillVo.setId("bill-2");
        secondBillVo.setDetailId("detail-2");
        LogisticsBillCostEntity secondBillCost = new LogisticsBillCostEntity();
        secondBillCost.setId("cost-2");

        List<Pair<LogisticsBillDTO.LogisticsBillVo, LogisticsBillCostEntity>> targetPairList = Arrays.asList(
                new Pair<>(firstBillVo, firstBillCost),
                new Pair<>(secondBillVo, secondBillCost)
        );
        Map<String, List<UpdateDTO>> targetUpdateMap = new HashMap<>();
        targetUpdateMap.put("detail-1", Collections.singletonList(
                updateDetail("cfg-shipping", null, BigDecimal.ONE, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        targetUpdateMap.put("detail-2", Collections.singletonList(
                updateDetail("cfg-declare", "", BigDecimal.TEN, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));

        Map<String, List<TmsCostDetailEntity>> mainIdListMap = new HashMap<>();
        mainIdListMap.put("cost-1", Collections.singletonList(
                costDetail("cost-1", "cfg-shipping", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));
        mainIdListMap.put("cost-2", Collections.singletonList(
                costDetail("cost-2", "cfg-declare", BigDecimal.ZERO, LogisticsBillCostTypeEnum.ACTUAL.getCode())
        ));

        Map<String, String> cfgCategoryCache = new HashMap<>();
        cfgCategoryCache.put("cfg-shipping", DictCostCategoryEnum.SHIPPING_COST.getCode());
        cfgCategoryCache.put("cfg-declare", DictCostCategoryEnum.DECLARE_COST.getCode());

        List<String> errorMsgList = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(service, "appendImportConfirmAmountErrors",
                Boolean.TRUE, targetPairList, targetUpdateMap, mainIdListMap, cfgCategoryCache, errorMsgList);

        assertTrue(errorMsgList.isEmpty());
        verifyNoInteractions(tmsCfgCostService);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockCostDetailLambdaQuery(List<TmsCostDetailEntity> detailList) {
        LambdaQueryChainWrapper<TmsCostDetailEntity> queryWrapper = Mockito.mock(LambdaQueryChainWrapper.class);
        when(tmsCostDetailService.lambdaQuery()).thenReturn(queryWrapper);
        when(queryWrapper.in(Mockito.any(), Mockito.anyCollection())).thenReturn(queryWrapper);
        when(queryWrapper.eq(Mockito.any(), Mockito.any())).thenReturn(queryWrapper);
        when(queryWrapper.list()).thenReturn(detailList);
    }
}
