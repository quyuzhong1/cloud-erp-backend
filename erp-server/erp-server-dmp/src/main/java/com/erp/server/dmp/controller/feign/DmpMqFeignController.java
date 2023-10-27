package com.erp.server.dmp.controller.feign;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
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
public class DmpMqFeignController {
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    /**
     * 发送MQ消息并保存任务
     * @param dto
     * @return
     */
    @PostMapping("/send/mq/save/task")
    public Boolean sendMqAndSaveTask(@RequestBody @Valid DmpPushTaskFeignDTO dto){
        dmpPushTaskService.sendMqAndSaveTask(dto);
        return Boolean.TRUE;
    }

    /**
     * 根据单个id查询推送任务
     * @param oneDTO
     * @return
     */
    @PostMapping("/getByParam")
    public DmpPushTaskEntity getByParam(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO){
        DmpPushTaskEntity dmpPushTaskEntity = dmpPushTaskService.getByParam(oneDTO);
        return ObjectUtils.isEmpty(dmpPushTaskEntity) ? new DmpPushTaskEntity() : dmpPushTaskEntity;
    }

    /**
     * 根据多个id查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("/listByParam")
    public List<DmpPushTaskEntity> listByParam(@RequestBody @Valid DmpSyncTaskDTO.ListDTO listDTO){
        List<DmpPushTaskEntity> list = dmpPushTaskService.listByParam(listDTO);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }
}
