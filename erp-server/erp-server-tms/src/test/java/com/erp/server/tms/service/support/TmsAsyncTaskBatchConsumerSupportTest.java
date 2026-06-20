package com.erp.server.tms.service.support;

import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.TmsAsyncTaskBatchPushHandler;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TmsAsyncTaskBatchConsumerSupportTest {

    private TmsAsyncTaskBatchConsumerSupport support;
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    private RedissonClient redissonClient;
    private RLock lock;
    private TestHandler handler;

    @Before
    public void setUp() throws Exception {
        support = new TmsAsyncTaskBatchConsumerSupport();
        asyncTaskRecordService = mock(TmsAsyncTaskRecordService.class);
        redissonClient = mock(RedissonClient.class);
        lock = mock(RLock.class);
        handler = spy(new TestHandler());
        ReflectionTestUtils.setField(support, "asyncTaskRecordService", asyncTaskRecordService);
        ReflectionTestUtils.setField(support, "redissonClient", redissonClient);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(eq(0L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    public void skipWhenTaskIdBlank() {
        support.execute(new TmsAsyncTaskRecordEntity(), handler);
        verify(asyncTaskRecordService, never()).loadBillBatchParams(anyString());
    }

    @Test
    public void skipWhenLockNotAcquired() throws Exception {
        when(lock.tryLock(eq(0L), eq(TimeUnit.SECONDS))).thenReturn(false);
        support.execute(task("task-1"), handler);
        verify(asyncTaskRecordService, never()).loadBillBatchParams(anyString());
    }

    @Test
    public void stopWhenBillBatchParamsMissing() {
        when(asyncTaskRecordService.loadBillBatchParams("task-1")).thenReturn(null);
        support.execute(task("task-1"), handler);
        verify(asyncTaskRecordService, never()).parseEnvelope(anyString());
    }

    @Test
    public void finishWhenPayloadValidationFails() {
        mockReadyTask(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        handler.validationError = "bad payload";
        support.execute(task("task-1"), handler);
        verify(asyncTaskRecordService).updateTask(eq("task-1"),
            eq(TmsAsyncTaskRecordStatusEnum.FINISH.getCode()), eq("bad payload"));
        verify(asyncTaskRecordService, never()).updateTaskFinally(anyString());
    }

    @Test
    public void claimPendingTaskAndProcessOneBatch() {
        mockReadyTask(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        when(handler.pageBatchIds(any(), any(), any(), any(), anyInt(), any()))
            .thenReturn(Arrays.asList("a", "b"), Collections.emptyList());
        when(handler.processBatch(any(), any(), any(), any(), anyList(), anyInt(), any()))
            .thenReturn(new TmsAsyncTaskRecordDTO.BatchProcessResult(2, 0));

        support.execute(task("task-1"), handler);

        verify(asyncTaskRecordService, atLeastOnce()).lambdaUpdate();
        verify(asyncTaskRecordService).updateTaskFinally("task-1");
        verify(handler).processBatch(eq("task-1"), any(), eq("payload"), any(), eq(Arrays.asList("a", "b")),
            eq(1), any());
    }

    @Test
    public void skipWhenAlreadyFinished() {
        mockReadyTask(TmsAsyncTaskRecordStatusEnum.FINISH.getCode());
        support.execute(task("task-1"), handler);
        verify(asyncTaskRecordService, never()).parseEnvelopePayloadOrFinishTask(
            anyString(), any(), any(), anyString());
    }

    @Test
    public void stopWhenPayloadParseFails() {
        mockReadyTask(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        when(asyncTaskRecordService.parseEnvelopePayloadOrFinishTask(
            eq("task-1"), any(), eq(String.class), anyString())).thenReturn(null);
        support.execute(task("task-1"), handler);
        verify(asyncTaskRecordService, never()).updateTaskFinally(anyString());
    }

    @Test
    public void useMqRecordWhenRefreshDisabled() {
        handler.refreshBeforeClaim = false;
        TmsAsyncTaskRecordEntity mqRecord = task("task-1");
        mqRecord.setStatus(TmsAsyncTaskRecordStatusEnum.ING.getCode());
        when(asyncTaskRecordService.loadBillBatchParams("task-1")).thenReturn(batchParams());
        when(asyncTaskRecordService.parseEnvelope(any())).thenReturn(envelope());
        when(asyncTaskRecordService.parseEnvelopePayloadOrFinishTask(
            eq("task-1"), any(), eq(String.class), anyString())).thenReturn("payload");
        when(asyncTaskRecordService.resolveOperatorLoginUser(any(), any(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO.class)))
            .thenReturn(new LoginUser());
        when(asyncTaskRecordService.resolveBatchSize(anyString(), anyInt())).thenReturn(100);
        when(asyncTaskRecordService.getById("task-1")).thenReturn(mqRecord);
        when(asyncTaskRecordService.shouldStopLoopTask(eq("task-1"), any())).thenReturn(false);
        when(asyncTaskRecordService.terminateTaskIfExecTimeoutReached(any(), any())).thenReturn(false);
        when(handler.pageBatchIds(any(), any(), any(), any(), anyInt(), any()))
            .thenReturn(Collections.emptyList());

        support.execute(mqRecord, handler);

        ArgumentCaptor<TmsAsyncTaskRecordEntity> captor = ArgumentCaptor.forClass(TmsAsyncTaskRecordEntity.class);
        verify(handler).pageBatchIds(any(), any(), any(), any(), anyInt(), captor.capture());
        assertEquals("task-1", captor.getValue().getId());
    }

    private void mockReadyTask(String status) {
        TmsAsyncTaskRecordEntity current = task("task-1");
        current.setStatus(status);
        current.setDetailCount(2);
        when(asyncTaskRecordService.loadBillBatchParams("task-1")).thenReturn(batchParams());
        when(asyncTaskRecordService.getById("task-1")).thenReturn(current);
        when(asyncTaskRecordService.parseEnvelope(any())).thenReturn(envelope());
        when(asyncTaskRecordService.parseEnvelopePayloadOrFinishTask(
            eq("task-1"), any(), eq(String.class), anyString())).thenReturn("payload");
        when(asyncTaskRecordService.resolveOperatorLoginUser(any(), any(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO.class)))
            .thenReturn(new LoginUser());
        LambdaUpdateChainWrapper<TmsAsyncTaskRecordEntity> updateChain = mock(LambdaUpdateChainWrapper.class);
        when(asyncTaskRecordService.lambdaUpdate()).thenReturn(updateChain);
        when(updateChain.set(any(), any())).thenReturn(updateChain);
        when(updateChain.eq(any(), any())).thenReturn(updateChain);
        when(updateChain.update()).thenReturn(true);
        when(asyncTaskRecordService.shouldStopLoopTask(eq("task-1"), any())).thenReturn(false);
        when(asyncTaskRecordService.terminateTaskIfExecTimeoutReached(any(), any())).thenReturn(false);
        when(asyncTaskRecordService.resolveBatchSize(anyString(), anyInt())).thenReturn(100);
        when(handler.pageBatchIds(any(), any(), any(), any(), anyInt(), any()))
            .thenReturn(Collections.emptyList());
    }

    private TmsAsyncTaskRecordEntity task(String id) {
        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        entity.setId(id);
        entity.setDataJson("{}");
        return entity;
    }

    private TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope() {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = new TmsAsyncTaskRecordDTO.TaskEnvelopeDTO();
        envelope.setPayloadType("test:method");
        envelope.setPayloadJson("{}");
        return envelope;
    }

    private CfgSettingValueDTO.BillBatchParamsDTO batchParams() {
        CfgSettingValueDTO.BillBatchParamsDTO params = new CfgSettingValueDTO.BillBatchParamsDTO();
        params.setBatch("100");
        params.setBatchTimeoutSeconds("60");
        return params;
    }

    private static class TestHandler implements TmsAsyncTaskBatchPushHandler<String> {
        private String validationError;
        private boolean refreshBeforeClaim = true;

        @Override
        public String taskDisplayName() {
            return "test task";
        }

        @Override
        public Class<String> payloadClass() {
            return String.class;
        }

        @Override
        public String payloadParseErrorMessage() {
            return "parse failed";
        }

        @Override
        public String validatePayload(String payload) {
            return validationError;
        }

        @Override
        public boolean refreshRecordBeforeClaim() {
            return refreshBeforeClaim;
        }

        @Override
        public List<String> pageBatchIds(String taskId, TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                         String payload, String lastId, int batchSize,
                                         TmsAsyncTaskRecordEntity taskRecord) {
            return Collections.emptyList();
        }

        @Override
        public TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                     String payload, LoginUser operatorUser,
                                                                     List<String> batchIds, int batchNumber,
                                                                     CfgSettingValueDTO.BillBatchParamsDTO billBatchParams) {
            return new TmsAsyncTaskRecordDTO.BatchProcessResult(0, 0);
        }
    }
}
