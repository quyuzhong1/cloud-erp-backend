package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.TaskConstant;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.PlatformApiService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private CfgSettingService cfgSettingService;

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
        List<JobTaskDTO> sourcejobTaskDTOList = platformApiTaskService.listApiTask(localTime, "pull");
        // 获取延时配置Map<apiCode, 延时秒数>
        Map<String, Integer> delayConfgMap = cfgSettingService.getApiTaskDelaySecond(SettingEnum.PLATFORM_API_TASK_DELAY_SECOND);
        // 过滤小于延时时间的任务
        List<JobTaskDTO> jobTaskDTOList = sourcejobTaskDTOList.stream()
                .filter(e -> {
                    // 执行中跳过校验
                    if (2 == e.getStatus()){
                        return true;
                    }
                    Integer delaySecond = delayConfgMap.get(e.getApiCode());
                    if (null == delaySecond){
                        // 配置不存在跳过
                        return true;
                    } else {
                        // 下次执行时间 + 延时时间 <= 当前时间
                        return !e.getNextTime().plusSeconds(delaySecond).isAfter(localTime);
                    }
                }).collect(Collectors.toList());

        // 任务量等于0，任务重新开始,分页设置成0
        if (CollectionUtil.isEmpty(jobTaskDTOList)) {
            return Collections.emptyList();
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
            // 移除缓存已有的数据
            for (JobTaskDTO jobTaskDTO : timeoutList) {
                redisTemplate.boundListOps(jobTaskDTO.getGroupId()).remove(0, JSONObject.toJSONString(jobTaskDTO));
            }
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
        platformApiTaskService.createOrEnablePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(),shopInfo.getName(),shopInfo.getDictPlatform()));
        // 添加完成后，修改店铺生成任务状态
        Boolean result = shopInfoFeign.updateShopInfoById(new ShopInfoEntity(shopInfo.getId(), Boolean.TRUE));
    }



}
