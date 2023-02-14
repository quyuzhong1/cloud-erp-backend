package com.erp.server.dmp.task.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpRefundInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpReturnOrderInfoService;
import com.erp.server.dmp.task.service.CreateRequestReportTaskService;
import com.erp.server.dmp.task.service.TbTaskTypeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class PullTaskCreateJob {

    @Resource
    TbTaskTypeService tbTaskTypeService;

    @Resource
    CreateRequestReportTaskService reportTaskService;

    @Resource
    DmpOrderInfoService dmpOrderInfoService;

    @Resource
    DmpReturnOrderInfoService dmpReturnOrderInfoService;

    @Resource
    DmpRefundInfoService dmpRefundInfoService;

    /**
     * 定时扫描需要创建拉取任务拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     **/
    //@Scheduled(cron = "0/20 * * * * ?")
    @XxlJob("createOrderJob")
    public void createOrderJob() {
        List<JobTaskDTO> list = tbTaskTypeService.getTask();
        if(CollectionUtil.isNotEmpty(list)){
            reportTaskService.addTaskToQueue(list);
        }
    }

    /**
     * 定时扫描需要添加到任务表的api接口
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     * @return void
     **/
    //@Scheduled(cron = "0/10 * * * * ?")
    @XxlJob("addShopTask")
    public ReturnT<String> addShopTask() {
        XxlJobHelper.log("addShopTask 任务开始执行");
        tbTaskTypeService.addTask();
        XxlJobHelper.log("addShopTask 任务开始完成");
        return ReturnT.SUCCESS;
    }


    /**
     * 清洗订单数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    //@Scheduled(cron = "0/10 * * * * ?")
    @XxlJob("cleanOrderTask")
    public ReturnT<String>  cleanOrderTask() {
        XxlJobHelper.log("cleanOrderTask 任务开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        Integer pageSize = StrUtil.isNotBlank(jobParam) ? Integer.valueOf(jobParam) : 100;
        dmpOrderInfoService.cleanOrder(pageSize);
        XxlJobHelper.log("cleanOrderTask 任务开始完成");
        return ReturnT.SUCCESS;
    }

    /**
     * 清洗退货数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    //@Scheduled(cron = "0/10 * * * * ?")
    @XxlJob("cleanReturnOrderTask")
    public ReturnT<String> cleanReturnOrderTask() {
        XxlJobHelper.log("cleanReturnOrderTask 任务开始执行");
        dmpReturnOrderInfoService.cleanReturnOrderTask();
        XxlJobHelper.log("cleanReturnOrderTask 任务开始完成");
        return ReturnT.SUCCESS;
    }


    /**
     * 清洗退款数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    //@Scheduled(cron = "0/10 * * * * ?")
    @XxlJob("cleanRefundTask")
    public ReturnT<String> cleanRefundTask() {
        XxlJobHelper.log("cleanRefundTask 任务开始执行");
        dmpRefundInfoService.cleanRefundTask();
        XxlJobHelper.log("cleanRefundTask 任务开始完成");
        return ReturnT.SUCCESS;
    }
}
