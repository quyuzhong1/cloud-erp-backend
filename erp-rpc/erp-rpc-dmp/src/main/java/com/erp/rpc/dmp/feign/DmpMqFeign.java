package com.erp.rpc.dmp.feign;


import com.common.business.dto.DmpPushTaskFeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author Will
 * @description: DMP远程调用接口
 * @date: 2023/1/12 16:54
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp/", contextId = "DmpMqFeign")
public interface DmpMqFeign {

    /**
     * 发送MQ消息并保存任务
     * @param dto
     * @return
     */
    @PostMapping("send/mq/save/task")
    Boolean sendMqAndSaveTask(@RequestBody @Valid DmpPushTaskFeignDTO dto);

}