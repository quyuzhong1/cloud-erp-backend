package com.erp.server.oms.schedule;

import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.server.oms.service.InvoiceInfoService;
import com.erp.server.oms.service.PackagePlanService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 组包计划job
 */
@Component
@Slf4j
public class PackagePlanJob {

    @Resource
    private PackagePlanService packagePlanService;


    /**
     * 获取组包计划的交接标签
     * - 组包状态为：等于已组包
     * - 交接标签下载：等于未下载
     * @return
     */

    @XxlJob("getHandoverLabelJob")
    public ReturnT<String> getHandoverLabel() {
        XxlJobHelper.log("获取组包计划的交接标签开始执行");
        //无交接标签 有物流跟踪号 平台是 wildberries 组包状态是已组包
        String jobParam = XxlJobHelper.getJobParam();
        XxlJobHelper.log("获取组包计划的交接标签开始执行，参数为：{}", jobParam);
        List<String> codeList = Arrays.asList(jobParam.split(","));
        List<PackagePlanDTO.LabelDTO> list = packagePlanService.getNoHandoverLabel(codeList);
        XxlJobHelper.log("获取组包计划的交接标签开始执行，需要获取的订单数：{}", list.size());
        list.forEach(item -> {
            XxlJobHelper.log("获取组包计划的交接标签开始执行，订单号：{}", item.getCode());
            try {
                packagePlanService.downloadHandoverLabel(item);
            }catch (Exception e){
                XxlJobHelper.log("获取组包计划的交接标签执行失败，异常：{}", e.getMessage());
            }
            XxlJobHelper.log("获取组包计划的交接标签完成执行，订单号：{}", item.getCode());
        });
        XxlJobHelper.log("获取组包计划的交接标签执行结束");
        return ReturnT.SUCCESS;
    }

}
