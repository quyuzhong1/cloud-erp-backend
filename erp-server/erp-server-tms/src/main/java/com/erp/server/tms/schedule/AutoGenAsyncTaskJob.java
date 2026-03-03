package com.erp.server.tms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author jack
 * @ClassName AutoGenAsyncTaskJob
 * @description: 生成Tms异步任务
 * @date 2026-01-30
 */
@Component
@Slf4j
@EnableScheduling
public class AutoGenAsyncTaskJob {

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;
    @Resource
    private CfgSettingService cfgSettingService;

    /**
     * 并行执行任务的线程池
     */
    private final Executor executor = Executors.newFixedThreadPool(5);
    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("AutoGenAsyncTaskJob")
    public ReturnT<String> AutoGenAsyncTaskJob() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("====开始自动生成tms异步任务====");

        try {
            // 查询系统配置
            CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
            if (Objects.isNull(cfgSettingEntity) || Objects.isNull(cfgSettingEntity.getDataJson())) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "无生成系统配置数据");
            }

            CfgSettingValueDTO.ReconciliationCycleDTO dto = BeanUtil.toBean(
                    cfgSettingEntity.getDataJson(),
                    CfgSettingValueDTO.ReconciliationCycleDTO.class
            );
            if (ObjectUtil.isEmpty(dto)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "配置数据转换失败");
            }

            // 并行执行所有任务
            CompletableFuture<TmsAsyncTaskRecordDTO.TaskResult> firstMileFuture = CompletableFuture
                    .supplyAsync(() -> executeTask("头程对账单", () -> genFirstMileReconciliation(dto)), executor)
                    .exceptionally(ex -> new TmsAsyncTaskRecordDTO.TaskResult("头程对账单", false, ex.getMessage(), 0));

            CompletableFuture<TmsAsyncTaskRecordDTO.TaskResult> declareFuture = CompletableFuture
                    .supplyAsync(() -> executeTask("报关对账单", () -> generateTmsB2cDeclareReconciliation(dto)), executor)
                    .exceptionally(ex -> new TmsAsyncTaskRecordDTO.TaskResult("报关对账单", false, ex.getMessage(), 0));

            CompletableFuture<TmsAsyncTaskRecordDTO.TaskResult> allocationFuture = CompletableFuture
                    .supplyAsync(() -> executeTask("头程费用分摊", () -> generateFirstMileCostAllocation(dto)), executor)
                    .exceptionally(ex -> new TmsAsyncTaskRecordDTO.TaskResult("头程费用分摊", false, ex.getMessage(), 0));

            CompletableFuture<TmsAsyncTaskRecordDTO.TaskResult> smallBagFuture = CompletableFuture
                    .supplyAsync(() -> executeTask("小包分摊", () -> generateSmallBagCostAllocation(dto)), executor)
                    .exceptionally(ex -> new TmsAsyncTaskRecordDTO.TaskResult("小包分摊", false, ex.getMessage(), 0));

            CompletableFuture<TmsAsyncTaskRecordDTO.TaskResult> transferFuture = CompletableFuture
                    .supplyAsync(() -> executeTask("中转分摊", () -> generateTransferDeclareCostAllocation(dto)), executor)
                    .exceptionally(ex -> new TmsAsyncTaskRecordDTO.TaskResult("中转分摊", false, ex.getMessage(), 0));

            // 等待所有任务完成
            List<TmsAsyncTaskRecordDTO.TaskResult> results = CompletableFuture.allOf(
                    firstMileFuture, declareFuture, allocationFuture, smallBagFuture, transferFuture
            ).thenApply(v -> Arrays.asList(
                    firstMileFuture.join(),
                    declareFuture.join(),
                    allocationFuture.join(),
                    smallBagFuture.join(),
                    transferFuture.join()
            )).join();

            // 统计执行结果
            long successCount = results.stream().filter(TmsAsyncTaskRecordDTO.TaskResult::isSuccess).count();
            long failCount = results.size() - successCount;
            long totalDuration = System.currentTimeMillis() - startTime;

            XxlJobHelper.log("==== 结束自动生成 tms 异步任务，总耗时:{}ms, 成功:{}, 失败:{} ====",
                    totalDuration, successCount, failCount);

            if (failCount > 0) {
                String errorMsg = results.stream()
                        .filter(r -> !r.isSuccess())
                        .map(r -> r.getName() + ": " + r.getErrorMsg())
                        .collect(Collectors.joining("; "));
                return new ReturnT<>(ReturnT.FAIL_CODE, "部分任务执行失败：" + errorMsg);
            }

            return ReturnT.SUCCESS;

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            XxlJobHelper.log("==== 自动生成 tms 异步任务异常终止，总耗时:{}ms, 错误:{}",
                    totalDuration, e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务执行异常：" + e.getMessage());
        }
    }

    /**
     * 执行任务并记录日志和耗时
     */
    private TmsAsyncTaskRecordDTO.TaskResult executeTask(String taskName, Runnable task) {
        long startTime = System.currentTimeMillis();
        try {
            XxlJobHelper.log("==== 开始自动生成{} ====", taskName);
            task.run();
            long duration = System.currentTimeMillis() - startTime;
            XxlJobHelper.log("==== 结束自动生成{}，耗时:{}ms ====", taskName, duration);
            return new TmsAsyncTaskRecordDTO.TaskResult(taskName, true, null, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            XxlJobHelper.log("==== 自动生成{}失败，耗时:{}ms, 错误:{}",
                    taskName, duration, e.getMessage(), e);
            return new TmsAsyncTaskRecordDTO.TaskResult(taskName, false, e.getMessage(), duration);
        }
    }


    /**
     * 任务执行器函数式接口
     */
    @FunctionalInterface
    private interface TaskExecutor {
        String taskName = "";

        void execute() throws Exception;
    }

    //中转分摊
    private void generateTransferDeclareCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getPackageAllocationType())) {
            Integer transferAllocationDate = dto.getTransferAllocationDate();
            LocalDateTime currentDateTime = LocalDateTime.now();
            startTime = currentDateTime.minus(1, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            endTime = currentDateTime.minus(0, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            String businessType = SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode();

            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(transferAllocationDate, maxDay); // 取较小值
            String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Boolean isExist = asyncTaskRecordService.isExist(businessType, startTimeStr);
            if (!isExist) {
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                pushDTO.setStartTime(startTime);
                pushDTO.setEndTime(endTime);
                pushDTO.setBusinessType(businessType);
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                asyncTaskRecordService.addAutoTask(businessType, jsonStr, startTimeStr);
            }
        }else {
            XxlJobHelper.log("[生成中转费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getFirstMileAllocationType());
        }
    }

    //小包分摊
    private void generateSmallBagCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getPackageAllocationType())) {
            Integer packageAllocationDate = dto.getPackageAllocationDate();
            LocalDateTime currentDateTime = LocalDateTime.now();
            startTime = currentDateTime.minus(1, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            endTime = currentDateTime.minus(0, ChronoUnit.MONTHS)
                    .withDayOfMonth(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            String businessType = SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode();

            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(packageAllocationDate, maxDay); // 取较小值
            String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Boolean isExist = asyncTaskRecordService.isExist(businessType, startTimeStr);
            if (!isExist) {
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                pushDTO.setStartTime(startTime);
                pushDTO.setEndTime(endTime);
                pushDTO.setBusinessType(businessType);
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                asyncTaskRecordService.addAutoTask(businessType, jsonStr, startTimeStr);
            }
        }else {
            XxlJobHelper.log("[生成小包费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getPackageAllocationType());
        }
    }

    //报关对账单
    private void generateTmsB2cDeclareReconciliation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        //生成日期1-31
        Integer declareReconciliationDate = MathUtil.ONE;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getDeclareReconciliationType())) {
            startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        } else if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileReconciliationType())) {
            declareReconciliationDate = dto.getDeclareReconciliationDate().intValue();
            endDate = LocalDate.now().minusDays(1);
            startDate = LocalDate.now().minusMonths(1);
        }else {
            XxlJobHelper.log("[生成头程对账单] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getDeclareReconciliationType());
            return;
        }
        String businessType = SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode();

        //根据生成日期作为任务的开始时间
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
        int day = Math.min(declareReconciliationDate, maxDay); // 取较小值
        String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Boolean isExist = asyncTaskRecordService.isExist(businessType, startTimeStr);
        if (!isExist) {
            //创建当月的自动任务
            TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
            pushDTO.setStartDate(startDate);
            pushDTO.setEndDate(endDate);
            pushDTO.setBusinessType(businessType);
            String jsonStr = JSONUtil.toJsonStr(pushDTO);
            asyncTaskRecordService.addAutoTask(businessType, jsonStr, startTimeStr);
        }
    }

    //头程对账单
    private void genFirstMileReconciliation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        //生成日期1-31
        Integer firstMileReconciliationDate = MathUtil.ONE;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(dto.getFirstMileReconciliationType())) {
            startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        } else if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileReconciliationType())) {
            firstMileReconciliationDate = dto.getFirstMileReconciliationDate();
            endDate = LocalDate.now().minusDays(1);
            startDate = LocalDate.now().minusMonths(1);
        }else {
            XxlJobHelper.log("[生成头程对账单] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getFirstMileReconciliationType());
            return;
        }
        String businessType = SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode();

        //根据生成日期作为任务的开始时间
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
        int day = Math.min(firstMileReconciliationDate, maxDay); // 取较小值
        String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Boolean isExist = asyncTaskRecordService.isExist(businessType, startTimeStr);
        if (!isExist) {
            //创建当月的自动任务
            TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
            pushDTO.setStartDate(startDate);
            pushDTO.setEndDate(endDate);
            pushDTO.setBusinessType(businessType);
            String jsonStr = JSONUtil.toJsonStr(pushDTO);
            asyncTaskRecordService.addAutoTask(businessType, jsonStr, startTimeStr);
        }
    }

    //头程分摊
    private void generateFirstMileCostAllocation(CfgSettingValueDTO.ReconciliationCycleDTO dto) {
        LocalDate reportPeriodMonth = null;
        //自然月生成
        if (ReconciliationTypeEnum.CREAT_BY_PERIOD.getCode().equals(dto.getFirstMileAllocationType())) {
            String businessType = SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode();
            //生成日期1-31
            Integer firstMileAllocationDate = dto.getFirstMileAllocationDate();
            //根据生成日期作为任务的开始时间
            LocalDate now = LocalDate.now();
            YearMonth yearMonth = YearMonth.from(now);
            int maxDay = yearMonth.lengthOfMonth(); // 获取当月最大天数
            int day = Math.min(firstMileAllocationDate, maxDay); // 取较小值
            String startTimeStr = now.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Boolean isExist = asyncTaskRecordService.isExist(businessType, startTimeStr);
            if (!isExist) {
                //创建当月的自动任务
                TmsAsyncTaskRecordDTO.PushParamsDTO pushDTO = new TmsAsyncTaskRecordDTO.PushParamsDTO();
                reportPeriodMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                pushDTO.setReportDate(reportPeriodMonth.withDayOfMonth(day).format(DateTimeFormatter.ofPattern("yyyy-MM")));
                pushDTO.setBusinessType(businessType);
                String jsonStr = JSONUtil.toJsonStr(pushDTO);
                asyncTaskRecordService.addAutoTask(businessType, jsonStr, startTimeStr);
            }
        } else {
            XxlJobHelper.log("[生成头程费用分摊] AutoGenAsyncTaskJob 任务结束: 生成类型【{}】不支持", dto.getFirstMileAllocationType());
        }
    }
}
