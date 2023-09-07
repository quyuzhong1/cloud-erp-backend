package com.erp.server.dmp.controller.feign;

import com.common.business.dto.DmpPushTaskFeignDTO;
import com.erp.server.dmp.service.DmpPushTaskService;
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

}
