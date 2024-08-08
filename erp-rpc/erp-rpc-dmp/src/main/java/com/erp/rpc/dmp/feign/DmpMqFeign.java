package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdsDTO;
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
@FeignClient(value = "erp-dmp",path = "feign/dmp", contextId = "DmpMqFeign",configuration = {FeignErrorDecoder.class})
public interface DmpMqFeign {

    /**
     * 保存任务
     * @param dto
     * @return
     */
    @PostMapping("save/pushTask")
    DmpPushTaskEntity saveTask(@RequestBody @Valid DmpPushTaskFeignDTO dto);
    /**
     * 批量保存
     * @param dtos
     * @return
     */
    @PostMapping("/save/pushTaskList")
    List<DmpPushTaskEntity> saveTaskList(@RequestBody List<DmpPushTaskFeignDTO> dtos);

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
    @PostMapping("/getByParam")
    DmpPushTaskEntity getByParam(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO);

    /**
     * 根据多个id查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("/listByParam")
    List<DmpPushTaskEntity> listByParam(@RequestBody @Valid DmpSyncTaskDTO.ListDTO listDTO);

    /**
     * 根据多个Code查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("/listByCodeParam")
    List<DmpPushTaskEntity> listByCodeParam(@RequestBody @Valid DmpSyncTaskDTO.ListCodeDTO listDTO);

    /**
     * 批量修改无需同步
     *
     * @param sourceIds
     * @return ApiResult
     * @author hyj
     * @date 2024/4/11 16:51
     */
    @PostMapping(value = "/batchNoNeedSyncBySourceId")
    Boolean batchNoNeedSyncBySourceId(@RequestBody List<String> sourceIds);
    /**
     * 根据sourceId重新同步
     *
     * @param sourceIds
     * @return ApiResult
     * @author hyj
     * @date 2024/4/11 16:51
     */
    @PostMapping(value = "/batchSyncBySourceId")
    Boolean batchSyncBySourceId(@RequestBody List<String> sourceIds);

    /**
     * 发送MQ延时等级3消息(10秒后消费)
     * @author Jim
     */
    @PostMapping("/send/delayLevel3SendTask")
    Boolean delayLevel3SendTask(@RequestBody List<DmpPushTaskEntity> list);
}