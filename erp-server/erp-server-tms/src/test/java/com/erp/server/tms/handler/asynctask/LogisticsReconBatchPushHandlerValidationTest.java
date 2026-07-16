package com.erp.server.tms.handler.asynctask;

import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogisticsReconBatchPushHandlerValidationTest {

    @Test
    public void matchHandlerShouldRejectLegacyPayloadWithMultipleMainIds() {
        TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO payload =
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO();
        payload.setIds(Arrays.asList("main-A", " main-B ", "main-A"));

        LogisticsReconMatchBatchPushHandler handler = new LogisticsReconMatchBatchPushHandler();

        assertEquals("旧版任务包含多个对账单，请终止旧任务后重新提交", handler.validatePayload(payload));
    }

    @Test
    public void confirmHandlerShouldRejectLegacyPayloadWithMultipleMainIds() {
        TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO payload =
                new TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO();
        payload.setIds(Arrays.asList("main-A", "main-B"));
        payload.setReconciliationStatus("confirmed");

        LogisticsReconConfirmBillBatchPushHandler handler = new LogisticsReconConfirmBillBatchPushHandler();

        assertEquals("旧版任务包含多个对账单，请终止旧任务后重新提交", handler.validatePayload(payload));
    }

    @Test
    public void handlersShouldKeepSingleLegacyIdCompatible() {
        TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO matchPayload =
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO();
        matchPayload.setIds(Arrays.asList(" main-A ", "main-A"));

        TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO confirmPayload =
                new TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO();
        confirmPayload.setIds(Arrays.asList(" main-A ", "main-A"));
        confirmPayload.setReconciliationStatus("confirmed");

        assertNull(new LogisticsReconMatchBatchPushHandler().validatePayload(matchPayload));
        assertNull(new LogisticsReconConfirmBillBatchPushHandler().validatePayload(confirmPayload));
    }

    @Test
    public void handlersShouldPreferMainIdWhenLegacyIdsContainMultipleValues() {
        TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO matchPayload =
                new TmsAsyncTaskRecordDTO.LogisticsReconMatchPayloadDTO("main-A", false);
        matchPayload.setIds(Arrays.asList("main-A", "main-B"));

        TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO confirmPayload =
                new TmsAsyncTaskRecordDTO.LogisticsReconConfirmBillPayloadDTO(
                        "main-A", "confirmed", null);

        assertNull(new LogisticsReconMatchBatchPushHandler().validatePayload(matchPayload));
        assertNull(new LogisticsReconConfirmBillBatchPushHandler().validatePayload(confirmPayload));
    }
}
