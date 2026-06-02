package com.erp.server.tms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SystemCodeEnum;
import com.common.business.vo.PagingVO;
import com.alibaba.fastjson.JSON;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.DistributeKeyConstant;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskMethodTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordBusinessTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordExecTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.TmsAsyncTaskRecordMapper;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "businessType", unlockAfterTx = true)
    public TmsAsyncTaskRecordEntity addManualTask(String businessType, String methodType, Integer detailCount, String json){
        String compactJson = compactTaskDataJson(json);
        List<TmsAsyncTaskRecordEntity> runningTasks = lambdaQuery()
            .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
            .in(TmsAsyncTaskRecordEntity::getStatus,
                Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
            .list();

        String reportPeriodStr = extractReportPeriodStr(compactJson);
        if (StringUtils.isNotBlank(reportPeriodStr) && CollUtil.isNotEmpty(runningTasks)) {
            Optional<TmsAsyncTaskRecordEntity> periodConflict = runningTasks.stream()
                .filter(task -> reportPeriodStr.equals(extractReportPeriodStr(task.getDataJson())))
                .findFirst();
            if (periodConflict.isPresent()) {
                TmsAsyncTaskRecordEntity conflict = periodConflict.get();
                log.warn("手动异步任务核算期间冲突，businessType: {}, reportPeriodStr: {}, 进行中任务 code: {}, methodType: {}",
                    businessType, reportPeriodStr, conflict.getCode(), conflict.getMethodType());
                return null;
            }
        }

        if (CollUtil.isNotEmpty(runningTasks)
            && runningTasks.stream()
            .filter(task -> StringUtils.equals(methodType, task.getMethodType())
                || (StringUtils.isBlank(methodType) && StringUtils.isBlank(task.getMethodType())))
            .anyMatch(task -> isSameManualTaskDataJson(compactJson, task.getDataJson()))) {
            log.warn("手动异步任务参数重复，businessType: {}, methodType: {}", businessType, methodType);
            return null;
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setMethodType(methodType);
        entity.setDetailCount(detailCount);
        entity.setDataJson(compactJson);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        entity.setExecType(TmsAsyncTaskRecordExecTypeEnum.MANUAL.getCode());
        return save(entity) ? entity : null;
    }

    /**
     * 比较手动任务入参是否等价（忽略 taskId、游标等执行过程字段）
     */
    private boolean isSameManualTaskDataJson(String json1, String json2) {
        try {
            return Objects.equals(normalizeManualTaskDataJson(json1), normalizeManualTaskDataJson(json2));
        } catch (Exception e) {
            log.warn("任务参数归一化比较失败，保守策略：允许创建任务, err={}", e.getMessage());
            return false;
        }
    }

    private String normalizeManualTaskDataJson(String json) {
        return compactTaskDataJson(json);
    }

    /**
     * 持久化用任务参数 JSON：仅保留有值字段，去掉执行期字段及表列已存的 businessType/methodType
     */
    private String compactTaskDataJson(String json) {
        if (StringUtils.isBlank(json)) {
            return "";
        }
        TmsAsyncTaskRecordDTO.PushParamsDTO params = parsePushParams(json);
        if (params == null) {
            return json.trim();
        }
        sanitizePushParamsForStorage(params);
        return JSONUtil.toJsonStr(params, TASK_DATA_JSON_CONFIG);
    }

    private String extractReportPeriodStr(String json) {
        TmsAsyncTaskRecordDTO.PushParamsDTO params = parsePushParams(json);
        if (params == null) {
            return null;
        }
        return trimToNull(params.getReportPeriodStr());
    }

    private TmsAsyncTaskRecordDTO.PushParamsDTO parsePushParams(String json) {
        try {
            return JSONUtil.toBean(json, TmsAsyncTaskRecordDTO.PushParamsDTO.class);
        } catch (Exception e) {
            log.debug("任务参数解析 PushParamsDTO 失败: {}", e.getMessage());
            return null;
        }
    }

    private void sanitizePushParamsForStorage(TmsAsyncTaskRecordDTO.PushParamsDTO params) {
        params.setTaskId(null);
        params.setLastId(null);
        params.setBatchSize(null);
        params.setBusinessType(null);
        params.setMethodType(null);
        params.setReportPeriodStr(trimToNull(params.getReportPeriodStr()));
        params.setReportStatus(trimToNull(params.getReportStatus()));
        params.setReportDate(trimToNull(params.getReportDate()));
        params.setType(trimToNull(params.getType()));
        if (CollUtil.isNotEmpty(params.getIds())) {
            List<String> sortedIds = params.getIds().stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .sorted()
                .collect(Collectors.toList());
            params.setIds(CollUtil.isEmpty(sortedIds) ? null : sortedIds);
        } else {
            params.setIds(null);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 新增自动任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "businessType", unlockAfterTx = true)
    public String addAutoTask(String businessType, String methodType, String json,String startTimeStr){
        //默认8小时
        Integer execTimeout = null;
        Integer errorCount = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && Objects.nonNull(cfgSettingEntity.getDataJson())){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            //头程对账单
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType, TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setMethodType(methodType);
        entity.setDataJson(json);
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
        if(Objects.nonNull(execTimeout)){
            entity.setExecTimeout(execTimeout);
        }
        if(Objects.nonNull(errorCount)){
            entity.setErrorCount(errorCount);
        }
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
                .set(TmsAsyncTaskRecordEntity::getErrorData, StringUtils.substring(errorMsg, 0, 1000))
                .set(TmsAsyncTaskRecordEntity::getErrorCount, 1)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    @Override
    public void finishTaskOnMainRecord(String taskId) {
        TmsAsyncTaskRecordEntity task = getById(taskId);
        boolean clearErrorData = task == null || task.getErrorCount() == null || task.getErrorCount() == 0;
        if (clearErrorData) {
            lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, "")
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
        } else {
            lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
        }
    }

    @Override
    public CfgSettingValueDTO.BillBatchParamsDTO loadBillBatchParams(String taskId) {
        CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.BILL_BATCH_PARAMS.getCode());
        if (byKey == null || byKey.getDataJson() == null) {
            log.error("配置项 {} 不存在或 dataJson 为空，taskId: {}",
                CfgSettingEnum.BILL_BATCH_PARAMS.getCode(), taskId);
            finishTaskWithError(taskId, "批次配置缺失");
            return null;
        }
        return JSON.parseObject(byKey.getDataJson().toJSONString(0), CfgSettingValueDTO.BillBatchParamsDTO.class);
    }

    @Override
    public int resolveBatchSize(String batchConfig, int defaultSize) {
        int batchSize = NumberUtils.toInt(batchConfig, defaultSize);
        return batchSize <= 0 ? defaultSize : batchSize;
    }

    @Override
    public String formatTaskErrorMessage(Exception e) {
        String message = e == null ? null : e.getMessage();
        return StringUtils.substring(Objects.toString(message, e == null ? "未知错误" : e.getClass().getSimpleName()), 0, 1000);
    }

    @Override
    public BatchResultDTO resolveDispatchClaimOrThrow(String taskId, boolean claimed, String taskCode, String errorPayload) {
        if (claimed) {
            return null;
        }
        TmsAsyncTaskRecordEntity existing = getById(taskId);
        if (existing != null && TmsAsyncTaskRecordStatusEnum.ING.getCode().equals(existing.getStatus())) {
            return BatchResultDTO.success(taskId, existing.getCode());
        }
        finishTaskWithError(taskId, "任务状态竞争失败");
        throw new ServiceException(ApiError.LOGISTICS_ASYNC_TASK_CREATE_ERROR, errorPayload);
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
     * 任务超时中止
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void terminateTaskTimeout(String taskId, String errorMsg) {
        List<TmsAsyncTaskDetailEntity> detailEntityList = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).list();
        int detailCount = detailEntityList.size();
        long finishCount = detailEntityList.stream().filter(e -> e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.FINISH.getCode())).count();
        tmsAsyncTaskDetailService.lambdaUpdate().set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
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
        Set<String> existStatusSet = list.stream().map(TmsAsyncTaskRecordDTO.TabListDTO::getTabFlag).collect(Collectors.toSet());
        for (String status : statusList) {
            if (!existStatusSet.contains(status)) {
                list.add(new TmsAsyncTaskRecordDTO.TabListDTO(status, "", 0));
            }
        }

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
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "entity.businessType", unlockAfterTx = true)
    public BatchResultDTO retry(TmsAsyncTaskRecordEntity entity) {
        //数据校验
        checkData(entity);

        //默认8小时
        Integer execTimeout = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && ObjectUtil.isNotEmpty(cfgSettingEntity.getDataJson()) && !Objects.equals(cfgSettingEntity.getDataJson(), "{}")){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            String businessType = entity.getBusinessType();
            //头程对账单
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setRetryTimes(entity.getRetryTimes() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(entity.getDataJson());
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        if(Objects.nonNull(execTimeout)){
            newTask.setExecTimeout(execTimeout);
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(businessType = DistributeKeyConstant.TMS_ASYNC_TASK_RECORD_KEY, keyName = "entity.businessType", unlockAfterTx = true)
    public BatchResultDTO errorRetry(TmsAsyncTaskRecordEntity entity) {
        //数据校验
        checkData(entity);
        //失败明细业务id集合
        List<String> businessIds = tmsAsyncTaskDetailService.listErrorDetail(entity.getId()).stream().map(TmsAsyncTaskDetailEntity::getBusinessId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isEmpty(businessIds)){
            throw new ServiceException("未找到错误明细");
        }
        //默认8小时
        Integer execTimeout = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && ObjectUtil.isNotEmpty(cfgSettingEntity.getDataJson()) && !Objects.equals(cfgSettingEntity.getDataJson(), "{}")){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            String businessType = entity.getBusinessType();
            //头程对账单
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        //entity.getDataJson()转TmsAsyncTaskRecordDTO.PushDTO实体类
        TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = JSONUtil.toBean(entity.getDataJson(), TmsAsyncTaskRecordDTO.PushParamsDTO.class);
        pushDTO.setIds(businessIds);

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setRetryTimes(entity.getRetryTimes() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(JSONUtil.toJsonStr(pushDTO));
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        if(Objects.nonNull(execTimeout)){
            newTask.setExecTimeout(execTimeout);
        }
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

    @Override
    public Boolean isExist(String businessType, String startTimeStr) {
        LocalDateTime startTime = DateUtil.parse(startTimeStr).toLocalDateTime();
        LocalDateTime endTime = startTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        Integer exist = baseMapper.isExist(businessType, startTime, endTime);
        if(exist >0 ){
            return true;
        }
        return false;
    }


    /**
     * 任务结束，记录错误数量
     */
    @Override
    public void updateTaskFinally(String taskId) {
        Integer errorCount = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode()).count();
        Integer detailCount = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).count();

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, "")
                .set(TmsAsyncTaskRecordEntity::getErrorCount,errorCount)
                .set(TmsAsyncTaskRecordEntity::getDetailCount,detailCount)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
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
                StringUtils.substring(errorMsg, 0, 1000));
    }

    /**
     * 启动任务
     * 中止超时任务
     */
    @Override
    public void startTask(){
        LocalDateTime now = LocalDateTime.now();

        // 查询所有 ING 或 PENDING 且 startTime <= now 的任务，去掉今日限制：
        // 避免补偿场景（如服务宕机恢复后）中历史 PENDING 任务被永久搁置
        List<TmsAsyncTaskRecordEntity> list = lambdaQuery()
                .in(TmsAsyncTaskRecordEntity::getStatus, Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .le(TmsAsyncTaskRecordEntity::getStartTime, now)
                .list();
        if(CollUtil.isEmpty(list)){
            log.info("TmsAsyncTaskRecord不存在待执行或者执行中的任务");
            return;
        }

        //执行中（手动 + 自动）
        List<TmsAsyncTaskRecordEntity> ingList = list.stream().filter(e -> e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.ING.getCode())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(ingList)){
            for (TmsAsyncTaskRecordEntity entity : ingList) {
                Integer execTimeout = entity.getExecTimeout();
                LocalDateTime startTime = entity.getStartTime();
                if(Objects.nonNull(startTime) && Objects.nonNull(execTimeout) && execTimeout > 0 ){
                    if(now.isAfter(startTime.plusSeconds(execTimeout))){
                        selfServer.terminateTaskTimeout(entity.getId(),"任务执行超时");
                    }
                }
            }
        }

        // 1.开始时间小于等于当前时间则继续执行后续业务代码
        // 2.自动类型
        // 3.待执行
        List<TmsAsyncTaskRecordEntity> pendingList = list.stream()
                .filter(e ->e.getStartTime().isBefore(now) && e.getExecType().equals(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode()) && e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .collect(Collectors.toList());
        if(CollUtil.isNotEmpty(pendingList)){
            for (TmsAsyncTaskRecordEntity entity : pendingList) {
                claimAndDispatch(entity);
            }
        }
    }

    private void claimAndDispatch(TmsAsyncTaskRecordEntity entity) {
        String dataJson = entity.getDataJson();
        if (StringUtils.isBlank(dataJson) || Objects.equals(dataJson, "{}")) {
            selfServer.updateTask(entity.getId(), TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "dataJson为空直接结束任务");
            return;
        }

        //消费者有回退机制
        TmsAsyncTaskRecordDTO.PushParamsDTO taskDTO = JSONUtil.toBean(entity.getDataJson(), TmsAsyncTaskRecordDTO.PushParamsDTO.class);
        taskDTO.setTaskId(entity.getId());
        boolean claimed = lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                .set(TmsAsyncTaskRecordEntity::getErrorData, "任务已派发")
                .eq(TmsAsyncTaskRecordEntity::getId, entity.getId())
                .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                .update();
        if (!claimed) {
            log.warn("异步任务已被其他调度认领，taskId: {}", entity.getId());
            return;
        }
        try {
            SendResult sendResult = mQProducerService.syncClassMsg(RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC, RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG, taskDTO, taskDTO.getTaskId());
            if (!SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                log.error("消息发送结果失败：{}", JSON.toJSONString(sendResult));
                lambdaUpdate()
                        .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                        .set(TmsAsyncTaskRecordEntity::getErrorData, "MQ消息发送失败")
                        .eq(TmsAsyncTaskRecordEntity::getId, entity.getId())
                        .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                        .update();
            } else {
                log.info("MQ数据结果：{}", JSONUtil.toJsonStr(sendResult));
            }
        } catch (Exception e) {
            log.error("消息发送异常，taskId: {}", entity.getId(), e);
            lambdaUpdate()
                    .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.PENDING.getCode())
                    .set(TmsAsyncTaskRecordEntity::getErrorData, StringUtils.substring(e.getMessage(), 0, 1000))
                    .eq(TmsAsyncTaskRecordEntity::getId, entity.getId())
                    .eq(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode())
                    .update();
        }
    }


    /**
     * 根据tms生成配置生成异步任务。
     * 不加 @Transactional：各子任务通过 selfServer.addAutoTask() 独立提交，
     * 任意一个生成失败不影响其他任务，失败时仅记录错误日志。
     */
    @Override
    public void genAutoTask() {
        long startTime = System.currentTimeMillis();
        log.info("====开始自动生成tms异步任务====");
        // 查询系统配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.isNull(cfgSettingEntity) || Objects.isNull(cfgSettingEntity.getDataJson())){
            log.error("====无生成系统配置数据====");
            return;
        }

        CfgSettingValueDTO.ReconciliationCycleDTO dto = BeanUtil.toBean(
                cfgSettingEntity.getDataJson(),
                CfgSettingValueDTO.ReconciliationCycleDTO.class
        );
        if(Objects.isNull(dto)){
            log.error("====CfgSettingValueDTO.ReconciliationCycleDTO为空====");
            return;
        }

        tryGenAutoTask("头程对账单", () -> generateFirstMileReconciliation(dto));
        tryGenAutoTask("报关对账", () -> generateTmsB2cDeclareReconciliation(dto));
        tryGenAutoTask("头程费用分摊", () -> generateFirstMileCostAllocation(dto));
        tryGenAutoTask("小包费用分摊", () -> generateSmallBagCostAllocation(dto));
        tryGenAutoTask("中转费用分摊", () -> generateTransferDeclareCostAllocation(dto));

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("====结束自动生成tms异步任务  执行时间：【{}】ms====", totalDuration);
    }

    /**
     * 各子任务生成失败相互隔离，不影响其他任务
     */
    private void tryGenAutoTask(String taskName, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("[自动生成任务] {} 生成异常: {}", taskName, e.getMessage(), e);
        }
    }


    //中转分摊
    private void generateTransferDeclareCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getPackageAllocationType())) {
            Integer transferAllocationDate = dto.getTransferAllocationDate();
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(transferAllocationDate, maxDay); // 取较小值
            String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //根据任务执行时间，推算出当时的时间范围
            LocalDateTime taskStartTime = DateUtil.parse(startTimeStr).toLocalDateTime();
            startTime = taskStartTime.minus(1, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            endTime = taskStartTime.minus(0, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode();

            Boolean isExist = isExist(businessType, startTimeStr);
            if (!isExist) {
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                pushDTO.setStartTime(startTime);
                pushDTO.setEndTime(endTime);
                pushDTO.setBusinessType(businessType);
                pushDTO.setMethodType(TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode());
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                selfServer.addAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), jsonStr, startTimeStr);
            }
        }else {
            log.error("[生成中转费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getPackageAllocationType());
        }
    }

    //小包分摊
    private void generateSmallBagCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getPackageAllocationType())) {
            Integer packageAllocationDate = dto.getPackageAllocationDate();
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(packageAllocationDate, maxDay); // 取较小值
            LocalDate localDate = now.withDayOfMonth(day);
            String startTimeStr = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //根据任务执行时间，推算出当时的时间范围
            LocalDateTime taskStartTime = DateUtil.parse(startTimeStr).toLocalDateTime();
            startTime = taskStartTime.minus(1, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            endTime = taskStartTime.minus(0, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            String businessType = TmsAsyncTaskRecordBusinessTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();

            Boolean isExist = isExist(businessType, startTimeStr);
            if (!isExist) {
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                pushDTO.setStartTime(startTime);
                pushDTO.setEndTime(endTime);
                pushDTO.setBusinessType(businessType);
                pushDTO.setReportDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                pushDTO.setMethodType(TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode());
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                selfServer.addAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), jsonStr, startTimeStr);
            }
        }else {
            log.error("[生成小包费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getPackageAllocationType());
        }
    }

    //报关对账单
    private void generateTmsB2cDeclareReconciliation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
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
            declareReconciliationDate = dto.getDeclareReconciliationDate().intValue();

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
            log.error("[生成报关对账] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getDeclareReconciliationType());
            return;
        }
        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode();
        //根据生成日期作为任务的开始时间
        Boolean isExist = isExist(businessType, startTimeStr);
        if (!isExist) {
            //创建当月的自动任务
            TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
            pushDTO.setStartDate(startDate);
            pushDTO.setEndDate(endDate);
            pushDTO.setBusinessType(businessType);
            String jsonStr = JSONUtil.toJsonStr(pushDTO);
            selfServer.addAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), jsonStr, startTimeStr);
        }
    }

    //头程对账单
    private void generateFirstMileReconciliation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
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
            firstMileReconciliationDate = dto.getFirstMileReconciliationDate();

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
            log.error("[生成头程对账单] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getFirstMileReconciliationType());
            return;
        }
        String businessType = TmsAsyncTaskRecordBusinessTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode();

        //根据生成日期作为任务的开始时间
        Boolean isExist = isExist(businessType, startTimeStr);
        if (!isExist) {
            //创建当月的自动任务
            TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
            pushDTO.setStartDate(startDate);
            pushDTO.setEndDate(endDate);
            pushDTO.setBusinessType(businessType);
            String jsonStr = JSONUtil.toJsonStr(pushDTO);
            selfServer.addAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), jsonStr, startTimeStr);
        }
    }

    //头程分摊
    private void generateFirstMileCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate reportPeriodMonth = null;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileAllocationType())) {
            String businessType = TmsAsyncTaskRecordBusinessTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode();
            //生成日期1-31
            Integer firstMileAllocationDate = dto.getFirstMileAllocationDate();
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(firstMileAllocationDate, maxDay); // 取较小值
            String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Boolean isExist = isExist(businessType, startTimeStr);
            if (!isExist) {
                //根据任务执行时间，推算出当时的时间
                LocalDate taskStartDate = LocalDate.parse(startTimeStr);
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                reportPeriodMonth = taskStartDate.minusMonths(1).withDayOfMonth(1);
                pushDTO.setReportDate(reportPeriodMonth.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM")));
                pushDTO.setBusinessType(businessType);
                pushDTO.setMethodType(TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode());
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                selfServer.addAutoTask(businessType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode(), jsonStr, startTimeStr);
            }
        } else {
            log.error("[生成头程费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getFirstMileAllocationType());
        }
    }


}
