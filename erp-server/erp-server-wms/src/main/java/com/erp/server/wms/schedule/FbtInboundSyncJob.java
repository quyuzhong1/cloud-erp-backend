package com.erp.server.wms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.server.wms.service.FbtInboundService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class FbtInboundSyncJob {

    private static final DateTimeFormatter JOB_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private FbtInboundService fbtInboundService;

    @XxlJob("fbtInventoryRecordSyncJob")
    public void syncInventoryRecords() {
        String jobParam = XxlJobHelper.getJobParam();
        Long startTime = null;
        Long endTime = null;
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            try {
                JSONObject jsonObject = JSONUtil.parseObj(jobParam);
                startTime = parseJobTime(jsonObject.getStr("startTime"));
                endTime = parseJobTime(jsonObject.getStr("endTime"));
            } catch (Exception e) {
                XxlJobHelper.log("FBT库存记录任务参数格式错误，jobParam={}", jobParam);
                log.warn("FBT库存记录任务参数格式错误, jobParam={}", jobParam, e);
            }
        }
        long start = System.currentTimeMillis();
        fbtInboundService.syncRecentInventorySnapshots();
        fbtInboundService.syncRecentInboundOrders(startTime, endTime);
        XxlJobHelper.log("FBT库存快照+流水定时同步结束(Search FBT Inventory + Record), startTime={}, endTime={}, costMs={}",
                startTime, endTime, System.currentTimeMillis() - start);
    }

    private Long parseJobTime(String timeText) {
        if (CharSequenceUtil.isBlank(timeText)) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timeText, JOB_TIME_FORMATTER);
            return localDateTime.toEpochSecond(ZoneOffset.ofHours(8));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间参数格式错误，需为yyyy-MM-dd HH:mm:ss, value=" + timeText, e);
        }
    }
}
