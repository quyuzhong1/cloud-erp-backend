package com.erp.server.oms.orchestration;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.WorkflowTaskNodeConfigDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import com.erp.server.oms.service.WorkflowTaskRecordService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 跨服务单步节点执行器
 */
@Slf4j
@Component
public class CrossServiceStepInvoker {

    private static final String ERROR_SOURCE_ORCHESTRATOR = "orchestrator";
    private static final String ERROR_SOURCE_REMOTE = "remote";

    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;

    /**
     * 执行单个任务节点：抢占 → Feign 调用远程节点 → 更新节点状态与耗时。
     *
     * @param entity         任务节点实体
     * @param nodeConfig     节点配置；为 null 时从 entity.classPath 解析
     * @param retryIncrement 失败时是否递增 retry_count（Job 补偿为 true，普通链式推进为 false）
     * @return 执行结果，含 SUCCESS / FAILED / WAITING / NOT_CLAIMED 等语义
     */
    public StepInvokeResult invoke(WorkflowTaskRecordEntity entity, WorkflowTaskNodeConfigDTO nodeConfig, boolean retryIncrement) {
        if (entity == null) {
            return StepInvokeResult.failed("任务节点不存在", ERROR_SOURCE_ORCHESTRATOR, 0L);
        }
        if (!workflowTaskRecordService.claimTask(entity.getId(), entity.getStatus())) {
            log.warn("任务节点已被其他消费者抢占，id={}, index={}, sourceId={}", entity.getId(), entity.getIndex(), entity.getSourceId());
            return StepInvokeResult.notClaimed();
        }
        entity.setStatus(WorkflowTaskRecordStatusEnum.PROCESSING.getCode());
        entity.setStartTime(LocalDateTime.now());
        workflowTaskRecordService.updateById(entity);
        //重新查询记录
        entity = workflowTaskRecordService.getById(entity.getId());
        String traceId = TraceContext.traceId();
        WorkflowTaskNodeConfigDTO config = nodeConfig == null ? WorkflowTaskNodeConfigParser.parse(entity.getClassPath()) : nodeConfig;
        String classPath = config.getClassPath();
        String methodName = config.getMethodName();
        if (StringUtils.isBlank(classPath) || StringUtils.isBlank(methodName)) {
            String[] legacy = splitLegacyClassPath(entity.getClassPath());
            classPath = legacy[0];
            methodName = legacy[1];
        }
        if (StringUtils.isAnyBlank(classPath, methodName)) {
            markFailed(entity, "classpath为空", retryIncrement, ERROR_SOURCE_ORCHESTRATOR, 0L);
            return StepInvokeResult.failed("classpath为空", ERROR_SOURCE_ORCHESTRATOR, 0L);
        }

        String jsonStr = entity.getInputData();
        if (StringUtils.isBlank(jsonStr)) {
            String msg = StrUtil.format("traceId: 【{}】，inputData为空", traceId);
            markFailed(entity, msg, retryIncrement, ERROR_SOURCE_ORCHESTRATOR, 0L);
            return StepInvokeResult.failed(msg, ERROR_SOURCE_ORCHESTRATOR, 0L);
        }

        Map<String, Object> inputDataMap;
        try {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            inputDataMap = gson.fromJson(jsonStr, mapType);
        } catch (Exception e) {
            String msg = StrUtil.format("traceId: 【{}】，inputData解析失败，id={}", traceId, entity.getId());
            markFailed(entity, msg, retryIncrement, ERROR_SOURCE_ORCHESTRATOR, 0L);
            return StepInvokeResult.failed(msg, ERROR_SOURCE_ORCHESTRATOR, 0L);
        }

        WorkflowTaskRecordDTO.MqRequestDTO dto = new WorkflowTaskRecordDTO.MqRequestDTO();
        dto.setData(inputDataMap);
        dto.setTaskId(entity.getId());
        dto.setSourceType(entity.getSourceType());
        dto.setIndex(entity.getIndex());
        dto.setInstanceId(entity.getInstanceId());
        dto.setTraceId(entity.getTraceId());

        long startMs = System.currentTimeMillis();
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO;
        try {
            mqResponseDTO = FeignQuery.invoke(WorkflowTaskRecordDTO.MqResponseDTO.class, classPath, methodName, Arrays.asList(dto));
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startMs;
            log.error("Feign invoke failed for workflowTaskRecordEntity id: {}", entity.getId(), e);
            String msg = StrUtil.format("traceId: 【{}】，Feign invoke failed, id={}, error={}", traceId, entity.getId(), e.getMessage());
            markFailed(entity, msg, retryIncrement, ERROR_SOURCE_REMOTE, duration);
            return StepInvokeResult.failed(msg, ERROR_SOURCE_REMOTE, duration);
        }
        long duration = System.currentTimeMillis() - startMs;

        if (Objects.isNull(mqResponseDTO)) {
            markFailed(entity, "远程返回为空", retryIncrement, ERROR_SOURCE_REMOTE, duration);
            return StepInvokeResult.failed("远程返回为空", ERROR_SOURCE_REMOTE, duration);
        }

        entity.setOutputData(JSON.toJSONString(mqResponseDTO.getData()));
        entity.setFeignDurationMs(duration);
        entity.setTargetService(StringUtils.defaultIfBlank(entity.getTargetService(), config.getServiceCode()));
        entity.setTargetEndpoint(StringUtils.defaultIfBlank(entity.getTargetEndpoint(), WorkflowTaskNodeConfigParser.buildTargetEndpoint(config)));

        String errorMsg = mqResponseDTO.getErrorMsg();
        String responseStatus = mqResponseDTO.getStatus();
        if (Objects.equals(responseStatus, WorkflowTaskRecordStatusEnum.WAITING.getCode())) {
            entity.setStatus(WorkflowTaskRecordStatusEnum.WAITING.getCode());
            entity.setLastError(StringUtils.defaultString(errorMsg));
            entity.setErrorSource(ERROR_SOURCE_REMOTE);
            entity.setEndTime(LocalDateTime.now());
            workflowTaskRecordService.updateById(entity);
            return StepInvokeResult.waiting(errorMsg);
        }
        if (Objects.equals(responseStatus, WorkflowTaskRecordStatusEnum.FAILED.getCode())) {
            markFailed(entity, StringUtils.defaultString(errorMsg), true, ERROR_SOURCE_REMOTE, duration);
            return StepInvokeResult.failed(StringUtils.defaultString(errorMsg), ERROR_SOURCE_REMOTE, duration);
        }
        if (StringUtils.isNotBlank(errorMsg)) {
            markFailed(entity, errorMsg, retryIncrement, ERROR_SOURCE_REMOTE, duration);
            return StepInvokeResult.failed(errorMsg, ERROR_SOURCE_REMOTE, duration);
        }

        entity.setStatus(WorkflowTaskRecordStatusEnum.SUCCESS.getCode());
        entity.setLastError("");
        entity.setErrorSource("");
        entity.setEndTime(LocalDateTime.now());
        entity.setRetryCount(Optional.ofNullable(entity.getRetryCount()).orElse(0) + (retryIncrement ? 1 : 0));
        workflowTaskRecordService.updateById(entity);
        return StepInvokeResult.success(entity.getOutputData());
    }

    private void markFailed(WorkflowTaskRecordEntity entity, String errorMsg, boolean retryIncrement,
                            String errorSource, long duration) {
        entity.setStatus(WorkflowTaskRecordStatusEnum.FAILED.getCode());
        entity.setLastError(errorMsg);
        entity.setRetryCount(Optional.ofNullable(entity.getRetryCount()).orElse(0) + (retryIncrement ? 1 : 0));
        entity.setErrorSource(errorSource);
        entity.setFeignDurationMs(duration);
        entity.setEndTime(LocalDateTime.now());
        workflowTaskRecordService.updateById(entity);
    }

    private String[] splitLegacyClassPath(String classPath) {
        if (StringUtils.isBlank(classPath)) {
            return new String[]{"", ""};
        }
        String[] split = classPath.split("#");
        if (split.length != 2) {
            return new String[]{"", ""};
        }
        return split;
    }
}
