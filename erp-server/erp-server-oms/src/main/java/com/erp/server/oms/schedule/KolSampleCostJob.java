package com.erp.server.oms.schedule;

import com.erp.server.oms.service.KolSampleCostService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 寄样费用job
 */
@Component
@Slf4j
public class KolSampleCostJob {

    @Resource
    private KolSampleCostService kolSampleCostService;


    /**
     * KOL寄样费用数据
     * @author will
     * @date 2025/12/9 14:34
     * @return ReturnT<String>
     */
    @XxlJob("updateKolSampleCostJob")
    public ReturnT<String> updateKolSampleCostJob() {
        XxlJobHelper.log("更新KOL寄样费用数据开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        kolSampleCostService.updateKolSampleCostJob();
        XxlJobHelper.log("更新KOL寄样费用数据执行结束");
        return ReturnT.SUCCESS;
    }

}
