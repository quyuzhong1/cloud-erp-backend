package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.JobTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
            JobTaskDTO jobTask = new JobTaskDTO(tbTask);
//            redisTemplate.boundListOps(tbTask.getDictPlatform()).leftPush(JSONObject.toJSONString(jobTask));
            String groupId = tbTask.getGroupId();
            if (StringUtils.isBlank(groupId)){
                groupId = tbTask.getDictPlatform();
            }
            if (StringUtils.isBlank(groupId)){
                log.error("任务无groupId和tDictPlatform, 无法触发:task={}", JSONUtil.toJsonStr(tbTask));
                continue;
            }
            // 移除原有相同的参数(空数组正常执行)
            redisTemplate.boundListOps(groupId).remove(0, JSONObject.toJSONString(jobTask));

            // 将当前参数添加或重新添加
            redisTemplate.boundListOps(groupId).leftPush(JSONObject.toJSONString(jobTask));
        }
    }
}
