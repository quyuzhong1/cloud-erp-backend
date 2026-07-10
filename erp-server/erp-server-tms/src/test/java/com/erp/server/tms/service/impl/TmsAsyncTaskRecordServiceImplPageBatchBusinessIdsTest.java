package com.erp.server.tms.service.impl;

import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.support.TmsAsyncTaskBatchIdPages;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TmsAsyncTaskRecordServiceImplPageBatchBusinessIdsTest {

    private TmsAsyncTaskRecordServiceImpl service;
    private TmsAsyncTaskDetailService detailService;

    @Before
    public void setUp() {
        service = new TmsAsyncTaskRecordServiceImpl();
        detailService = Mockito.mock(TmsAsyncTaskDetailService.class);
        ReflectionTestUtils.setField(service, "tmsAsyncTaskDetailService", detailService);
    }

    @Test
    public void failedOnlyUsesDetailCursor() {
        when(detailService.listFailedBusinessIdsByCursor(eq("source-1"), eq("id-1"), eq(10)))
            .thenReturn(Arrays.asList("a", "b"));

        List<String> result = service.pageBatchBusinessIds(
            com.erp.model.tms.dto.TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY,
            "source-1",
            "id-1",
            10,
            (lastId, batchSize) -> Collections.singletonList("ignored"),
            (lastId, batchSize) -> Collections.singletonList("ignored"));

        assertEquals(Arrays.asList("a", "b"), result);
    }

    @Test
    public void selectedProviderHasPriorityOverDefault() {
        BatchBusinessIdProvider defaultProvider = (lastId, batchSize) -> Collections.singletonList("default");
        BatchBusinessIdProvider selectedProvider = (lastId, batchSize) -> Collections.singletonList("selected");

        List<String> result = service.pageBatchBusinessIds(
            null, null, "", 10, defaultProvider, selectedProvider);

        assertEquals(Collections.singletonList("selected"), result);
    }

    @Test
    public void defaultProviderUsedWhenNoSelectedProvider() {
        BatchBusinessIdProvider defaultProvider = (lastId, batchSize) -> Arrays.asList("x", "y");

        List<String> result = service.pageBatchBusinessIds(
            null, null, "", 10, defaultProvider, null);

        assertEquals(Arrays.asList("x", "y"), result);
    }

    @Test
    public void selectedIdProviderPagesDistinctIds() {
        BatchBusinessIdProvider selectedProvider = TmsAsyncTaskBatchIdPages.selectedIdProvider(
            Arrays.asList("1", "2", "3", "4"));

        assertEquals(Arrays.asList("1", "2"), selectedProvider.page("", 2));
        assertEquals(Arrays.asList("3", "4"), selectedProvider.page("2", 2));
        assertTrue(selectedProvider.page("4", 2).isEmpty());
    }
}
