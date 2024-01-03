package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.PlatformApiEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.service.PlatformApiService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TbTaskTypeService {
    @Resource
    private PlatformApiService platformApiService;
    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    private Long timeoutSeconds;

    private Long timeoutMabangHours;

    @Value("${openApi.mabang.timeoutHour:24}")
    public void setTimeoutMabangHours(Long timeoutMabangHours) {
        this.timeoutMabangHours = timeoutMabangHours;
    }

    @Value("${openApi.timeoutSeconds:3600}")
    public void setTimeoutSeconds(Long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 定时查询需要拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/8 17:32
     * @return java.util.List<com.erp.server.entity.PlatformApiTaskEntity>
     **/
    public List<JobTaskDTO> getTask() {
        LocalDateTime localTime = LocalDateTime.now();
        // 查询任务列表
        List<JobTaskDTO> jobTaskDTOList = platformApiTaskService.listApiTask(localTime, "pull");
        // 任务量等于0，任务重新开始,分页设置成0
        if (CollectionUtil.isEmpty(jobTaskDTOList)) {
            return jobTaskDTOList;
        }
        // 设置超时恢复状态
        List<JobTaskDTO> inProgressList = jobTaskDTOList.stream()
            .filter(task -> 1 == task.getStatus())
            .collect(Collectors.toList());
        List<JobTaskDTO> timeoutList = jobTaskDTOList.stream()
            .filter(task -> 2 == task.getStatus())
            .filter(task -> {
                LocalDateTime nextTime = task.getNextTime();
                LocalDateTime updateTime = task.getUpdateTime();
                if (task.getApiName().contains(TaskConstant.MABANG)) {
                    return localTime.isAfter(nextTime.plusHours(timeoutMabangHours))
                            && localTime.isAfter(updateTime.plusHours(timeoutMabangHours));
                } else {
                    // 检查和获取默认时间
                    Long currentTimeoutSeconds = task.getAndCheckTimeoutSeconds(timeoutSeconds);
                    return localTime.isAfter(nextTime.plusSeconds(currentTimeoutSeconds))
                            && localTime.isAfter(updateTime.plusSeconds(currentTimeoutSeconds));
                }
            }).collect(Collectors.toList());
        // 需要设置超时恢复的任务
        if (CollectionUtil.isNotEmpty(timeoutList)){
            platformApiTaskService.updateTaskTypeState(timeoutList, 1);
        }
        // 设置任务正在执行中
        if (CollectionUtil.isNotEmpty(inProgressList)){
            platformApiTaskService.updateTaskTypeState(inProgressList, 2);
        }
        return inProgressList;
    }

    /**
     * 定时添加平台任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:49
     * @return void
     **/
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void addTask(ShopInfoEntity shopInfo) {
        // 查询需要当前平台需要增加的任务
        platformApiTaskService.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(),shopInfo.getDictPlatform()));
        // 添加完成后，修改店铺生成任务状态
        Boolean result = shopInfoFeign.updateShopInfoById(new ShopInfoEntity(shopInfo.getId(), Boolean.TRUE));
    }



}
