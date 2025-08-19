package com.erp.server.wms.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.server.wms.service.VirtualInventoryAgeService;
import com.erp.server.wms.service.VirtualInventoryDetailHisService;
import com.erp.server.wms.service.WmsVirtualDetailMsgService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 虚拟仓任务
 * @author will
 * @date 2024/12/9 19:21
 */
@Component
@Slf4j
@EnableScheduling
public class WmsVirtualDetailMsgJob {
    @Resource
    private WmsVirtualDetailMsgService wmsVirtualDetailMsgService;

    @Resource
    private VirtualInventoryDetailHisService virtualInventoryDetailHisService;


    @Resource
    private VirtualInventoryAgeService virtualInventoryAgeService;

    /**
     * 虚拟仓明细任务
     * @author will
     * @date 2024/12/9 19:19
     * @return ReturnT<String>
     */
    @XxlJob("virtualDetailMsgJob")
    public ReturnT<String> virtualDetailMsgJob() {
        XxlJobHelper.log("=====自动执行生成虚拟仓批次流水 开始任务=====");
        long start = System.currentTimeMillis();
        wmsVirtualDetailMsgService.virtualDetailMsgJob();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====自动执行生成虚拟仓批次流水 结束任务=====");
        return ReturnT.SUCCESS;
    }

    /**
     * 历史虚拟仓保存任务
     * @author will
     * @date 2024/12/9 19:21
     * @return ReturnT<String>
     */
    @XxlJob("hisVirtualInventoryJob")
    public ReturnT<String> hisVirtualInventoryJob() {
        XxlJobHelper.log("=====自动执行生成虚拟仓流水结余 开始任务=====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        virtualInventoryDetailHisService.hisVirtualInventoryJob(jobParam);
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====自动执行生成虚拟仓流水结余 结束任务=====");
        return ReturnT.SUCCESS;
    }

    /**
     * 生成虚拟仓分析数据
     * @author will
     * @date 2025/8/19 16:22
     * @return ReturnT<String>
     */
    @XxlJob("virtualInventoryAgeJob")
    public ReturnT<String> virtualInventoryAgeJob() {
        XxlJobHelper.log("=====自动执行生成虚拟仓库龄分析数据 开始任务=====");
        long start = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();
        LocalDate date = LocalDate.now().minusDays(1L);
        if (CharSequenceUtil.isNotBlank(jobParam)) {
            date = LocalDate.parse(jobParam, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        virtualInventoryAgeService.generateVirtualInventoryAge(date);
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====自动执行生成虚拟仓库龄分析数据 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
