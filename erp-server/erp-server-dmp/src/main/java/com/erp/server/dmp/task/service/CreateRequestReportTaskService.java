package com.erp.server.dmp.task.service;

import com.alibaba.fastjson2.JSONObject;
import com.erp.model.dmp.dto.JobTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CreateRequestReportTaskService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 创建请求任务到队列中去
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     * @param list 任务信息集合
     **/
    public void addTaskToQueue(List<JobTaskDTO> list) {
        for (JobTaskDTO tbTask : list) {
            JobTaskDTO jobTask = new JobTaskDTO();
            jobTask.setId(tbTask.getId());
            jobTask.setPlatformId(tbTask.getPlatformId());
            jobTask.setApiId(tbTask.getApiId());
            jobTask.setIntervalTime(tbTask.getIntervalTime());
            jobTask.setLastTime(tbTask.getLastTime());
            jobTask.setNextTime(tbTask.getNextTime());
            jobTask.setState(tbTask.getState());
            jobTask.setApiCode(tbTask.getApiCode());
            jobTask.setApiName(tbTask.getApiName());
            jobTask.setTaskName(tbTask.getTaskName());
            redisTemplate.boundListOps(tbTask.getTaskName()).leftPush(JSONObject.toJSONString(jobTask));
        }
    }
}
