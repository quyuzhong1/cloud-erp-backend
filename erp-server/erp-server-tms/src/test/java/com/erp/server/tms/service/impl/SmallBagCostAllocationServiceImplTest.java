package com.erp.server.tms.service.impl;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO.ListDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.mapper.SmallBagCostAllocationMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SmallBagCostAllocationServiceImplTest {

    private SmallBagCostAllocationServiceImpl service;
    private SmallBagCostAllocationMapper mapper;
    private DmpTaskFeign dmpTaskFeign;

    @Before
    public void setUp() {
        service = Mockito.spy(new SmallBagCostAllocationServiceImpl());
        mapper = Mockito.mock(SmallBagCostAllocationMapper.class);
        dmpTaskFeign = Mockito.mock(DmpTaskFeign.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ReflectionTestUtils.setField(service, "dmpTaskFeign", dmpTaskFeign);
        doNothing().when(service).handleDataPaging(anyList());
    }

    @Test
    public void pagingUsesExactCountThenReturnsDetailsInIdPageOrder() {
        SmallBagCostAllocationDTO.PagingParamDTO params = new SmallBagCostAllocationDTO.PagingParamDTO();
        PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto = pagingRequest(1, 3, params);
        when(mapper.pagingCount(same(params))).thenReturn(3L);
        when(mapper.pagingDetailIds(same(params), eq(0L), eq(3L))).thenReturn(Arrays.asList("h-3", "h-1", "h-2"));
        when(mapper.selectByDetailIds(Arrays.asList("h-3", "h-1", "h-2")))
            .thenReturn(Arrays.asList(detail("h-1"), detail("h-3"), detail("h-2")));

        PagingVO<ListDTO> result = service.paging(dto);

        assertEquals(3, result.getTotalCount());
        assertEquals(Arrays.asList("h-3", "h-1", "h-2"), detailIds(result));
        verify(mapper).pagingCount(same(params));
        verify(mapper).pagingDetailIds(same(params), eq(0L), eq(3L));
    }

    @Test
    public void pagingReturnsEmptyListWithoutIdOrDetailQueryWhenExactCountIsZero() {
        SmallBagCostAllocationDTO.PagingParamDTO params = new SmallBagCostAllocationDTO.PagingParamDTO();
        PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto = pagingRequest(2, 20, params);
        when(mapper.pagingCount(same(params))).thenReturn(0L);

        PagingVO<ListDTO> result = service.paging(dto);

        assertEquals(0, result.getTotalCount());
        assertTrue(result.getList().isEmpty());
        verify(mapper).pagingCount(same(params));
        verify(mapper, never()).pagingDetailIds(any(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
        verify(mapper, never()).selectByDetailIds(anyList());
    }

    @Test
    public void pagingCalculatesOffsetAndKeepsCountWhenDetailQueryDropsMissingIds() {
        SmallBagCostAllocationDTO.PagingParamDTO params = new SmallBagCostAllocationDTO.PagingParamDTO();
        PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto = pagingRequest(3, 50, params);
        when(mapper.pagingCount(same(params))).thenReturn(60L);
        when(mapper.pagingDetailIds(same(params), eq(100L), eq(50L))).thenReturn(Arrays.asList("h-1", "h-2"));
        when(mapper.selectByDetailIds(Arrays.asList("h-1", "h-2"))).thenReturn(Collections.singletonList(detail("h-1")));

        PagingVO<ListDTO> result = service.paging(dto);

        assertEquals(60, result.getTotalCount());
        assertEquals(Collections.singletonList("h-1"), detailIds(result));
        verify(mapper).pagingDetailIds(same(params), eq(100L), eq(50L));
    }

    @Test
    public void loadBatchRatesDeduplicatesCurrencyRequestsAndUsesBatchFeignOnce() {
        when(dmpTaskFeign.getRates(Arrays.asList(new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD"))))
            .thenReturn(Arrays.asList(new BiSettlementExchangeRateDTO.BatchRateResultDTO("2026-07-01", "USD", new BigDecimal("7.2"))));

        Map<String, BigDecimal> rateMap = service.loadBatchRates(Arrays.asList(
            rateRecord("2026-07", "USD"),
            rateRecord("2026-07", "USD"),
            rateRecord("2026-07", "CNY")
        ));

        assertEquals(new BigDecimal("7.2"), rateMap.get("2026-07-01_USD"));
        verify(dmpTaskFeign).getRates(Arrays.asList(new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD")));
        verify(dmpTaskFeign, never()).getRate(any(String.class), any(String.class));
    }

    @Test
    public void resolvePageRateThrowsWhenBatchRateIsMissing() {
        when(dmpTaskFeign.getRates(Arrays.asList(new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD"))))
            .thenReturn(Arrays.asList(new BiSettlementExchangeRateDTO.BatchRateResultDTO("2026-07-01", "USD", null)));

        Map<String, BigDecimal> rateMap = service.loadBatchRates(Collections.singletonList(rateRecord("2026-07", "USD")));

        try {
            service.resolvePageRate("2026-07", "USD", rateMap);
        } catch (ServiceException ex) {
            assertEquals("汇率为空，请维护汇率后再查询", ex.getMessage());
            assertNull(rateMap.get("2026-07-01_USD"));
            return;
        }
        throw new AssertionError("Expected ServiceException");
    }

    /**
     * 构造测试用分页请求。
     *
     * @param currPage 当前页
     * @param pageSize 每页数量
     * @param params 分页筛选参数
     * @return 分页请求 DTO
     */
    private PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> pagingRequest(int currPage,
                                                                              int pageSize,
                                                                              SmallBagCostAllocationDTO.PagingParamDTO params) {
        PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto = new PagingDTO<>();
        dto.setCurrPage(currPage);
        dto.setPageSize(pageSize);
        dto.setParams(params);
        return dto;
    }

    /**
     * 构造最小分页明细记录。
     *
     * @param detailId 明细主键
     * @return 分页记录
     */
    private ListDTO detail(String detailId) {
        ListDTO dto = new ListDTO();
        dto.setDetailId(detailId);
        return dto;
    }

    /**
     * 构造批量汇率加载测试使用的最小记录。
     *
     * @param reportDate 报告月份
     * @param unitCurrency 源币别
     * @return 分页记录
     */
    private ListDTO rateRecord(String reportDate, String unitCurrency) {
        ListDTO dto = new ListDTO();
        dto.setReportDate(reportDate);
        dto.setUnitCurrency(unitCurrency);
        return dto;
    }

    /**
     * 从分页结果中提取明细主键。
     *
     * @param result 分页响应
     * @return 明细主键列表
     */
    private List<String> detailIds(PagingVO<ListDTO> result) {
        return result.getList().stream().map(ListDTO::getDetailId).collect(java.util.stream.Collectors.toList());
    }
}
