package com.erp.rpc.dmp.feign;


import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Will
 * @description: DMP远程调用接口
 * @date: 2023/1/12 16:54
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp/", contextId = "DmpMqFeign")
public interface DmpMqFeign {

    /**
     * 保存任务
     * @param dto
     * @return
     */
    @PostMapping("save/pushTask")
    DmpPushTaskEntity saveTask(@RequestBody @Valid DmpPushTaskFeignDTO dto);


    /**
     * @description: 发送MQ消息
     * @author Will
     * @date: 2024/5/14 14:22
     * @param list
     * @return String
     */
    @PostMapping("send/sendTask")
    Boolean sendTask(@RequestBody List<DmpPushTaskEntity> list);

    /**
     * 根据单个id查询推送任务
     * @param oneDTO
     * @return
     */
    @PostMapping("getByParam")
    DmpPushTaskEntity getByParam(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO);

    /**
     * 根据多个id查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("listByParam")
    List<DmpPushTaskEntity> listByParam(@RequestBody @Valid DmpSyncTaskDTO.ListDTO listDTO);

}