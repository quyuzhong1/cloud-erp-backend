package com.erp.server.tms.schedule;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
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

/**
 * @author jack
 * @ClassName GenerateAutoTaskALLJob
 * @description: 生成Tms异步任务
 * @date 2026-01-30
 */
@Component
@Slf4j
@EnableScheduling
public class GenerateAutoTaskALLJob {

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    /**
     *
     *异步任务状态更新
     * @return
     */
    @XxlJob("GenerateAutoTaskALLJob")
    public ReturnT<String> AutoGenAsyncTaskJob() {
        XxlJobHelper.log("====开始自动生成tms异步任务====");
        asyncTaskRecordService.generateAutoTaskALL();
        XxlJobHelper.log("====结束自动生成tms异步任务====");
        return ReturnT.SUCCESS;

    }
}
