package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.task.mapper.PlatformApiTaskMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PlatformApiTaskServiceImpl extends ServiceImpl<PlatformApiTaskMapper, PlatformApiTaskEntity>
        implements PlatformApiTaskService {

    /**
     * 修改任务下次执行
     *
     * @param jobTaskDTO jobTaskDTO
     * @param type
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     **/
    @Override
    public Boolean updateTaskStateById(JobTaskDTO jobTaskDTO, Integer type){
        LambdaUpdateWrapper<PlatformApiTaskEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        if (1 != type){
            Integer interval = jobTaskDTO.getIntervalTime();
            LocalDateTime lastTime = jobTaskDTO.getLastTime();
            LocalDateTime nextTime = lastTime.plusSeconds(interval);
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getLastTime, jobTaskDTO.getLastTime());
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getNextTime, nextTime);
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryCount, 0);
        }else {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryCount, jobTaskDTO.getRetryCount() + 1);
        }
        if(3 != type){
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getState, 1);
        }
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getUpdateTime, LocalDateTime.now());
        lambdaUpdateWrapper.eq(PlatformApiTaskEntity::getId, jobTaskDTO.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public PlatformApiTaskEntity getByApiCode(String taskName) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getApiCode, taskName)
                .le(PlatformApiTaskEntity::getLastTime, LocalDateTime.now())
                .eq(PlatformApiTaskEntity::getState, 3)
                .one();
    }
}
