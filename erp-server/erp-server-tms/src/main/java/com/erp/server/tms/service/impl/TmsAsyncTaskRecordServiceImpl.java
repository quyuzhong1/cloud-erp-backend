package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.UserStateConstants;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SystemCodeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.DistributeKeyConstant;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import com.erp.model.tms.enums.TmsAsyncTaskMethodTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordBusinessTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordExecTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.TmsAsyncTaskRecordMapper;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 异步任务记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@Service
public class TmsAsyncTaskRecordServiceImpl extends SuperServiceImpl<TmsAsyncTaskRecordMapper, TmsAsyncTaskRecordEntity> implements TmsAsyncTaskRecordService {

    private static final JSONConfig TASK_DATA_JSON_CONFIG = JSONConfig.create().setIgnoreNullValue(true);

    private static final Integer ERROR_START = 0;
    private static final Integer ERROR_END = 1000;
    private static final int DEFAULT_WATCHDOG_MAIN_TASK_LIMIT = 100;      // 每轮默认最多扫描超时主任务数
    private static final int DEFAULT_WATCHDOG_DETAIL_LIMIT = 1000;        // 每轮默认最多扫描待清理明细数
    private static final int DEFAULT_WATCHDOG_UPDATE_BATCH_SIZE = 500;    // 明细批量标记失败默认每批条数
    private static final int DEFAULT_WATCHDOG_MAX_ROUNDS = 3;             // 每阶段默认最多循环轮数
    private static final int MAX_WATCHDOG_MAIN_TASK_LIMIT = 500;          // 主任务扫描上限硬顶，防配置过大
    private static final int MAX_WATCHDOG_DETAIL_LIMIT = 5000;            // 明细扫描上限硬顶，防配置过大
    private static final int MAX_WATCHDOG_UPDATE_BATCH_SIZE = 500;        // 批量更新上限硬顶，与 DB 分批规范一致
    private static final int MAX_WATCHDOG_MAX_ROUNDS = 10;              // 循环轮数上限硬顶，防单次 Job 过长

    @Resource
    private TmsAsyncTaskDetailService tmsAsyncTaskDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private MQProducerService mQProducerService;

    @Lazy
    @Resource
    private TmsAsyncTaskRecordService selfServer;

    /**
     * 新增手动任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "dto.businessType,dto.methodType", unlockAfterTx = true)
    public TmsAsyncTaskRecordEntity addManualTask(TmsAsyncTaskRecordDTO.ManualCreateDTO dto){
        String businessType = dto.getBusinessType();
        String methodType = dto.getMethodType();
        Integer detailCount = dto.getDetailCount();
        String compactJson = compactTaskDataJson(dto.getDataJson());
        List<TmsAsyncTaskRecordEntity> runningTasks = lambdaQuery()
            .eq(TmsAsyncTaskRecordEntity::getExecType, TmsAsyncTaskRecordExecTypeEnum.MANUAL.getCode())
            .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
            .eq(TmsAsyncTaskRecordEntity::getMethodType, methodType)
            .in(TmsAsyncTaskRecordEntity::getStatus,Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
            .list();

        //仅头程/小包费用分摊防重维度需要不同月份
        if (CollUtil.isNotEmpty(runningTasks)) {
            String reportDate = extractReportDate(compactJson);
            if (StringUtils.isBlank(reportDate)) {
                log.warn("存在进行中手动任务且无法提取核算期间，businessType: {}, methodType: {}", businessType, methodType);
                throw new ServiceException("存在进行中的异步任务，请稍后重试或联系管理员");
            }
            Optional<TmsAsyncTaskRecordEntity> periodConflict = runningTasks.stream()
                    .filter(task -> reportDate.equals(extractReportDate(task.getDataJson())))
                    .findFirst();
            if (periodConflict.isPresent()) {
                TmsAsyncTaskRecordEntity conflict = periodConflict.get();
                log.warn("手动异步任务核算期间冲突，businessType: {}, methodType: {}, reportDate: {}, 进行中任务 code: {}",
                        businessType, conflict.getMethodType(), reportDate, conflict.getCode());
                throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, reportDate);
            }
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setMethodType(methodType);
        entity.setDetailCount(detailCount);
        entity.setDataJson(compactJson);
        entity.setStartTime(LocalDateTime.now());
        entity.setExecTimeout(resolveTaskExecTimeout(loadBillBatchParams(null)));
        entity.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        entity.setExecType(TmsAsyncTaskRecordExecTypeEnum.MANUAL.getCode());
        if (!save(entity)) {
            throw new ServiceException("异步任务创建失败");
        }
        return entity;
    }

    /**
     * 持久化用任务参数 JSON：仅保留有值字段。
     */
    private String compactTaskDataJson(String json) {
        if (StringUtils.isBlank(json)) {
            return "";
        }
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = parseEnvelope(json);
        if (envelope == null) {
            throw new ServiceException("任务参数必须为 TaskEnvelope 格式");
        }
        return JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
    }

    /**
     * 获取头程/小包费用分摊日期参数（手动任务防重维度）。
     */
    private String extractReportDate(String json) {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = parseEnvelope(json);
        if (envelope == null) {
            return null;
        }
        TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO smallBagPayload =
                parseEnvelopePayload(envelope, TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO.class);
        if (smallBagPayload != null && StringUtils.isNotBlank(smallBagPayload.getReportDate())) {
            return trimToNull(smallBagPayload.getReportDate());
        }
        TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO firstMilePayload =
                parseEnvelopePayload(envelope, TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO.class);
        return firstMilePayload == null ? null : trimToNull(firstMilePayload.getReportDate());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 新增自动周期任务。
     * <p>
     * 防重键为 {@code businessType + methodType + startTimeStr}；命中已存在或保存失败时返回 {@code null}。
     * {@link TmsAsyncTaskRecordDTO.AutoCreateDTO#getExecTimeout()} 为空时回退批次配置超时。
     *
     * @param dto 自动任务创建入参
     * @return 创建成功的任务主键；跳过或失败时返回 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "dto.businessType,dto.methodType", unlockAfterTx = true)
    public String addAutoTask(TmsAsyncTaskRecordDTO.AutoCreateDTO dto){
        String businessType = dto.getBusinessType();
        String methodType = dto.getMethodType();
        String compactJson = compactTaskDataJson(dto.getDataJson());
        String startTimeStr = dto.getStartTimeStr();
        String effectiveStartTimeStr = StringUtils.isNotBlank(startTimeStr)
            ? startTimeStr
            : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (isExistAutoTask(businessType, methodType, effectiveStartTimeStr)) {
            log.warn("自动异步任务已存在，跳过创建，businessType: {}, methodType: {}, startTimeStr: {}",
                businessType, methodType, effectiveStartTimeStr);
            return null;
        }
        Integer execTimeout = dto.getExecTimeout();
        if (execTimeout == null || execTimeout <= 0) {
            execTimeout = resolveTaskExecTimeout(loadBillBatchParams(null));
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setMethodType(methodType);
        entity.setDataJson(compactJson);
        //startTimeStr转时间戳LocalDateTime
        LocalDateTime startTime = null;
        if (StringUtils.isNotBlank(startTimeStr)) {
            // 将 yyyy-MM-dd 格式的字符串转换为 LocalDate
            LocalDate localDate = LocalDate.parse(startTimeStr);
            // 转换为 LocalDateTime，时分秒默认为 00:00:00
            startTime = localDate.atStartOfDay();
        } else {
            // 如果 startTimeStr 为空，则使用当前时间
            startTime = LocalDateTime.now();
        }
        entity.setStartTime(startTime);
        entity.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        entity.setExecType(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode());
        entity.setExecTimeout(execTimeout);
        return save(entity) ? entity.getId() : null;
    }

    @Override
    public void updateTask(String taskId,String status, String errorMsg) {
        // 主表不允许有failed状态，统一改为finish，但必须记录errorData
        if (TmsAsyncTaskRecordStatusEnum.FAILED.getCode().equals(status)) {
            status = TmsAsyncTaskRecordStatusEnum.FINISH.getCode();
        }
        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, status)
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, errorMsg)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    @Override
    public void finishTaskWithError(String taskId, String errorMsg) {
        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, StringUtils.substring(errorMsg, ERROR_START, ERROR_END))
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    @Override
    public CfgSettingValueDTO.BillBatchParamsDTO loadBillBatchParams(String taskId) {
        CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.BILL_BATCH_PARAMS.getCode());
        if (byKey == null || byKey.getDataJson() == null) {
            log.error("配置项 {} 不存在或 dataJson 为空，taskId: {}",
                CfgSettingEnum.BILL_BATCH_PARAMS.getCode(), taskId);
            if (StringUtils.isNotBlank(taskId)) {
                finishTaskWithError(taskId, "批次配置缺失");
            }
            return null;
        }
        return JSONUtil.toBean(byKey.getDataJson(), CfgSettingValueDTO.BillBatchParamsDTO.class);
    }

    @Override
    public int resolveBatchSize(String batchConfig, int defaultSize) {
        int batchSize = NumberUtils.toInt(batchConfig, defaultSize);
        return batchSize <= 0 ? defaultSize : batchSize;
    }

    @Override
    public int resolveTimeoutSeconds(String timeoutConfig, int defaultSeconds) {
        int timeoutSeconds = NumberUtils.toInt(timeoutConfig, defaultSeconds);
        return timeoutSeconds <= 0 ? defaultSeconds : timeoutSeconds;
    }

    /**
     * 从批次配置解析主任务超时时间。
     * <p>
     * 未配置或配置非法时使用 28800 秒，保持任务有明确止损时间。
     *
     * @param billBatchParamsDTO 批次配置
     * @return 主任务超时时间，单位：秒
     */
    @Override
    public int resolveTaskExecTimeout(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        return resolveTimeoutSeconds(billBatchParamsDTO == null ? null : billBatchParamsDTO.getTaskTimeoutSeconds(), 28800);
    }

    /**
     * 小包明细僵死窗口 = 批次等待超时时间 + 明细缓冲时间。
     * <p>
     * 缓冲时间用于覆盖线程刚超过批次等待时间但仍可能正常收尾的情况，避免过早标记失败。
     *
     * @param billBatchParamsDTO 批次配置
     * @return 小包明细僵死判定窗口，单位：秒
     */
    @Override
    public int resolveStaleDetailSeconds(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        int timeoutSeconds = resolveTimeoutSeconds(billBatchParamsDTO == null ? null : billBatchParamsDTO.getBatchTimeoutSeconds(), 5000);
        int bufferSeconds = resolveTimeoutSeconds(billBatchParamsDTO == null ? null : billBatchParamsDTO.getDetailBufferSeconds(), 600);
        return timeoutSeconds + bufferSeconds;
    }

    /**
     * 判断明细是否为僵死 ING。
     * <p>
     * 只依赖认领时刷新的 startTime，不使用 updateTime，避免被其他更新动作干扰判断。
     *
     * @param detail 任务明细
     * @param staleBefore 僵死阈值时间
     * @return true 表示该 ING 明细已超过执行窗口
     */
    @Override
    public boolean isStaleIngDetail(TmsAsyncTaskDetailEntity detail, LocalDateTime staleBefore) {
        return detail != null
                && staleBefore != null
                && Objects.equals(detail.getStatus(), TmsAsyncTaskRecordStatusEnum.ING.getCode())
                && detail.getStartTime() != null
                && !detail.getStartTime().isAfter(staleBefore);
    }

    @Override
    public String formatTaskErrorMessage(Exception e) {
        String message = e == null ? null : e.getMessage();
        return StringUtils.substring(Objects.toString(message, e == null ? "未知错误" : e.getClass().getSimpleName()), ERROR_START, ERROR_END);
    }


    /**
     * 构建业务载荷类型。
     * <p>
     * 当前按 businessType:methodType 约定生成，消费者可据此识别已迁移任务的 payload 类型。
     *
     * @param businessType 业务类型
     * @param methodType 方法类型
     * @return 载荷类型
     */
    @Override
    public String buildPayloadType(String businessType, String methodType) {
        return StringUtils.defaultString(businessType) + ":" + StringUtils.defaultString(methodType);
    }

    /**
     * 构建 TMS 异步任务信封。
     * <p>
     * 信封保存调度、重试和载荷路由信息，业务参数会序列化到 payloadJson 中。
     *
     * @param businessType 业务类型
     * @param methodType 方法类型
     * @param retryMode 重试模式
     * @param retrySourceTaskId 来源任务 ID
     * @param payload 业务载荷对象
     * @return 任务信封
     */
    @Override
    public TmsAsyncTaskRecordDTO.TaskEnvelopeDTO buildEnvelope(String businessType, String methodType,
                                                               String retryMode, String retrySourceTaskId, Object payload) {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = new TmsAsyncTaskRecordDTO.TaskEnvelopeDTO();
        envelope.setBusinessType(StringUtils.trimToNull(businessType));
        envelope.setMethodType(StringUtils.trimToNull(methodType));
        envelope.setRetryMode(StringUtils.trimToNull(retryMode));
        envelope.setRetrySourceTaskId(StringUtils.trimToNull(retrySourceTaskId));
        envelope.setPayloadType(buildPayloadType(businessType, methodType));
        envelope.setPayloadVersion(1);
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        if (loginUser != null) {
            envelope.setOperatorUserId(StringUtils.trimToNull(loginUser.getUid()));
            envelope.setOperatorUserName(StringUtils.trimToNull(loginUser.getUserName()));
        }
        envelope.setPayloadJson(payload == null ? "{}" : JSONUtil.toJsonStr(payload, TASK_DATA_JSON_CONFIG));
        return envelope;
    }

    /**
     * 解析任务信封。
     *
     * @param dataJson 任务参数 JSON
     * @return 任务信封；非信封或解析失败时返回 null
     */
    @Override
    public TmsAsyncTaskRecordDTO.TaskEnvelopeDTO parseEnvelope(String dataJson) {
        if (StringUtils.isBlank(dataJson)) {
            return null;
        }
        try {
            TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = JSONUtil.toBean(dataJson, TmsAsyncTaskRecordDTO.TaskEnvelopeDTO.class);
            if (envelope == null || StringUtils.isBlank(envelope.getPayloadType()) || StringUtils.isBlank(envelope.getPayloadJson())) {
                return null;
            }
            return envelope;
        } catch (Exception e) {
            log.debug("任务参数解析 TaskEnvelopeDTO 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析任务信封中的业务载荷。
     * <p>
     * 解析失败时返回 null，由业务消费者记录确定性的任务失败原因。
     *
     * @param envelope 任务信封
     * @param payloadClass 载荷类型
     * @param <T> 载荷泛型
     * @return 业务载荷；解析失败时返回 null
     */
    @Override
    public <T> T parseEnvelopePayload(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope, Class<T> payloadClass) {
        if (envelope == null || StringUtils.isBlank(envelope.getPayloadJson()) || payloadClass == null) {
            return null;
        }
        try {
            return JSONUtil.toBean(envelope.getPayloadJson(), payloadClass);
        } catch (Exception e) {
            log.warn("任务信封载荷解析失败，payloadType: {}, error: {}", envelope.getPayloadType(), e.getMessage());
            return null;
        }
    }

    /**
     * 解析任务信封中的业务载荷，解析失败时统一记录任务失败原因。
     */
    @Override
    public <T> T parseEnvelopePayloadOrFinishTask(String taskId, TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                  Class<T> payloadClass, String errorMsg) {
        T payload = parseEnvelopePayload(envelope, payloadClass);
        if (payload != null) {
            return payload;
        }
        if (StringUtils.isNotBlank(taskId)) {
            finishTaskWithError(taskId, errorMsg);
        }
        return null;
    }

    @Override
    @Deprecated
    public LoginUser resolveOperatorLoginUser(TmsAsyncTaskRecordEntity taskRecord, TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope) {
        if (envelope != null && StringUtils.isNotBlank(envelope.getOperatorUserId())) {
            LoginUser loginUser = new LoginUser();
            loginUser.setUid(envelope.getOperatorUserId().trim());
            loginUser.setUserName(StringUtils.defaultIfBlank(StringUtils.trimToNull(envelope.getOperatorUserName()),
                UserStateConstants.USER_SYSTEM));
            return loginUser;
        }
        TmsAsyncTaskRecordEntity operatorTask = taskRecord;
        if (envelope != null && StringUtils.isNotBlank(envelope.getRetrySourceTaskId())) {
            TmsAsyncTaskRecordEntity sourceTask = getById(envelope.getRetrySourceTaskId());
            if (sourceTask != null) {
                operatorTask = sourceTask;
            }
        }
        if (operatorTask != null && StringUtils.isNotBlank(operatorTask.getCreateUserId())) {
            LoginUser loginUser = new LoginUser();
            loginUser.setUid(operatorTask.getCreateUserId());
            loginUser.setUserName(StringUtils.defaultIfBlank(operatorTask.getCreateUserName(), UserStateConstants.USER_SYSTEM));
            return loginUser;
        }
        return UserContext.getDefaultLoginUser();
    }

    @Override
    public BatchResultDTO dispatchManualEnvelopeTask(String businessType,
                                                     String methodType,
                                                     int detailCount,
                                                     TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                     String dispatchSuccessLogTemplate) {
        String jsonStr = JSONUtil.toJsonStr(envelope);
        TmsAsyncTaskRecordEntity taskRecord = selfServer.addManualTask(
            new TmsAsyncTaskRecordDTO.ManualCreateDTO(businessType, methodType, detailCount, jsonStr));
        claimAndDispatch(taskRecord, true);
        if (StringUtils.isNotBlank(dispatchSuccessLogTemplate)) {
            log.info(dispatchSuccessLogTemplate, taskRecord.getId(), detailCount);
        }
        return BatchResultDTO.success(taskRecord.getId(), taskRecord.getCode());
    }

    @Override
    public List<String> pageBatchBusinessIds(String retryMode,
                                             String retrySourceTaskId,
                                             String lastId,
                                             int batchSize,
                                             BatchBusinessIdProvider defaultProvider,
                                             BatchBusinessIdProvider selectedIdProvider) {
        if (TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY.equals(retryMode)) {
            return tmsAsyncTaskDetailService.listFailedBusinessIdsByCursor(retrySourceTaskId, lastId, batchSize);
        }
        if (selectedIdProvider != null) {
            return selectedIdProvider.page(lastId, batchSize);
        }
        if (defaultProvider == null) {
            return Collections.emptyList();
        }
        return defaultProvider.page(lastId, batchSize);
    }

    @Override
    public boolean shouldStopLoopTask(String taskId, TmsAsyncTaskRecordEntity currentTask) {
        if (currentTask == null) {
            log.error("循环中任务记录已消失，终止处理，taskId: {}", taskId);
            return true;
        }
        if (Objects.equals(currentTask.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
            log.warn("循环过程中，任务状态显示已完成，taskId: {}", taskId);
            return true;
        }
        return false;
    }

    /**
     * 分批循环内的轻量主任务超时检查。
     * <p>
     * watchdog 仍作为最终兜底；业务循环可复用该方法在低频任务刷新点及时止损。
     */
    @Override
    public boolean terminateTaskIfExecTimeoutReached(TmsAsyncTaskRecordEntity currentTask,
                                                     CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        if (currentTask == null || currentTask.getStartTime() == null) {
            return false;
        }
        int execTimeout = currentTask.getExecTimeout() == null
                ? resolveTaskExecTimeout(billBatchParamsDTO)
                : currentTask.getExecTimeout();
        if (execTimeout <= 0) {
            return false;
        }
        LocalDateTime timeoutAt = currentTask.getStartTime().plusSeconds(execTimeout);
        if (LocalDateTime.now().isBefore(timeoutAt)) {
            return false;
        }
        terminateTaskTimeout(currentTask.getId(), "任务执行超时");
        log.warn("异步任务执行超时，已终止任务，taskId: {}, execTimeout: {}", currentTask.getId(), execTimeout);
        return true;
    }

    /**
     * 任务超时中止
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void terminateTaskTimeout(String taskId, String errorMsg) {
        List<TmsAsyncTaskDetailEntity> detailEntityList = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).list();
        int detailCount = detailEntityList.size();
        long finishCount = detailEntityList.stream().filter(e -> e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.FINISH.getCode())).count();
        tmsAsyncTaskDetailService.lambdaUpdate()
                .set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData, errorMsg)
                .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
                .ne(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .update();

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus,  TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, errorMsg)
                .set(TmsAsyncTaskRecordEntity::getDetailCount, detailCount)
                .set(TmsAsyncTaskRecordEntity::getErrorCount, detailCount - finishCount)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    @Override
    public List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        TmsAsyncTaskRecordDTO.PagingParamDTO searchParam = new TmsAsyncTaskRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TmsAsyncTaskRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        // 获取状态列表
        List<String> statusList = TmsAsyncTaskRecordStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(TmsAsyncTaskRecordDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.stream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new TmsAsyncTaskRecordDTO.TabListDTO(status, "", 0));
            }
        });

        list.stream().forEach(e ->{
            e.setTabFlagName(TmsAsyncTaskRecordStatusEnum.getName(e.getTabFlag()));
        });

        // 修改为按照 TmsAsyncTaskRecordStatusEnum 枚举声明顺序排序
        list.sort(Comparator.comparingInt(tabDto -> {
            TmsAsyncTaskRecordStatusEnum statusEnum = TmsAsyncTaskRecordStatusEnum.getByCode(tabDto.getTabFlag());
            return statusEnum != null ? statusEnum.ordinal() : Integer.MAX_VALUE;
        }));

        // 在列表开头添加"全部"统计
        list.add(0, new TmsAsyncTaskRecordDTO.TabListDTO("all","全部", 0));
        return list;
    }

    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsAsyncTaskRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<TmsAsyncTaskRecordDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        for (TmsAsyncTaskRecordDTO.ListDTO data : list) {
            data.setSysModuleName(SystemCodeEnum.getName(data.getSysModule()));

            data.setBusinessTypeName(TmsAsyncTaskRecordBusinessTypeEnum.getName(data.getBusinessType()));
            data.setStatusName(TmsAsyncTaskRecordStatusEnum.getName(data.getStatus()));
            data.setExecTypeName(TmsAsyncTaskRecordExecTypeEnum.getName(data.getExecType()));
            data.setMethodTypeName(TmsAsyncTaskMethodTypeEnum.getName(data.getMethodType()));
        }
    }


    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        String id = pagingParamDTO.getParams().getId();
        IPage<TmsAsyncTaskRecordDTO.DetailListDTO> pageData = this.baseMapper.pagingError(query, id);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("异步任务导出", EXPORT_TMS_ASYNC_TASK_RECORD.getCode(), param);
    }


    @Override
    public void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("异步任务错误导出", EXPORT_TMS_ASYNC_TASK_DETAIL.getCode(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto) {
        TmsAsyncTaskRecordEntity entity = getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("异步任务记录不存在");
        }
        //时间戳转LocalDateTime
        LocalDateTime startTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dto.getStartTime()),
                ZoneId.systemDefault()
        );

        //判断dto的startTime是否比entity的startTime大
        if (entity.getStartTime().isAfter(startTime)) {
            throw new ServiceException("不能早于当前的任务执行时间");
        }

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStartTime, startTime)
                .eq(TmsAsyncTaskRecordEntity::getId, dto.getId())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "entity.businessType,entity.methodType", unlockAfterTx = true)
    public BatchResultDTO retry(TmsAsyncTaskRecordEntity entity) {
        //数据校验
        checkData(entity);

        Integer execTimeout = resolveAutoTaskExecTimeout(entity);
        String retryDataJson = buildFullRetryDataJson(entity);

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setRetryTimes(entity.getRetryTimes() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(retryDataJson);
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setMethodType(entity.getMethodType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        newTask.setExecTimeout(execTimeout);
        newTask.setExecType(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode());
        boolean save = save(newTask);
        if(!save){
            throw new ServiceException("保存失败");
        }
        //表示任务已重试过
        entity.setIsRetry(Boolean.TRUE);
        updateById(entity);
        return BatchResultDTO.success(newTask.getId(), newTask.getCode(), OperationTypeEnum.ADD);
    }

    private void checkData(TmsAsyncTaskRecordEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("异步任务记录不存在");
        }

        Boolean isRetry = entity.getIsRetry();
        if(Boolean.TRUE.equals(isRetry)){
            throw new ServiceException("已有重试任务，无法再次重试");
        }

        Integer retryTimes = entity.getRetryTimes();
        if(null == retryTimes || retryTimes <= 0){
            throw new ServiceException("已超过最大重试次数");
        }

        String status = entity.getStatus();
        if(!Objects.equals(status, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())){
            throw new ServiceException("任务未完成不支持重新创建任务重试");
        }

        Integer count = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, entity.getId()).eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode()).count();
        if(Objects.isNull(count) || count <= 0){
            throw new ServiceException("无错误数量不支持重新创建任务重试");
        }

        String execType = entity.getExecType();
        if(StringUtils.isBlank(execType) || !Objects.equals(execType, TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode())){
            throw new ServiceException("仅支持执行类型为自动的任务重试");
        }
        String dataJson = entity.getDataJson();
        if(StringUtils.isBlank(dataJson) || Objects.equals(dataJson,"{}")){
            throw new ServiceException("dataJson为空，无法重新创建任务重试");
        }
    }

    /**
     * 校验失败明细重试的来源任务。
     * <p>
     * 失败明细重试支持手动和自动来源任务，但仍要求来源任务已完成、未创建过重试任务、
     * 存在失败明细且 dataJson 可用于构建新的重试任务。
     *
     * @param entity 来源任务
     */
    private void checkErrorRetryData(TmsAsyncTaskRecordEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("异步任务记录不存在");
        }
        if (Boolean.TRUE.equals(entity.getIsRetry())) {
            throw new ServiceException("已有重试任务，无法再次重试");
        }
        Integer retryTimes = entity.getRetryTimes();
        if (retryTimes == null || retryTimes <= 0) {
            throw new ServiceException("已超过最大重试次数");
        }
        if (!Objects.equals(entity.getStatus(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode())) {
            throw new ServiceException("任务未完成不支持错误重试");
        }
        Integer count = tmsAsyncTaskDetailService.lambdaQuery()
                .eq(TmsAsyncTaskDetailEntity::getMainId, entity.getId())
                .eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .count();
        if (count == null || count <= 0) {
            throw new ServiceException("无错误数量不支持错误重试");
        }
        if (StringUtils.isBlank(entity.getDataJson()) || Objects.equals(entity.getDataJson(), "{}")) {
            throw new ServiceException("dataJson为空，无法错误重试");
        }
    }

    /**
     * 构建完整重试任务的 dataJson。
     * <p>
     * 完整重试保留原业务载荷，但必须清理 taskId 和失败明细重试元数据，
     * 避免新的自动重试任务误走 FAILED_ONLY 游标消费逻辑。
     *
     * @param entity 来源任务
     * @return 新完整重试任务持久化的 dataJson
     */
    private String buildFullRetryDataJson(TmsAsyncTaskRecordEntity entity) {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = parseEnvelope(entity.getDataJson());
        if (envelope == null) {
            throw new ServiceException("dataJson解析失败，无法重新创建任务重试");
        }
        envelope.setRetryMode(null);
        envelope.setRetrySourceTaskId(null);
        return JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
    }

    /**
     * 构建失败明细重试任务的 dataJson。
     * <p>
     * 失败明细重试只保存来源任务 ID 和 FAILED_ONLY 模式，消费时按来源任务失败明细游标分页，
     * 不把失败业务 ID 批量写入任务参数。
     *
     * @param entity 来源任务
     * @return 新失败明细重试任务持久化的 dataJson
     */
    private String buildFailedOnlyRetryDataJson(TmsAsyncTaskRecordEntity entity) {
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = parseEnvelope(entity.getDataJson());
        if (envelope == null) {
            throw new ServiceException("dataJson解析失败，无法错误重试");
        }
        envelope.setRetrySourceTaskId(entity.getId());
        envelope.setRetryMode(TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY);
        if (StringUtils.isBlank(envelope.getBusinessType())) {
            envelope.setBusinessType(entity.getBusinessType());
        }
        if (StringUtils.isBlank(envelope.getMethodType())) {
            envelope.setMethodType(entity.getMethodType());
        }
        return JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
    }

    /**
     * 创建失败明细重试任务并在事务提交后立即派发。
     * <p>
     * 任务创建通过 {@link #createFailedOnlyRetryTask(TmsAsyncTaskRecordEntity)} 走代理事务，
     * MQ 派发在该方法返回后执行，避免消费者先于事务提交读取不到新任务。
     *
     * @param entity 来源任务
     * @return 批量操作结果
     */
    @Override
    public BatchResultDTO errorRetry(TmsAsyncTaskRecordEntity entity) {
        TmsAsyncTaskRecordEntity newTask = selfServer.createFailedOnlyRetryTask(entity);
        // createFailedOnlyRetryTask 通过代理完成事务提交后再派发 MQ，避免消费者先于任务提交查询不到记录。
        claimAndDispatch(newTask, true);
        return BatchResultDTO.success(newTask.getId(), newTask.getCode(), OperationTypeEnum.ADD);
    }

    /**
     * 事务内创建失败明细重试任务。
     * <p>
     * 该方法只负责持久化新任务和标记来源任务已重试，不发送 MQ。
     * 调用方应在事务提交后再派发，避免消息消费和数据库提交之间产生竞态。
     *
     * @param entity 来源任务
     * @return 已保存的新重试任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "entity.businessType,entity.methodType", unlockAfterTx = true)
    public TmsAsyncTaskRecordEntity createFailedOnlyRetryTask(TmsAsyncTaskRecordEntity entity) {
        checkErrorRetryData(entity);
        Integer execTimeout = resolveTaskExecTimeout(loadBillBatchParams(null));
        String retryDataJson = buildFailedOnlyRetryDataJson(entity);

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setRetryTimes(entity.getRetryTimes() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(retryDataJson);
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setMethodType(entity.getMethodType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        newTask.setExecTimeout(execTimeout);
        newTask.setExecType(TmsAsyncTaskRecordExecTypeEnum.MANUAL.getCode());
        boolean save = save(newTask);
        if(!save){
            throw new ServiceException("保存失败");
        }
        //表示任务已重试过
        entity.setIsRetry(Boolean.TRUE);
        updateById(entity);
        return newTask;
    }

    @Override
    public Boolean isExist(String businessType, String methodType, String startTimeStr) {
        return isExistAutoTask(businessType, methodType, startTimeStr);
    }

    private Boolean isExistAutoTask(String businessType, String methodType, String startTimeStr) {
        LocalDateTime[] timeRange = resolveStartTimeDayRange(startTimeStr);
        return lambdaQuery()
            .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
            .eq(TmsAsyncTaskRecordEntity::getMethodType, methodType)
            .eq(TmsAsyncTaskRecordEntity::getExecType, TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode())
            .ge(TmsAsyncTaskRecordEntity::getStartTime, timeRange[0])
            .lt(TmsAsyncTaskRecordEntity::getStartTime, timeRange[1])
            .count() > 0;
    }

    private LocalDateTime[] resolveStartTimeDayRange(String startTimeStr) {
        LocalDateTime startTime = DateUtil.parse(startTimeStr).toLocalDateTime();
        LocalDateTime endTime = startTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        return new LocalDateTime[]{startTime, endTime};
    }

    /**
     * 小包自动任务去重：除当前 methodType 外，仍识别历史 {@code PUSH_ALLOCATION} 任务。
     */
    private Boolean isExistSmallBagPushAutoTask(String businessType, String methodType, String startTimeStr) {
        return isExistAutoTask(businessType, methodType, startTimeStr)
            || isExistAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), startTimeStr);
    }

    /**
     * 校验并返回自动任务生成日（1-31）；非法配置直接抛业务异常，由 {@link #tryGenAutoTask} 捕获记入汇总。
     *
     * @param configuredDay 配置中的生成日
     * @param taskName      业务展示名，用于异常文案
     * @param fieldName     配置项展示名，用于异常文案
     * @return 合法生成日
     */
    private int resolveAutoTaskDay(Integer configuredDay, String taskName, String fieldName) {
        if (configuredDay == null || configuredDay < 1 || configuredDay > 31) {
            throw new ServiceException(taskName + fieldName + "配置非法，必须在1-31之间");
        }
        return configuredDay;
    }

    /**
     * 完整重试保持自动周期任务语义，超时时间从自动任务配置来源解析。
     */
    private Integer resolveAutoTaskExecTimeout(TmsAsyncTaskRecordEntity entity) {
        CfgSettingValueDTO.ReconciliationCycleDTO cycleDTO = loadReconciliationCycle();
        Integer configuredTimeout = resolveAutoTaskConfiguredTimeout(entity, cycleDTO);
        if (configuredTimeout != null && configuredTimeout > 0) {
            return configuredTimeout;
        }
        if (entity != null && entity.getExecTimeout() != null && entity.getExecTimeout() > 0) {
            return entity.getExecTimeout();
        }
        return resolveTaskExecTimeout(loadBillBatchParams(null));
    }

    private CfgSettingValueDTO.ReconciliationCycleDTO loadReconciliationCycle() {
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if (cfgSettingEntity == null || cfgSettingEntity.getDataJson() == null) {
            log.warn("自动任务生成配置缺失，完整重试将回退来源任务超时时间");
            return null;
        }
        return JSONUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingValueDTO.ReconciliationCycleDTO.class);
    }

    private Integer resolveAutoTaskConfiguredTimeout(TmsAsyncTaskRecordEntity entity, CfgSettingValueDTO.ReconciliationCycleDTO cycleDTO) {
        if (entity == null || cycleDTO == null || StringUtils.isBlank(entity.getBusinessType())) {
            return null;
        }
        String businessType = entity.getBusinessType();
        if (Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())) {
            return cycleDTO.getFirstMileExecTimeout();
        }
        if (Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())) {
            return cycleDTO.getDeclareExecTimeout();
        }
        if (Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())) {
            return cycleDTO.getFirstMileAllocationeExecTimeout();
        }
        if (Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())) {
            return cycleDTO.getPackageBeginExecTimeout();
        }
        if (Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())) {
            return cycleDTO.getTransferBeginExecTimeout();
        }
        return null;
    }


    /**
     * 正常完整收尾：主表保持三态，任务级异常通过 errorData 表达，不能被这里覆盖。
     */
    @Override
    public void updateTaskFinally(String taskId) {
        Integer errorCount = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode()).count();
        Integer detailCount = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).count();
        TmsAsyncTaskRecordEntity currentTask = getById(taskId);
        boolean clearProgressMsg = shouldClearTaskProgressMsg(currentTask);

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(clearProgressMsg, TmsAsyncTaskRecordEntity::getErrorData, "")
                .set(TmsAsyncTaskRecordEntity::getErrorCount,errorCount)
                .set(TmsAsyncTaskRecordEntity::getDetailCount,detailCount)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    /**
     * 正常收尾只清理派发/处理中进度文案，保留超时、查询失败等任务级异常原因。
     */
    private boolean shouldClearTaskProgressMsg(TmsAsyncTaskRecordEntity currentTask) {
        if (currentTask == null || StringUtils.isBlank(currentTask.getErrorData())) {
            return true;
        }
        String errorData = currentTask.getErrorData();
        return ApiError.COMMON_BATCH_PROCESSING.getMsg().equals(errorData);
    }

    /**
     * 统一处理任务详情失败状态更新
     */
    @Override
    public void updateTaskDetailFailure(String taskDetailId, Exception e) {
        //把Exception e 转字符串
        String errorMsg = ExceptionUtils.getStackTrace(e);
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "未知错误";
        }
        // 限制错误信息长度，避免数据库字段超限
        tmsAsyncTaskDetailService.updateDetail(taskDetailId,
                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                StringUtils.substring(errorMsg, ERROR_START, ERROR_END));
    }

    /**
     * 启动到期的自动周期任务。
     * 超时终止和孤儿明细清理由 watchdogTask 统一处理。
     */
    @Override
    public void startTask(){
        LocalDateTime now = LocalDateTime.now();
        // 只派发到期自动待执行任务，手动任务和清理职责不在周期派发器处理。
        List<TmsAsyncTaskRecordEntity> list = lambdaQuery()
                .eq(TmsAsyncTaskRecordEntity::getExecType, TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode())
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .le(TmsAsyncTaskRecordEntity::getStartTime, now)
                .list();
        if(CollUtil.isEmpty(list)){
            log.info("TmsAsyncTaskRecord不存在到期自动待执行任务");
            return;
        }

        for (TmsAsyncTaskRecordEntity entity : list) {
            try {
                claimAndDispatch(entity, false);
            } catch (Exception e) {
                log.error("自动异步任务派发异常，taskId: {}", entity.getId(), e);
                selfServer.updateTask(entity.getId(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), formatTaskErrorMessage(e));
            }
        }
    }

    /**
     * 异步任务 watchdog 兜底清理入口。
     * <p>
     * 消费端内循环已有 {@link #terminateTaskIfExecTimeoutReached} 等轻量检查，watchdog 是进程崩溃、
     * MQ 重投异常等场景下的有界 fallback，单次执行受 limit + maxRounds 约束，避免占满调度线程。
     * <p>
     * 明细清理统一标记 FAILED，不回退 PENDING，防止僵死明细被重复消费。
     */
    @Override
    public void watchdogTask() {
        CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO = loadBillBatchParams(null);
        TmsAsyncTaskRecordDTO.WatchdogRunConfig runConfig = resolveWatchdogRunConfig(billBatchParamsDTO);
        //阶段一：终止超时 ING 主任务。
        terminateTimedOutIngTasks(runConfig, billBatchParamsDTO);
        //阶段二：清理孤儿 ING 明细（主任务已 FINISH，明细仍 ING）。
        markFinishedTaskIngDetailsFailed(runConfig);
        markConfiguredStaleIngDetailsFailed(runConfig, billBatchParamsDTO);
    }

    /**
     * 阶段一：终止超时 ING 主任务。
     * <p>
     * 超时判定与 {@link #terminateTaskIfExecTimeoutReached} 一致：优先 task.execTimeout，
     * 为空时取 BILL_BATCH_PARAMS.taskTimeoutSeconds（默认 28800 秒）。
     * 配置缺失时仍可用默认超时执行，不阻断本阶段。
     */
    private void terminateTimedOutIngTasks(TmsAsyncTaskRecordDTO.WatchdogRunConfig runConfig, CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        LocalDateTime now = LocalDateTime.now();
        int defaultExecTimeout = resolveTaskExecTimeout(billBatchParamsDTO);
        for (int round = 0; round < runConfig.getMaxRounds(); round++) {
            List<TmsAsyncTaskRecordEntity> timedOutTasks =
                    baseMapper.listTimedOutTasksForWatchdog(defaultExecTimeout, now, runConfig.getMainTaskLimit());
            if (CollUtil.isEmpty(timedOutTasks)) {
                return;
            }
            for (TmsAsyncTaskRecordEntity entity : timedOutTasks) {
                selfServer.terminateTaskTimeout(entity.getId(), "任务执行超时");
            }
            if (timedOutTasks.size() < runConfig.getMainTaskLimit()) {
                return;
            }
        }
    }

    /**
     * 阶段二：清理孤儿 ING 明细（主任务已 FINISH，明细仍 ING）。
     * <p>
     * 典型场景：主任务异常收尾或进程中断，明细状态未同步。仅更新明细，不再动主任务。
     */
    private void markFinishedTaskIngDetailsFailed(TmsAsyncTaskRecordDTO.WatchdogRunConfig runConfig) {
        for (int round = 0; round < runConfig.getMaxRounds(); round++) {
            List<String> detailIds = tmsAsyncTaskDetailService.listOrphanIngDetailIds(runConfig.getDetailLimit());
            if (CollUtil.isEmpty(detailIds)) {
                return;
            }
            int failedCount = markDetailsFailedInBatches(
                    detailIds, "主任务已结束，明细仍为执行中，系统自动标记失败", runConfig.getUpdateBatchSize());
            if (failedCount > 0) {
                log.warn("已清理主任务结束后的ING明细，明细数量: {}", failedCount);
            }
            if (detailIds.size() < runConfig.getDetailLimit() || failedCount <= 0) {
                return;
            }
        }
    }

    /**
     * 阶段三：按 businessType + methodType 清理僵死 ING 明细。
     * <p>
     * 仅对已迁移至 envelope 的批次任务类型生效（见 {@link #staleDetailCleanupTaskTypes()}），
     * 僵死窗口 = batchTimeoutSeconds + detailBufferSeconds，与消费端 {@link #isStaleIngDetail} 一致。
     * BILL_BATCH_PARAMS 缺失时跳过——僵死窗口无法计算，且不影响阶段一/二的默认兜底。
     */
    private void markConfiguredStaleIngDetailsFailed(TmsAsyncTaskRecordDTO.WatchdogRunConfig runConfig, CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        if (billBatchParamsDTO == null) {
            return;
        }
        LocalDateTime staleBefore = LocalDateTime.now().minusSeconds(resolveStaleDetailSeconds(billBatchParamsDTO));
        for (TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType taskType : staleDetailCleanupTaskTypes()) {
            for (int round = 0; round < runConfig.getMaxRounds(); round++) {
                List<String> detailIds = tmsAsyncTaskDetailService.listStaleIngDetailIdsByTaskType(
                        taskType.getBusinessType(), taskType.getMethodType(), staleBefore, runConfig.getDetailLimit());
                if (CollUtil.isEmpty(detailIds)) {
                    break;
                }
                int failedCount = markDetailsFailedInBatches(
                        detailIds, ApiError.ASYNC_TASK_DETAIL_TIMEOUT.getMsg(), runConfig.getUpdateBatchSize());
                if (failedCount > 0) {
                    log.warn("已清理僵死ING明细，businessType: {}, methodType: {}, 明细数量: {}",
                            taskType.getBusinessType(), taskType.getMethodType(), failedCount);
                }
                if (detailIds.size() < runConfig.getDetailLimit() || failedCount <= 0) {
                    break;
                }
            }
        }
    }

    /** 明细批量标记失败，单批不超过 500 条，控制单次 UPDATE 行数。 */
    private int markDetailsFailedInBatches(List<String> detailIds, String errorMsg, int batchSize) {
        if (CollUtil.isEmpty(detailIds)) {
            return 0;
        }
        int failedCount = 0;
        for (int i = 0; i < detailIds.size(); i += batchSize) {
            List<String> batch = detailIds.subList(i, Math.min(i + batchSize, detailIds.size()));
            failedCount += tmsAsyncTaskDetailService.markDetailsFailed(batch, errorMsg);
        }
        return failedCount;
    }

    /** 解析 watchdog 扫描/更新上限，配置非法或缺失时用默认值并 clamp 到硬上限。 */
    private TmsAsyncTaskRecordDTO.WatchdogRunConfig resolveWatchdogRunConfig(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO) {
        int mainTaskLimit = boundedWatchdogValue(
                billBatchParamsDTO == null ? null : billBatchParamsDTO.getWatchdogMainTaskLimit(),
                DEFAULT_WATCHDOG_MAIN_TASK_LIMIT, MAX_WATCHDOG_MAIN_TASK_LIMIT);
        int detailLimit = boundedWatchdogValue(
                billBatchParamsDTO == null ? null : billBatchParamsDTO.getWatchdogDetailLimit(),
                DEFAULT_WATCHDOG_DETAIL_LIMIT, MAX_WATCHDOG_DETAIL_LIMIT);
        int updateBatchSize = boundedWatchdogValue(
                billBatchParamsDTO == null ? null : billBatchParamsDTO.getWatchdogUpdateBatchSize(),
                DEFAULT_WATCHDOG_UPDATE_BATCH_SIZE, MAX_WATCHDOG_UPDATE_BATCH_SIZE);
        int maxRounds = boundedWatchdogValue(
                billBatchParamsDTO == null ? null : billBatchParamsDTO.getWatchdogMaxRounds(),
                DEFAULT_WATCHDOG_MAX_ROUNDS, MAX_WATCHDOG_MAX_ROUNDS);
        return new TmsAsyncTaskRecordDTO.WatchdogRunConfig(mainTaskLimit, detailLimit, updateBatchSize, maxRounds);
    }

    private int boundedWatchdogValue(String configuredValue, int defaultValue, int maxValue) {
        int resolved = resolveBatchSize(configuredValue, defaultValue);
        return Math.min(resolved, maxValue);
    }

    /**
     * 启用僵死明细 watchdog 清理的任务类型白名单。
     * <p>
     * 新增迁移任务类型时需在此注册 businessType + methodType，不可仅按 businessType 全量开启。
     */
    private List<TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType> staleDetailCleanupTaskTypes() {
        String smallBagBusinessType = TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
        String firstMileBusinessType = TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode();
        String transferDeclareBusinessType = TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode();
        String firstMileReconBusinessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode();
        String b2cDeclareBusinessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode();
        return Arrays.asList(
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.SELFDELIVER_PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.LASTMILE_PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.SELFDELIVER_UPDATE_RECONCILIATION_STATUS.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(smallBagBusinessType, TmsAsyncTaskMethodTypeEnum.LASTMILE_UPDATE_RECONCILIATION_STATUS.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(firstMileBusinessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(firstMileBusinessType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(firstMileBusinessType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(firstMileBusinessType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(transferDeclareBusinessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(transferDeclareBusinessType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(transferDeclareBusinessType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(transferDeclareBusinessType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(firstMileReconBusinessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode()),
                new TmsAsyncTaskRecordDTO.WatchdogStaleDetailTaskType(b2cDeclareBusinessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())
        );
    }

    /**
     * 认领待执行任务并发送 MQ。
     * <p>
     * 手动即时派发失败时会抛出业务异常并透传给调用方；自动周期派发失败时回退为 PENDING。
     *
     * @param entity 待派发任务
     * @param manualImmediate 是否为用户触发的即时派发
     */
    @Override
    public void claimAndDispatch(TmsAsyncTaskRecordEntity entity, boolean manualImmediate) {
        String dataJson = entity.getDataJson();
        if (StringUtils.isBlank(dataJson) || Objects.equals(dataJson, "{}")) {
            selfServer.updateTask(entity.getId(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "dataJson为空直接结束任务");
            if (manualImmediate) {
                throw new ServiceException("dataJson为空，无法派发任务");
            }
            return;
        }

        Object dispatchPayload = buildDispatchPayload(entity);
        if (dispatchPayload == null) {
            selfServer.updateTask(entity.getId(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "任务参数解析失败");
            if (manualImmediate) {
                throw new ServiceException("任务参数解析失败，无法派发任务");
            }
            return;
        }
        LocalDateTime dispatchTime = LocalDateTime.now();
        boolean claimed = lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .set(TmsAsyncTaskRecordEntity::getErrorData, ApiError.COMMON_BATCH_PROCESSING.getMsg())
                .set(TmsAsyncTaskRecordEntity::getStartTime, dispatchTime)
                .eq(TmsAsyncTaskRecordEntity::getId, entity.getId())
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .update();
        if (!claimed) {
            log.warn("异步任务已被其他调度认领，taskId: {}", entity.getId());
            if (manualImmediate) {
                throw new ServiceException("异步任务已被其他调度认领，无法重复派发");
            }
            return;
        }
        if (dispatchPayload instanceof TmsAsyncTaskRecordEntity) {
            TmsAsyncTaskRecordEntity dispatchTask = (TmsAsyncTaskRecordEntity) dispatchPayload;
            dispatchTask.setStatus(TmsAsyncTaskRecordStatusEnum.ING.getCode());
            dispatchTask.setErrorData(ApiError.COMMON_BATCH_PROCESSING.getMsg());
            dispatchTask.setStartTime(dispatchTime);
        }
        SendResult sendResult;
        try {
            sendResult = mQProducerService.syncClassMsg(
                    RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC,
                    RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG,
                    dispatchPayload,
                    entity.getId());
        } catch (Exception e) {
            log.error("消息发送异常，taskId: {}", entity.getId(), e);
            handleDispatchFailure(entity.getId(), StringUtils.substring(e.getMessage(), ERROR_START, ERROR_END), manualImmediate);
            return;
        }
        if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            log.error("消息发送结果失败：{}", JSONUtil.toJsonStr(sendResult));
            handleDispatchFailure(entity.getId(), "MQ消息发送失败", manualImmediate);
        } else {
            log.info("MQ数据结果：{}", JSONUtil.toJsonStr(sendResult));
        }
    }

    /**
     * 构建实际发送到 MQ 的消息体。
     * <p>
     * 派发消息统一发送任务实体；消费者根据任务 ID、businessType、methodType 路由，
     * 并从实体 dataJson 解析 envelope payload。
     *
     * @param entity 待派发任务
     * @return MQ 消息体；信封解析失败时返回 null
     */
    private Object buildDispatchPayload(TmsAsyncTaskRecordEntity entity) {
        if (parseEnvelope(entity.getDataJson()) == null) {
            return null;
        }
        return entity;
    }

    /**
     * 处理 MQ 派发失败。
     * <p>
     * 手动即时派发需要把失败透传给调用方；自动周期派发则回退为 PENDING，等待后续调度重试。
     *
     * @param taskId 任务 ID
     * @param errorMsg 失败原因
     * @param manualImmediate 是否为用户触发的即时派发
     */
    private void handleDispatchFailure(String taskId, String errorMsg, boolean manualImmediate) {
        String finalErrorMsg = StringUtils.defaultString(errorMsg, "MQ消息发送失败");
        if (manualImmediate) {
            selfServer.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), finalErrorMsg);
            throw new ServiceException(finalErrorMsg);
        }
        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .set(TmsAsyncTaskRecordEntity::getErrorData, finalErrorMsg)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .update();
    }


    /**
     * 解析自动任务主超时：优先使用对账周期配置，缺失时回退批次配置默认值。
     *
     * @param configuredTimeout 对账周期配置中单据维度的超时秒数
     * @return 有效超时秒数
     */
    private Integer resolveConfiguredExecTimeout(Integer configuredTimeout) {
        if (configuredTimeout != null && configuredTimeout > 0) {
            return configuredTimeout;
        }
        return resolveTaskExecTimeout(loadBillBatchParams(null));
    }

    /**
     * 按对账周期配置生成全部自动周期任务（仅创建，不派发 MQ）。
     * <p>
     * 不加 {@code @Transactional}：各子任务通过 {@code selfServer.addAutoTask()} 独立提交，
     * 任意一项失败不影响其余业务。任务为 {@code AUTO + PENDING}，到期派发由 {@link #startTask()} 负责。
     *
     * @return 本次执行汇总，供调度器与应用日志输出
     */
    @Override
    public TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO genAutoTask() {
        long startTime = System.currentTimeMillis();
        log.info("====开始自动生成tms异步任务====");
        List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> results = new ArrayList<>();
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if (Objects.isNull(cfgSettingEntity) || Objects.isNull(cfgSettingEntity.getDataJson())) {
            String message = "无生成系统配置数据";
            log.error("===={}====", message);
            return buildGenAutoTaskResult(System.currentTimeMillis() - startTime,
                Collections.singletonList(buildGenAutoTaskFailedItem("全局配置", message)));
        }

        CfgSettingValueDTO.ReconciliationCycleDTO dto = JSONUtil.toBean(
                cfgSettingEntity.getDataJson(),
                CfgSettingValueDTO.ReconciliationCycleDTO.class
        );
        if (Objects.isNull(dto)) {
            String message = "ReconciliationCycleDTO 为空";
            log.error("===={}====", message);
            return buildGenAutoTaskResult(System.currentTimeMillis() - startTime,
                Collections.singletonList(buildGenAutoTaskFailedItem("全局配置", message)));
        }

        tryGenAutoTask("头程对账单", () -> generateFirstMileReconciliation(dto), results);
        tryGenAutoTask("报关对账", () -> generateTmsB2cDeclareReconciliation(dto), results);
        tryGenAutoTask("头程费用分摊", () -> generateFirstMileCostAllocation(dto), results);
        tryGenAutoTask("小包费用分摊", () -> generateSmallBagCostAllocation(dto), results);
        tryGenAutoTask("中转费用分摊", () -> generateTransferDeclareCostAllocation(dto), results);

        TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO summary =
            buildGenAutoTaskResult(System.currentTimeMillis() - startTime, results);
        logGenAutoTaskSummary(summary);
        return summary;
    }

    /**
     * 执行单个业务生成器并收集结果；异常不向外抛出，转为 FAILED 汇总项。
     *
     * @param taskName 展示名称，写入汇总日志
     * @param task     业务生成器
     * @param results  汇总结果收集器
     */
    private void tryGenAutoTask(String taskName, java.util.function.Supplier<List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult>> task,
                                List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> results) {
        try {
            List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> items = task.get();
            if (CollUtil.isNotEmpty(items)) {
                results.addAll(items);
            }
        } catch (Exception e) {
            log.error("[自动生成任务] {} 生成异常: {}", taskName, e.getMessage(), e);
            results.add(buildGenAutoTaskFailedItem(taskName, e.getMessage()));
        }
    }

    /**
     * 构建生成失败汇总项。
     *
     * @param taskName 展示名称
     * @param message  失败原因
     * @return FAILED 状态的结果项
     */
    private TmsAsyncTaskRecordDTO.GenAutoTaskItemResult buildGenAutoTaskFailedItem(String taskName, String message) {
        return new TmsAsyncTaskRecordDTO.GenAutoTaskItemResult(
            taskName, null, null, null, null,
            TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_FAILED, message);
    }

    /**
     * 构建「生成类型不支持」汇总项。
     *
     * @param taskName         展示名称
     * @param unsupportedType  当前配置的 reconciliationType 值
     * @return SKIPPED_UNSUPPORTED 状态的结果项
     */
    private TmsAsyncTaskRecordDTO.GenAutoTaskItemResult buildGenAutoTaskSkippedUnsupportedItem(
            String taskName, String unsupportedType) {
        return new TmsAsyncTaskRecordDTO.GenAutoTaskItemResult(
            taskName, null, null, null, null,
            TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_SKIPPED_UNSUPPORTED,
            "生成类型【" + unsupportedType + "】不支持");
    }

    /**
     * 创建单条自动任务并返回对应汇总项。
     * <p>
     * 调用方须预先完成去重判断（{@code exists}）并传入该单据在周期配置中的超时秒数。
     *
     * @param taskName      展示名称
     * @param businessType  业务类型
     * @param methodType    方法类型
     * @param startTimeStr  调度日 yyyy-MM-dd
     * @param jsonStr       信封 JSON
     * @param execTimeout   主任务超时秒数
     * @param exists        同调度日任务是否已存在
     * @return CREATED / SKIPPED_EXISTS / FAILED 汇总项
     */
    private TmsAsyncTaskRecordDTO.GenAutoTaskItemResult createAutoTaskItem(String taskName, String businessType,
            String methodType, String startTimeStr, String jsonStr, Integer execTimeout, boolean exists) {
        if (exists) {
            return new TmsAsyncTaskRecordDTO.GenAutoTaskItemResult(
                taskName, businessType, methodType, startTimeStr, null,
                TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_SKIPPED_EXISTS, null);
        }
        TmsAsyncTaskRecordDTO.AutoCreateDTO createDTO = new TmsAsyncTaskRecordDTO.AutoCreateDTO(
            businessType, methodType, jsonStr, startTimeStr, execTimeout);
        String taskId = selfServer.addAutoTask(createDTO);
        if (StringUtils.isNotBlank(taskId)) {
            return new TmsAsyncTaskRecordDTO.GenAutoTaskItemResult(
                taskName, businessType, methodType, startTimeStr, taskId,
                TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_CREATED, null);
        }
        return new TmsAsyncTaskRecordDTO.GenAutoTaskItemResult(
            taskName, businessType, methodType, startTimeStr, null,
            TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_FAILED, "任务保存失败");
    }

    /**
     * 统计汇总计数并封装返回对象。
     *
     * @param durationMs 总耗时（毫秒）
     * @param items      逐项结果
     * @return 带 created / skipped / failed 计数的汇总
     */
    private TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO buildGenAutoTaskResult(long durationMs,
            List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> items) {
        int createdCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        for (TmsAsyncTaskRecordDTO.GenAutoTaskItemResult item : items) {
            if (TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_CREATED.equals(item.getStatus())) {
                createdCount++;
            } else if (TmsAsyncTaskRecordDTO.GenAutoTaskItemResult.STATUS_FAILED.equals(item.getStatus())) {
                failedCount++;
            } else {
                skippedCount++;
            }
        }
        return new TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO(durationMs, items, createdCount, skippedCount, failedCount);
    }

    /**
     * 将生成汇总写入应用日志，供排查与对账。
     *
     * @param summary 本次 genAutoTask 执行汇总
     */
    private void logGenAutoTaskSummary(TmsAsyncTaskRecordDTO.GenAutoTaskResultDTO summary) {
        log.info("====结束自动生成tms异步任务 耗时={}ms created={} skipped={} failed={}====",
            summary.getDurationMs(), summary.getCreatedCount(), summary.getSkippedCount(), summary.getFailedCount());
        if (CollUtil.isEmpty(summary.getItems())) {
            return;
        }
        for (TmsAsyncTaskRecordDTO.GenAutoTaskItemResult item : summary.getItems()) {
            log.info("[自动生成任务] {} | {} | businessType={} methodType={} startTime={} taskId={} message={}",
                item.getTaskName(), item.getStatus(), item.getBusinessType(), item.getMethodType(),
                item.getStartTimeStr(), item.getTaskId(), item.getMessage());
        }
    }


    /**
     * 自动生成中转费用分摊下推任务（{@code TRANSFER_DECLARE_COST_ALLOCATION + PUSH_ALLOCATION}）。
     * <p>
     * 按配置的自然月生成日在当月创建 {@code AUTO + PENDING} 任务，载荷为上月审核日期区间；
     * 任务由 {@link #startTask()} 到期派发，同一 {@code businessType + methodType + 调度日} 去重。
     *
     * @param dto 对账周期配置，仅支持 {@code CREAT_BY_MONTH} 且读取 {@code transferAllocationType}
     * @return 单项或跳过/失败汇总列表（长度恒为 1）
     */
    private List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> generateTransferDeclareCostAllocation(
            CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        if (!ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getTransferAllocationType())) {
            log.warn("[生成中转费用分摊] 生成类型【{}】不支持", dto.getTransferAllocationType());
            return Collections.singletonList(
                buildGenAutoTaskSkippedUnsupportedItem("中转费用分摊", dto.getTransferAllocationType()));
        }
        int transferAllocationDate = resolveAutoTaskDay(dto.getTransferAllocationDate(), "中转费用分摊", "生成日期");
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        int maxDay = yearMonth.lengthOfMonth();
        int day = Math.min(transferAllocationDate, maxDay);
        String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime taskStartTime = DateUtil.parse(startTimeStr).toLocalDateTime();
        LocalDateTime periodStart = taskStartTime.minus(1, ChronoUnit.MONTHS)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime periodEnd = taskStartTime.minus(0, ChronoUnit.MONTHS)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode();
        String methodType = TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode();
        TmsAsyncTaskRecordDTO.TransferDeclarePushAllocationPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.TransferDeclarePushAllocationPayloadDTO(
                periodStart.toLocalDate(), periodEnd.toLocalDate());
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = buildEnvelope(
            businessType, methodType, null, null, payload);
        String jsonStr = JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
        return Collections.singletonList(createAutoTaskItem(
            "中转费用分摊", businessType, methodType, startTimeStr, jsonStr,
            resolveConfiguredExecTimeout(dto.getTransferBeginExecTimeout()),
            isExist(businessType, methodType, startTimeStr)));
    }

    /**
     * 自动生成小包费用分摊下推任务（{@code SMALL_BAG_COST_ALLOCATION}）。
     * <p>
     * 按 {@code packageAllocationType = CREAT_BY_MONTH} 在当月调度日创建两条任务：
     * 自配送（{@code SELFDELIVER_PUSH_ALLOCATION}）与尾程（{@code LASTMILE_PUSH_ALLOCATION}），
     * 载荷为 {@code reportDate + type}，超时取自 {@code packageBeginExecTimeout}。
     *
     * @param dto 对账周期配置
     * @return 最多 2 项汇总结果
     */
    private List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> generateSmallBagCostAllocation(
            CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        if (!ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getPackageAllocationType())) {
            log.warn("[生成小包费用分摊] 生成类型【{}】不支持", dto.getPackageAllocationType());
            return Collections.singletonList(
                buildGenAutoTaskSkippedUnsupportedItem("小包费用分摊", dto.getPackageAllocationType()));
        }
        int packageAllocationDate = resolveAutoTaskDay(dto.getPackageAllocationDate(), "小包费用分摊", "生成日期");
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        int maxDay = yearMonth.lengthOfMonth();
        int day = Math.min(packageAllocationDate, maxDay);
        LocalDate localDate = now.withDayOfMonth(day);
        String startTimeStr = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime taskStartTime = DateUtil.parse(startTimeStr).toLocalDateTime();
        LocalDateTime startTime = taskStartTime.minus(1, ChronoUnit.MONTHS)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime endTime = taskStartTime.minus(0, ChronoUnit.MONTHS)
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();
        String reportDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Integer execTimeout = resolveConfiguredExecTimeout(dto.getPackageBeginExecTimeout());
        List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> results = new ArrayList<>(2);
        results.add(createSmallBagPushAutoTask("小包费用分摊-自配送", businessType, startTimeStr, reportDate,
            DictCostAttributionEnum.SELF_DELIVER.getCode(),
            TmsAsyncTaskMethodTypeEnum.SELFDELIVER_PUSH_ALLOCATION.getCode(), execTimeout));
        results.add(createSmallBagPushAutoTask("小包费用分摊-尾程", businessType, startTimeStr, reportDate,
            DictCostAttributionEnum.LAST_MILE.getCode(),
            TmsAsyncTaskMethodTypeEnum.LASTMILE_PUSH_ALLOCATION.getCode(), execTimeout));
        return results;
    }

    /**
     * 创建单条小包下推自动任务汇总项。
     * <p>
     * 去重时兼容历史 {@code PUSH_ALLOCATION} 方法类型任务，避免迁移期重复创建。
     *
     * @param taskName      展示名称
     * @param businessType  业务类型
     * @param startTimeStr  调度日
     * @param reportDate    核算月份 yyyy-MM
     * @param type          费用归属（自配送/尾程）
     * @param methodType    方法类型
     * @param execTimeout   主任务超时秒数
     * @return 单项汇总结果
     */
    private TmsAsyncTaskRecordDTO.GenAutoTaskItemResult createSmallBagPushAutoTask(String taskName, String businessType,
            String startTimeStr, String reportDate, String type, String methodType, Integer execTimeout) {
        TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.SmallBagPushAllocationPayloadDTO(reportDate, type);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
            buildEnvelope(businessType, methodType, null, null, payload);
        String jsonStr = JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
        return createAutoTaskItem(taskName, businessType, methodType, startTimeStr, jsonStr, execTimeout,
            isExistSmallBagPushAutoTask(businessType, methodType, startTimeStr));
    }

    /**
     * 自动生成报关对账下推任务（{@code TMS_B2C_DECLARE_RECONCILIATION + PUSH_ALLOCATION}）。
     * <p>
     * {@code CREAT_BY_MONTH} 固定每月 1 日为调度日；{@code CREAT_BY_PERIOD} 使用配置的生成日期。
     * 账期均为调度日的上一个自然月。
     *
     * @param dto 对账周期配置
     * @return 单项汇总结果
     */
    private List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> generateTmsB2cDeclareReconciliation(
            CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        String startTimeStr ="";
        //生成日期1-31
        Integer declareReconciliationDate = MathUtil.ONE;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getDeclareReconciliationType())) {
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(declareReconciliationDate, maxDay); // 取较小值
            startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate taskStartDate = LocalDate.parse(startTimeStr);
            //根据任务执行时间，推算出当时的时间范围
            startDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            endDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        } else if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getDeclareReconciliationType())) {
            declareReconciliationDate = resolveAutoTaskDay(dto.getDeclareReconciliationDate(), "报关对账", "生成日期");

            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(declareReconciliationDate, maxDay); // 取较小值
            startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate taskStartDate = LocalDate.parse(startTimeStr);
            //根据任务执行时间，推算出当时的时间范围
            startDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            endDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }else {
            log.warn("[生成报关对账] 生成类型【{}】不支持", dto.getDeclareReconciliationType());
            return Collections.singletonList(
                buildGenAutoTaskSkippedUnsupportedItem("报关对账", dto.getDeclareReconciliationType()));
        }
        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode();
        String methodType = TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode();
        TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.B2cDeclareReconciliationPushPayloadDTO(startDate, endDate);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = buildEnvelope(
            businessType, methodType, null, null, payload);
        String jsonStr = JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
        return Collections.singletonList(createAutoTaskItem(
            "报关对账", businessType, methodType, startTimeStr, jsonStr,
            resolveConfiguredExecTimeout(dto.getDeclareExecTimeout()),
            isExist(businessType, methodType, startTimeStr)));
    }

    /**
     * 自动生成头程对账单下推任务（{@code TMS_FIRST_MILE_RECONCILIATION + PUSH_ALLOCATION}）。
     * <p>
     * {@code CREAT_BY_MONTH}：调度日固定为每月 1 日，账期为上自然月；
     * {@code CREAT_BY_PERIOD}：调度日为配置日，账期为「上月配置日 ~ 调度日前一日」。
     *
     * @param dto 对账周期配置
     * @return 单项汇总结果
     */
    private List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> generateFirstMileReconciliation(
            CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        String startTimeStr ="";
        //生成日期1-31
        Integer firstMileReconciliationDate = MathUtil.ONE;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getFirstMileReconciliationType())) {
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(firstMileReconciliationDate, maxDay); // 取较小值
            startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate taskStartDate = LocalDate.parse(startTimeStr);

            startDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            endDate = taskStartDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        } else if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileReconciliationType())) {
            firstMileReconciliationDate = resolveAutoTaskDay(dto.getFirstMileReconciliationDate(), "头程对账单", "生成日期");

            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(firstMileReconciliationDate, maxDay); // 取较小值
            startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate taskStartDate = LocalDate.parse(startTimeStr);

            endDate = taskStartDate.minusDays(1);
            startDate = taskStartDate.minusMonths(1);
        }else {
            log.warn("[生成头程对账单] 生成类型【{}】不支持", dto.getFirstMileReconciliationType());
            return Collections.singletonList(
                buildGenAutoTaskSkippedUnsupportedItem("头程对账单", dto.getFirstMileReconciliationType()));
        }
        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode();
        String methodType = TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode();
        TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.FirstMileReconciliationPushPayloadDTO(startDate, endDate);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope = buildEnvelope(
            businessType, methodType, null, null, payload);
        String jsonStr = JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
        return Collections.singletonList(createAutoTaskItem(
            "头程对账单", businessType, methodType, startTimeStr, jsonStr,
            resolveConfiguredExecTimeout(dto.getFirstMileExecTimeout()),
            isExist(businessType, methodType, startTimeStr)));
    }

    /**
     * 自动生成头程费用分摊下推任务（{@code FIRST_MILE_COST_ALLOCATION + PUSH_ALLOCATION}）。
     * <p>
     * 仅支持 {@code CREAT_BY_PERIOD}；载荷 {@code reportDate} 为调度日所在月的上一个月（yyyy-MM）。
     *
     * @param dto 对账周期配置
     * @return 单项汇总结果
     */
    private List<TmsAsyncTaskRecordDTO.GenAutoTaskItemResult> generateFirstMileCostAllocation(
            CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        if (!ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileAllocationType())) {
            log.warn("[生成头程费用分摊] 生成类型【{}】不支持", dto.getFirstMileAllocationType());
            return Collections.singletonList(
                buildGenAutoTaskSkippedUnsupportedItem("头程费用分摊", dto.getFirstMileAllocationType()));
        }
        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode();
        String methodType = TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode();
        int firstMileAllocationDate = resolveAutoTaskDay(dto.getFirstMileAllocationDate(), "头程费用分摊", "生成日期");
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        int maxDay = yearMonth.lengthOfMonth();
        int day = Math.min(firstMileAllocationDate, maxDay);
        LocalDate localDate = now.withDayOfMonth(day);
        String startTimeStr = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String reportDate = localDate.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO payload =
            new TmsAsyncTaskRecordDTO.FirstMilePushAllocationPayloadDTO(reportDate);
        TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope =
            buildEnvelope(businessType, methodType, null, null, payload);
        String jsonStr = JSONUtil.toJsonStr(envelope, TASK_DATA_JSON_CONFIG);
        return Collections.singletonList(createAutoTaskItem(
            "头程费用分摊", businessType, methodType, startTimeStr, jsonStr,
            resolveConfiguredExecTimeout(dto.getFirstMileAllocationeExecTimeout()),
            isExist(businessType, methodType, startTimeStr)));
    }


}
