package com.cloud.erp.chrome.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.erp.chrome.constant.BusinessType;
import com.cloud.erp.chrome.constant.ErpPlatform;
import com.cloud.erp.chrome.dto.GyyParamDTO;
import com.cloud.erp.chrome.dto.GyySearchParamDTO;
import com.cloud.erp.chrome.dto.MabangOrderParamDTO;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自动调度创建任务
 * @Date 2022-08-25 16:37
 * @Dreated by yl
 */
@Component
@Slf4j
public class TaskJob {

    @Autowired
    private ChromeTaskInfoService  chromeTaskInfoService;

    /**
     * 生成马帮的任务  每天00:25
     */
    @XxlJob("addMabangTask")
    public void addMabangTask() {
        chromeTaskInfoService.createOrderTask(ErpPlatform.MABANG);
    }

    /**
     * 生成管易云的任务  每天 00:15
     */
    @XxlJob("addGyyTask")
    public void addGyyTask() {
        chromeTaskInfoService.createOrderTask(ErpPlatform.GYY);
    }

    /**
     * 生成云星空任务 每天00:05
     */
    @XxlJob("addYxkTask")
    public void addYxkTask() {
        chromeTaskInfoService.createOrderTask(ErpPlatform.YXK);
    }

    public static void main(String[] args) {
        DateTime yearStartDay = DateUtil.beginOfYear(new Date());
        DateTime yearStartTime=DateUtil.beginOfDay(yearStartDay);
        String yearStartTimeStr=yearStartTime.toString("yyyy-MM-dd HH:mm:ss");
        System.out.println(yearStartTimeStr);
    }
}
