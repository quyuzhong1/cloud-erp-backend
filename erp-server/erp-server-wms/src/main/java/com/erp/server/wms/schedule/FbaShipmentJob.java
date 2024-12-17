package com.erp.server.wms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.server.wms.service.FbaTransitCalculateReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName FbaShipmentJob
 * @description: FBA核算任务
 * @date 2024年12月12日
 * @version: 1.0
 */
@Slf4j
@Component
public class FbaShipmentJob {
    @Resource
    private FbaTransitCalculateReportService fbaTransitCalculateReportService;

    /**
     * 自动计算FBA在途报表
     * 每月1号计算上月报表
     */
    @XxlJob("autoCalculateFbaShipment")
    public void autoCalculateFbaShipment() {
        XxlJobHelper.log("====开始自动计算FBA在途报表====");
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数={}", jobParam);
        LocalDate reportMonth = null;
        if (StringUtils.isNotBlank(jobParam)) {
            JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            reportMonth = LocalDate.parse(jsonObject.getStr("reportMonth"));
        }
        if (Objects.isNull(reportMonth)){
            //上个月的月份
            reportMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        }
        fbaTransitCalculateReportService.autoCalculateFbaShipment(reportMonth);
        XxlJobHelper.log("====结束自动计算FBA在途报表====");
    }


}
