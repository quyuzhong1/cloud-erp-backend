package com.erp.server.tms.handler.asynctask;

import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.support.LogisticsReconMatchFailReasonSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogisticsReconMatchBatchPushHandlerRetryTest {

    private LogisticsReconMatchBatchPushHandler handler;
    private TmsAsyncTaskDetailService detailService;

    @Before
    public void setUp() {
        handler = new LogisticsReconMatchBatchPushHandler();
        detailService = Mockito.mock(TmsAsyncTaskDetailService.class);
        ReflectionTestUtils.setField(handler, "tmsAsyncTaskDetailService", detailService);
    }

    @Test
    public void failedOnlyRetryShouldExcludeNonRetryablePartialCommitDetails() {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = new TmsAsyncTaskRecordDTO.TaskEnvelopeDTO();
        envelope.setRetryMode(TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY);
        envelope.setRetrySourceTaskId("source-task");
        when(detailService.listRetryableFailedBusinessIdsByCursor(
                "source-task", "sub-10", 20, LogisticsReconMatchFailReasonSupport.NON_RETRYABLE_PREFIX))
                .thenReturn(Arrays.asList("sub-11", "sub-12"));

        List<String> result = handler.pageBatchIds(
                "retry-task", envelope,
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO("main-1", false),
                "sub-10", 20, new TmsAsyncTaskRecordEntity());

        assertEquals(Arrays.asList("sub-11", "sub-12"), result);
        verify(detailService).listRetryableFailedBusinessIdsByCursor(
                "source-task", "sub-10", 20, LogisticsReconMatchFailReasonSupport.NON_RETRYABLE_PREFIX);
    }
}
