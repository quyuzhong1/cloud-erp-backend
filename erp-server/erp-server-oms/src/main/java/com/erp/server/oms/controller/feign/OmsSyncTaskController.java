package com.erp.server.oms.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.server.oms.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 推送数据
 * @author Will
 * @date: 2023/10/19 11:05
 */
@RestController
@RequestMapping("feign/omsSyncTask")
public class OmsSyncTaskController {

    @Resource
    private SyncTaskService syncTaskService;

    /**
     * 发送推送任务
     * @author Will
     * @date: 2023/10/19 11:07
     * @param syncParamDTO
     */
    @PostMapping("/findDataSendSyncTask")
    public void findDataSendSyncTask(@RequestBody @Validated DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        syncTaskService.findDataSendSyncTask(syncParamDTO);
    }
}
