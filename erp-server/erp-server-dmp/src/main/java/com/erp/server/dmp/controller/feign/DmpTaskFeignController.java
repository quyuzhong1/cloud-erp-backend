package com.erp.server.dmp.controller.feign;

import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    @PostMapping("feign/dmp/allAddOrUpdateTaskAndSchedule")
    public Boolean allAddOrUpdateTaskAndSchedule(PlatformTaskDTO.DisabledDTO dto){
        return platformApiTaskService.allAddOrUpdateTaskAndSchedule(dto);
    }
}
