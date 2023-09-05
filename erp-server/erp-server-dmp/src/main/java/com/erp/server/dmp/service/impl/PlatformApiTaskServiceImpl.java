package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.server.dmp.mapper.PlatformApiTaskMapper;
import com.erp.server.dmp.service.PlatformApiTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlatformApiTaskServiceImpl extends SuperServiceImpl<PlatformApiTaskMapper, PlatformApiTaskEntity>
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
            LocalDateTime nextTime = jobTaskDTO.getNextTime();
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getLastTime, nextTime);
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getNextTime, nextTime.plusSeconds(interval));
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, 0);
        }else {
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getRetryTimes, jobTaskDTO.getRetryTimes() + 1);
        }
        if(3 != type){
            lambdaUpdateWrapper.set(PlatformApiTaskEntity::getStatus, 1);
        }
        lambdaUpdateWrapper.set(PlatformApiTaskEntity::getUpdateTime, LocalDateTime.now());
        lambdaUpdateWrapper.eq(PlatformApiTaskEntity::getId, jobTaskDTO.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Override
    public PlatformApiTaskEntity getByApiCode(String taskName) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getApiCode, taskName)
                .le(PlatformApiTaskEntity::getNextTime, LocalDateTime.now())
                .eq(PlatformApiTaskEntity::getStatus, 3)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createPlatformTask(PlatformTaskDTO.AddDTO dto) {
        // 根据店铺id查询是否已经存在任务
        List<PlatformApiTaskEntity> taskEntity = lambdaQuery()
                .eq(PlatformApiTaskEntity::getShopId, dto.getShopId())
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .eq(PlatformApiTaskEntity::getDisabled, Boolean.FALSE)
                .list();
        // 如果存在任务，删除任务
        if(CollectionUtil.isNotEmpty(taskEntity)){
            removeByIds(taskEntity.stream().map(PlatformApiTaskEntity::getId).collect(Collectors.toList()));
        }
        // 根据平台code查询需要添加的任务
        List<PlatformApiTaskEntity> taskList = lambdaQuery()
                .eq(PlatformApiTaskEntity::getDictPlatform, dto.getDictPlatform())
                .eq(PlatformApiTaskEntity::getDisabled, Boolean.FALSE)
                .list();
        // 添加平台任务记录，时间为当前时间，下次执行时间为当前时间加上间隔时间，状态为待执行
        if(CollectionUtil.isEmpty(taskList)){
            return Boolean.TRUE;
        }
        List<PlatformApiTaskEntity> collect = taskList.stream().map(task -> {
            PlatformApiTaskEntity entity = new PlatformApiTaskEntity();
            entity.setShopId(dto.getShopId());
            entity.setDictPlatform(dto.getDictPlatform());
            entity.setApiCode(task.getApiCode());
            entity.setApiName(task.getApiName());
            entity.setIntervalTime(task.getIntervalTime());
            entity.setLastTime(LocalDateTime.now());
            entity.setNextTime(LocalDateTime.now().plusSeconds(task.getIntervalTime()));
            entity.setStatus(1);
            entity.setRetryTimes(0);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        return null;
    }

    @Override
    public List<PlatformApiTaskEntity> listByPlatformAndShop(String dictPlatform, String shopId) {
        return lambdaQuery()
                .eq(PlatformApiTaskEntity::getDictPlatform, dictPlatform)
                .eq(PlatformApiTaskEntity::getShopId, shopId)
                .list();
    }

    @Override
    public List<JobTaskDTO> listApiTask(LocalDateTime localTime) {
        return baseMapper.selectApiTask(localTime);
    }

    @Override
    public void updateTaskTypeState(List<JobTaskDTO> timeoutList, int type) {
        baseMapper.updateTaskTypeState(timeoutList, type);
    }
}
