package com.erp.server.dmp.task.service;

import com.erp.server.dmp.entity.dmp.PlatformApiEntity;
import com.erp.server.dmp.entity.dmp.PlatformApiTaskEntity;
import com.erp.server.dmp.entity.dto.JobTaskDTO;
import com.erp.server.dmp.task.mapper.PlatformApiMapper;
import com.erp.server.dmp.task.mapper.PlatformApiTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TbTaskTypeService {
    @Resource
    private PlatformApiMapper platformApiMapper;

    @Resource
    private PlatformApiTaskMapper platformApiTaskMapper;

    private final Integer pageSize = 100;

    private static Integer pageNumber = 1;

    /**
     * 定时查询需要拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/8 17:32
     * @return java.util.List<com.erp.server.entity.PlatformApiTaskEntity>
     **/
    public List<JobTaskDTO> getTask() {
        Long localTime = System.currentTimeMillis() / 1000;
        // 查询任务列表
        List<JobTaskDTO> jobTaskDTOList = platformApiTaskMapper.selectApiTask((pageNumber - 1), pageSize, localTime);
        pageNumber++;
        if (jobTaskDTOList == null || jobTaskDTOList.isEmpty()) { // 任务量等于0，任务重新开始,分页设置成0
            pageNumber = 1;
            return null;
        }
        // 设置任务正在执行中
        platformApiTaskMapper.updateTaskTypeState(jobTaskDTOList);
        return jobTaskDTOList;
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
                    taskType.setLastTime(0);
                    taskType.setNextTime(0);
                    taskType.setCreateTime(new Date());
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
