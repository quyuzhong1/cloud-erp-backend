package com.erp.server.dmp.push.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.erp.model.dmp.constant.TaskConstant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.thread.PullKingdeeDateThread;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/12 15:19
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeeProductDetailJob {

    @Autowired
    private RedisTemplate<String, String> template;

    @Resource
    private PullKingdeeDateThread pullKingdeeDateThread;


}
