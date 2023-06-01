package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.erp.model.dmp.constant.TaskConstant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.server.dmp.mapper.PlatformApiMapper;
import com.erp.server.dmp.mapper.PlatformApiTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TbTaskTypeService {
    @Resource
    private PlatformApiMapper platformApiMapper;

    @Resource
    private PlatformApiTaskMapper platformApiTaskMapper;

    private Long timeoutSeconds;

    private Long timeoutMabangHours;

    @Value("${openApi.mabang.timeoutHour:26}")
    public void setTimeoutMabangHours(Long timeoutMabangHours) {
        this.timeoutMabangHours = timeoutMabangHours;
    }

    @Value("${openApi.timeoutSeconds:3600}")
    public void setTimeoutSeconds(Long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    private final Integer PAGE_SIZE = 100;

    private static  Integer PAGE_NUMBER = 1;

    /**
     * 定时查询需要拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/8 17:32
     * @return java.util.List<com.erp.server.entity.PlatformApiTaskEntity>
     **/
    public List<JobTaskDTO> getTask() {
        LocalDateTime localTime = LocalDateTime.now();
        // 查询任务列表
        List<JobTaskDTO> jobTaskDTOList = platformApiTaskMapper.selectApiTask((PAGE_NUMBER - 1), PAGE_SIZE, localTime);
        PAGE_NUMBER++;
        // 任务量等于0，任务重新开始,分页设置成0
        if (CollectionUtil.isEmpty(jobTaskDTOList)) {
            PAGE_NUMBER = 1;
            return jobTaskDTOList;
        }
        // 设置超时恢复状态
        List<JobTaskDTO> timeoutList = new ArrayList<>();
        List<JobTaskDTO> inProgressList = new ArrayList<>();
        for (JobTaskDTO jobTaskDTO : jobTaskDTOList) {
            if (1 == jobTaskDTO.getState()) {
                inProgressList.add(jobTaskDTO);
            }else if(2 == jobTaskDTO.getState()){
                // 马帮历史数据超时
                LocalDateTime nextTime = jobTaskDTO.getNextTime();
                LocalDateTime updateTime = jobTaskDTO.getUpdateTime();
                if (TaskConstant.MABANG_PULL_DATA_TASK.equals(jobTaskDTO.getTaskName())) {
                    if (localTime.isAfter(nextTime.plusHours(timeoutMabangHours)) && localTime.isAfter(updateTime.plusHours(timeoutMabangHours))) {
                        timeoutList.add(jobTaskDTO);
                    }
                }else {
                    if (localTime.isAfter(nextTime.plusSeconds(timeoutSeconds)) && localTime.isAfter(updateTime.plusSeconds(timeoutSeconds))) {
                        timeoutList.add(jobTaskDTO);
                    }
                }
            }
        }
        // 需要设置超时恢复的任务
        if (CollectionUtil.isNotEmpty(timeoutList)){
            platformApiTaskMapper.updateTaskTypeState(timeoutList, 1);
        }

        // 设置任务正在执行中
        if (CollectionUtil.isNotEmpty(inProgressList)){
            platformApiTaskMapper.updateTaskTypeState(inProgressList, 2);
        }
        return inProgressList;
    }

    /**
     * 定时添加平台任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:49
     * @return void
     **/
    @Transactional
    public void addTask() {
        try {
            List<PlatformApiEntity> platformApiEntities = platformApiMapper.selectPlatformApiNoTask();
            if (platformApiEntities != null && platformApiEntities.size() > 0) {
                List<PlatformApiTaskEntity> taskEntityList = new ArrayList<>();
                platformApiEntities.forEach(req -> {
                    PlatformApiTaskEntity taskType = new PlatformApiTaskEntity();
                    taskType.setIntervalTime(60 * 30);
                    taskType.setState(1);
                    taskType.setApiId(req.getId());
                    taskType.setLastTime(null);
                    taskType.setNextTime(null);
                    taskType.setCreateTime(LocalDateTime.now());
                    taskType.setPlatformId(req.getPlatformId());
                    taskType.setApiCode(req.getApiCode());
                    taskType.setApiName(req.getApiName());
                    taskEntityList.add(taskType);
                });
                platformApiTaskMapper.batchInsert(taskEntityList);
                platformApiMapper.updatePlatformApiState(taskEntityList);
            }

        } catch (Exception e) {
            log.error(" === 增加任务失败， 错误信息 = {}", e.getMessage());
        }
    }



}
