package com.erp.server.dmp.task.schedule;

import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.task.service.CreateRequestReportTaskService;
import com.erp.server.dmp.task.service.TbTaskTypeService;
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

    /**
     * 定时扫描需要创建拉取任务拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     **/
    //@Scheduled(cron = "0/20 * * * * ?")
    public void createOrderJob() {
        List<JobTaskDTO> list = tbTaskTypeService.getTask();
        if(list != null){
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
    public void addShopTask() {
         tbTaskTypeService.addTask();
    }
}
