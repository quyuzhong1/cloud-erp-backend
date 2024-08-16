package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.DmpTaskMsgDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 中台发送MQFeign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/

@Slf4j
@RestController
@RequestMapping("feign/dmp/")
public class DmpTaskFeignController {
    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @PostMapping("/createPlatformTask")
    public Boolean createPlatformTask(@RequestBody @Valid PlatformTaskDTO.AddDTO dto){
        return platformApiTaskService.createOrEnablePlatformTask(dto);
    }

    @PostMapping("/removePlatformTask")
    public Boolean removePlatformTask(@RequestBody @Valid PlatformTaskDTO.AddDTO dto){
        return platformApiTaskService.removePlatformTask(dto);
    }

    @PostMapping("/disabledPlatformTask")
    public Boolean disabledPlatformTask(PlatformTaskDTO.DisabledDTO disabledDTO){
        return platformApiTaskService.disabledPlatformTask(disabledDTO);
    }

    @PostMapping("/createThirdWarehouseTask")
    public Boolean createThirdWarehouseTask(@RequestBody @Valid ThirdWarehouseTaskDTO.AddDTO dto){
        return platformApiTaskService.createThirdWarehouseTask(dto);
    }

    @PostMapping("/getPullTaskById")
    public DmpPullTaskEntity getPullTaskById(@RequestBody String id){
        return dmpPullTaskService.getById(id);
    }

    /**
     * 更新任务禁用/启用和取消/开启报告计划
     *
     */
    @PostMapping("/allAddOrUpdateTaskAndSchedule")
    public Boolean allAddOrUpdateTaskAndSchedule(@RequestBody @Valid PlatformTaskDTO.DisabledDTO dto){
        return platformApiTaskService.allAddOrUpdateTaskAndSchedule(dto);
    }
    /**
     * 获取飞书预警信息需要推送的(PushTask任务记录)
     * @return
     */
    @PostMapping("/getWarnPushTaskList")
    public List<DmpPushTaskEntity> getWarnPushTaskList(@RequestBody List<String> statusList){
        return dmpPushTaskService.getWarnPushTaskList(statusList);
    }

    /**
     * 获取飞书预警信息需要推送的(PullTask任务记录)
     * @return
     */
    @PostMapping("/getWarnPullTaskList")
    public List<DmpPullTaskEntity> getWarnPullTaskList(@RequestBody List<String> statusList){
        return dmpPullTaskService.getWarnPullTaskList(statusList);
    }
    /**
     * 获取飞书预警信息需要推送的(Task汇总报告)
     * @return
     */
    @PostMapping("/getWarnTaskReport")
    public List<DmpTaskMsgDTO> getWarnTaskReport(@RequestBody List<String> statusList){
        return dmpPushTaskService.getWarnTaskReport(statusList);
    }

    /**
     * 根据来源ID查询
     * @param sourceIdList
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    @PostMapping("feign/dmp/listBySourceIds")
    List<DmpPushTaskEntity> listBySourceIds(List<String> sourceIdList){
        return dmpPushTaskService.lambdaQuery().in(DmpPushTaskEntity::getSourceId, sourceIdList).list();
    }

    @PostMapping("/push/deleteBySourceId")
    void deletePushTaskBySourceId(String sourceId){
        dmpPushTaskService.lambdaUpdate().eq(DmpPushTaskEntity::getSourceId, sourceId).remove();
    }
}
