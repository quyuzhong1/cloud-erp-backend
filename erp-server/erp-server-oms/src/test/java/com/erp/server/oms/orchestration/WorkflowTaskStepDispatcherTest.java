package com.erp.server.oms.orchestration;

import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskInstanceStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import com.erp.server.oms.service.WorkflowTaskInstanceService;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

/**
 * 单步调度器核心流程单测（Mock 依赖，验证实例解析与链式推进）。
 */
public class WorkflowTaskStepDispatcherTest {

    private WorkflowTaskStepDispatcher dispatcher;
    private WorkflowTaskRecordService workflowTaskRecordService;
    private WorkflowTaskInstanceService workflowTaskInstanceService;
    private CrossServiceStepInvoker crossServiceStepInvoker;
    private MQProducerService mqProducerService;

    @Before
    public void setUp() throws Exception {
        dispatcher = new WorkflowTaskStepDispatcher();
        workflowTaskRecordService = Mockito.mock(WorkflowTaskRecordService.class);
        workflowTaskInstanceService = Mockito.mock(WorkflowTaskInstanceService.class);
        crossServiceStepInvoker = Mockito.mock(CrossServiceStepInvoker.class);
        mqProducerService = Mockito.mock(MQProducerService.class);

        inject("workflowTaskRecordService", workflowTaskRecordService);
        inject("workflowTaskInstanceService", workflowTaskInstanceService);
        inject("crossServiceStepInvoker", crossServiceStepInvoker);
        inject("mqProducerService", mqProducerService);

        SendResult sendResult = new SendResult();
        sendResult.setSendStatus(SendStatus.SEND_OK);
        Mockito.when(mqProducerService.syncClassMsgWithDelayLevel(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(sendResult);
    }

    @Test
    public void dispatchSkipsTerminalSuccessInstance() {
        WorkflowTaskInstanceEntity instance = buildInstance("inst-1", WorkflowTaskInstanceStatusEnum.SUCCESS.getCode());
        Mockito.when(workflowTaskInstanceService.getById("inst-1")).thenReturn(instance);

        WorkflowTaskRecordDTO.AddTaskDTO mqDTO = buildMqDto();
        mqDTO.setInstanceId("inst-1");

        dispatcher.dispatch(mqDTO);

        Mockito.verify(crossServiceStepInvoker, Mockito.never())
                .invoke(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(mqProducerService, Mockito.never())
                .syncClassMsgWithDelayLevel(Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void dispatchChainsToNextStepWhenCurrentAlreadySuccess() {
        WorkflowTaskInstanceEntity instance = buildInstance("inst-2", WorkflowTaskInstanceStatusEnum.RUNNING.getCode());
        Mockito.when(workflowTaskInstanceService.getById("inst-2")).thenReturn(instance);

        WorkflowTaskRecordEntity step0 = buildStep("s0", 0, WorkflowTaskRecordStatusEnum.SUCCESS.getCode());
        WorkflowTaskRecordEntity step1 = buildStep("s1", 1, WorkflowTaskRecordStatusEnum.PENDING.getCode());
        step0.setInstanceId("inst-2");
        step1.setInstanceId("inst-2");

        mockLambdaQuerySteps(Arrays.asList(step0, step1));

        WorkflowTaskRecordDTO.AddTaskDTO mqDTO = buildMqDto();
        mqDTO.setInstanceId("inst-2");
        mqDTO.setTargetIndex(0);

        dispatcher.dispatch(mqDTO);

        ArgumentCaptor<WorkflowTaskRecordDTO.AddTaskDTO> captor = ArgumentCaptor.forClass(WorkflowTaskRecordDTO.AddTaskDTO.class);
        Mockito.verify(mqProducerService).syncClassMsgWithDelayLevel(
                Mockito.anyString(), Mockito.anyString(), captor.capture(), Mockito.anyString(), Mockito.anyInt());
        Assert.assertEquals("inst-2", captor.getValue().getInstanceId());
        Assert.assertEquals(Integer.valueOf(1), captor.getValue().getTargetIndex());
        Mockito.verify(workflowTaskInstanceService).markRunning("inst-2", 1, 2);
    }

    @Test
    public void dispatchDoesNotFallbackWhenInstanceIdInvalid() {
        Mockito.when(workflowTaskInstanceService.getById("missing")).thenReturn(null);

        WorkflowTaskRecordDTO.AddTaskDTO mqDTO = buildMqDto();
        mqDTO.setInstanceId("missing");

        dispatcher.dispatch(mqDTO);

        Mockito.verify(workflowTaskInstanceService, Mockito.never())
                .getLatestBySource(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(crossServiceStepInvoker, Mockito.never())
                .invoke(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void dispatchExecutesPendingStepAndChainsOnSuccess() {
        WorkflowTaskInstanceEntity instance = buildInstance("inst-3", WorkflowTaskInstanceStatusEnum.RUNNING.getCode());
        Mockito.when(workflowTaskInstanceService.getById("inst-3")).thenReturn(instance);

        WorkflowTaskRecordEntity step0 = buildStep("s0", 0, WorkflowTaskRecordStatusEnum.PENDING.getCode());
        WorkflowTaskRecordEntity step1 = buildStep("s1", 1, WorkflowTaskRecordStatusEnum.PENDING.getCode());
        step0.setInstanceId("inst-3");
        step0.setInputData("{\"k\":\"v\"}");
        step1.setInstanceId("inst-3");

        mockLambdaQuerySteps(Arrays.asList(step0, step1));

        Mockito.when(crossServiceStepInvoker.invoke(Mockito.eq(step0), Mockito.isNull(), Mockito.eq(false)))
                .thenReturn(StepInvokeResult.success("{\"out\":1}"));

        WorkflowTaskRecordDTO.AddTaskDTO mqDTO = buildMqDto();
        mqDTO.setInstanceId("inst-3");
        mqDTO.setTargetIndex(0);

        dispatcher.dispatch(mqDTO);

        Mockito.verify(crossServiceStepInvoker).invoke(Mockito.eq(step0), Mockito.isNull(), Mockito.eq(false));
        ArgumentCaptor<WorkflowTaskRecordDTO.AddTaskDTO> captor = ArgumentCaptor.forClass(WorkflowTaskRecordDTO.AddTaskDTO.class);
        Mockito.verify(mqProducerService).syncClassMsgWithDelayLevel(
                Mockito.anyString(), Mockito.anyString(), captor.capture(), Mockito.anyString(), Mockito.anyInt());
        Assert.assertEquals(Integer.valueOf(1), captor.getValue().getTargetIndex());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockLambdaQuerySteps(java.util.List<WorkflowTaskRecordEntity> steps) {
        com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper chain =
                Mockito.mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
        Mockito.when(workflowTaskRecordService.lambdaQuery()).thenReturn(chain);
        Mockito.when(chain.eq(Mockito.any(), Mockito.any())).thenReturn(chain);
        Mockito.doReturn(chain).when(chain).orderByAsc((com.baomidou.mybatisplus.core.toolkit.support.SFunction) Mockito.any());
        Mockito.when(chain.list()).thenReturn(steps);
    }

    private WorkflowTaskRecordDTO.AddTaskDTO buildMqDto() {
        WorkflowTaskRecordDTO.AddTaskDTO dto = new WorkflowTaskRecordDTO.AddTaskDTO();
        dto.setSourceId("source-1");
        dto.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.EXHIBITION_ORDER_APPROVE);
        return dto;
    }

    private WorkflowTaskInstanceEntity buildInstance(String id, String status) {
        WorkflowTaskInstanceEntity entity = new WorkflowTaskInstanceEntity();
        entity.setId(id);
        entity.setSourceId("source-1");
        entity.setSourceType(WorkflowTaskRecordTypeEnum.EXHIBITION_ORDER_APPROVE.getCode());
        entity.setStatus(status);
        entity.setCurrentIndex(0);
        entity.setTotalSteps(2);
        entity.setIsDeleted(false);
        return entity;
    }

    private WorkflowTaskRecordEntity buildStep(String id, int index, String status) {
        WorkflowTaskRecordEntity entity = new WorkflowTaskRecordEntity();
        entity.setId(id);
        entity.setIndex(index);
        entity.setStatus(status);
        entity.setSourceId("source-1");
        entity.setSourceType(WorkflowTaskRecordTypeEnum.EXHIBITION_ORDER_APPROVE.getCode());
        entity.setClassPath("com.example.Foo#bar");
        entity.setIsDeleted(false);
        return entity;
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = WorkflowTaskStepDispatcher.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(dispatcher, value);
    }
}
