package com.erp.server.tms.service;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.common.business.service.SuperService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 异步任务记录 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface TmsAsyncTaskRecordService extends SuperService<TmsAsyncTaskRecordEntity> {

    /**
     * 新增手动任务（带方法类型，在同一 businessType 下区分不同方法）
     * 防重键为 businessType + methodType + dataJson
     *
     * @param detailCount 预期明细数量（创建时直接写入，省去二次更新）
     * @return 创建成功的任务记录；命中防重或保存失败时返回 null
     */
    TmsAsyncTaskRecordEntity addManualTask(TmsAsyncTaskRecordDTO.ManualCreateDTO dto);

    /**
     * 新增自动任务（带方法类型）
     */
    String addAutoTask(TmsAsyncTaskRecordDTO.AutoCreateDTO dto);

    void updateTask(String taskId, String status, String errorMsg);

    /**
     * 任务未执行即失败收尾：FINISH + errorData + errorCount=1，便于任务列表识别异常结束
     */
    void finishTaskWithError(String taskId, String errorMsg);

    /**
     * 加载批次配置；缺失时标记任务失败并返回 null
     */
    CfgSettingValueDTO.BillBatchParamsDTO loadBillBatchParams(String taskId);

    /**
     * 解析批次大小，非法或缺失时使用默认值
     */
    int resolveBatchSize(String batchConfig, int defaultSize);

    /**
     * 解析批次并发等待超时秒数，非法或 <=0 时使用默认值
     */
    int resolveTimeoutSeconds(String timeoutConfig, int defaultSeconds);

    /**
     * 从异步任务批次配置解析主任务执行超时时间。
     * <p>
     * 该值用于消费侧和定时器统一判断主任务是否超时，避免不同入口使用不同超时来源。
     *
     * @param billBatchParamsDTO 批次配置，来源于 {@link #loadBillBatchParams(String)}
     * @return 主任务超时时间，单位：秒
     */
    int resolveTaskExecTimeout(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO);

    /**
     * 从异步任务批次配置解析小包明细僵死判定窗口。
     * <p>
     * 判定窗口由批次等待超时时间和明细僵死缓冲时间相加得到，降低慢执行被误判为僵死的概率。
     *
     * @param billBatchParamsDTO 批次配置，来源于 {@link #loadBillBatchParams(String)}
     * @return 小包明细僵死判定窗口，单位：秒
     */
    int resolveStaleDetailSeconds(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO);

    int resolveSmallBagStaleDetailSeconds(CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO);

    /**
     * 判断小包明细是否为超过执行窗口的 ING 明细。
     *
     * @param detail 任务明细
     * @param staleBefore 僵死阈值时间，早于或等于该时间的 ING 明细视为僵死
     * @return true 表示明细处于 ING 且执行开始时间已超过僵死阈值
     */
    boolean isStaleIngDetail(com.erp.model.tms.entity.TmsAsyncTaskDetailEntity detail, LocalDateTime staleBefore);

    boolean isSmallBagStaleIngDetail(com.erp.model.tms.entity.TmsAsyncTaskDetailEntity detail, LocalDateTime staleBefore);

    /**
     * 格式化任务错误信息，避免 getMessage() 为 null
     */
    String formatTaskErrorMessage(Exception e);

    /**
     * MQ 派发前 CAS 认领失败时的幂等处理：任务已在 ING 则返回成功，否则标记失败并抛异常
     *
     * @return 幂等成功时返回 BatchResultDTO，认领成功时返回 null 由调用方继续
     */
    BatchResultDTO resolveDispatchClaimOrThrow(String taskId, boolean claimed, String taskCode, String errorPayload);

    /**
     * 构建业务载荷类型，默认使用 businessType:methodType 约定。
     */
    String buildPayloadType(String businessType, String methodType);

    /**
     * 构建 TMS 异步任务信封。
     */
    TmsAsyncTaskRecordDTO.TaskEnvelopeDTO buildEnvelope(String businessType, String methodType,
                                                        String retryMode, String retrySourceTaskId, Object payload);

    /**
     * 解析任务信封；非信封 JSON 返回 null。
     */
    TmsAsyncTaskRecordDTO.TaskEnvelopeDTO parseEnvelope(String dataJson);

    /**
     * 解析信封中的业务载荷。
     */
    <T> T parseEnvelopePayload(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope, Class<T> payloadClass);

    /**
     * 解析信封业务载荷；解析失败时按给定原因结束任务。
     *
     * @param taskId 任务 ID
     * @param envelope 任务信封
     * @param payloadClass 业务载荷类型
     * @param errorMsg 解析失败时写入主任务的失败原因
     * @param <T> 业务载荷泛型
     * @return 业务载荷；解析失败时返回 null
     */
    <T> T parseEnvelopePayloadOrFinishTask(String taskId, TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                           Class<T> payloadClass, String errorMsg);

    /**
     * 从任务记录和信封构建消费期运行态参数。
     */
    TmsAsyncTaskRecordDTO.PushParamsDTO buildDispatchPushParams(TmsAsyncTaskRecordEntity taskRecord,
                                                                TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope);

    /**
     * 认领待执行任务并派发 MQ。
     */
    void claimAndDispatch(TmsAsyncTaskRecordEntity entity, boolean manualImmediate);

    /**
     * 解析异步任务执行业务时的操作人：优先 PushParams 显式操作人，错误重试时追溯源任务创建人，否则取当前任务创建人。
     */
    LoginUser resolveOperatorLoginUser(TmsAsyncTaskRecordEntity taskRecord, TmsAsyncTaskRecordDTO.PushParamsDTO pushParams);

    /**
     * 分批循环内检查任务是否应终止（记录消失或已完成）
     */
    boolean shouldStopLoopTask(String taskId, TmsAsyncTaskRecordEntity currentTask);

    /**
     * 分批循环内检查主任务是否已超过执行超时时间。
     * <p>
     * 若任务已超时，该方法会调用统一超时终止流程并返回 true，业务循环应立即停止。
     *
     * @param currentTask 当前主任务记录
     * @param billBatchParamsDTO 批次配置，用于任务未写入 execTimeout 时兜底解析
     * @return true 表示已触发超时终止，调用方应停止后续批次处理
     */
    boolean terminateTaskIfExecTimeoutReached(TmsAsyncTaskRecordEntity currentTask,
                                              CfgSettingValueDTO.BillBatchParamsDTO billBatchParamsDTO);

    void terminateTaskTimeout(String taskId, String errorMsg);

    void updateTaskFinally(String taskId);

    PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto);

    List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto);

    void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO dto, HttpServletResponse response);

    PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> dto);

    void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO dto, HttpServletResponse response);

    void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto);

    BatchResultDTO retry(TmsAsyncTaskRecordEntity entity);

    BatchResultDTO errorRetry(TmsAsyncTaskRecordEntity entity);

    /**
     * 创建失败明细重试任务。
     * <p>
     * 该方法仅负责数据库事务内的任务创建和源任务标记，MQ 派发由调用方在事务提交后执行。
     */
    TmsAsyncTaskRecordEntity createFailedOnlyRetryTask(TmsAsyncTaskRecordEntity entity);

    Boolean isExist(String businessType, String methodType, String startTimeStr);

    void updateTaskDetailFailure(String taskDetailId, Exception e);

    /**
     * 实现类需保证事务边界。
     */
    void startTask();


    void genAutoTask();

    /**
     * 异步任务 watchdog：负责超时和孤儿明细清理。
     */
    void watchdogTask();
}
