package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.task.mapper.PlatformApiTaskMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class PlatformApiTaskServiceImpl extends ServiceImpl<PlatformApiTaskMapper, PlatformApiTaskEntity>
        implements PlatformApiTaskService {

    /**
     * 修改任务下次执行
     * @Author Luo_WG
     * @Date 2022/11/15 10:24
     * @param jobTaskDTO jobTaskDTO
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateTaskStateById(JobTaskDTO jobTaskDTO){
        Integer interval = jobTaskDTO.getIntervalTime();
        LocalDateTime lastTime = jobTaskDTO.getLastTime();
        LocalDateTime nextTime = lastTime.plusSeconds(interval);
        LambdaUpdateWrapper<PlatformApiTaskEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getLastTime, jobTaskDTO.getLastTime());
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getNextTime, nextTime);
        if(3 != jobTaskDTO.getState()){
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getState, 1);
        }
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getUpdateTime, LocalDateTime.now());
        lambdaUpdateWrapper.eq(PlatformApiTaskEntity::getId, jobTaskDTO.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public PlatformApiTaskEntity getByApiCode(String taskName) {
        return lambdaQuery().eq(PlatformApiTaskEntity::getApiCode, taskName)
                .eq(PlatformApiTaskEntity::getState, 3)
                .oneOpt().orElse(null);
    }
}
