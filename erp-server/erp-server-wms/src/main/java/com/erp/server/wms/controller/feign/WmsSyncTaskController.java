package com.erp.server.wms.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.server.wms.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 推送数据
 * @Author Luo_WG
 * @Date 2023/10/30 11:49
 **/

@RestController
@RequestMapping("feign/wmsSyncTask")
public class WmsSyncTaskController {


    @Resource
    private SyncTaskService syncTaskService;

    /**
     * 更新任务列表负责人名称
     * @author Will
     * @date: 2023/10/19 11:07
     * @param syncParamDTO
     */
    @PostMapping("/findDataSendSyncTask")
    public void findDataSendSyncTask(@RequestBody @Validated DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        syncTaskService.findDataSendSyncTask(syncParamDTO);
    }

    /**
     * 查询数据重新发送
     * @author Will
     * @date: 2023/10/19 11:07
     * @param syncParamDTO
     */
    @PostMapping("/findMaBangDataSendSyncTask")
    public void findMaBangDataSendSyncTask(@RequestBody @Validated DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        syncTaskService.findMaBangDataSendSyncTask(syncParamDTO);
    }

    /**
     * 查询旺店通数据重新发送
     * @param syncParamDTO
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    @PostMapping("/findWdtDataSendSyncTask")
    void findWdtDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO){
        syncTaskService.findWdtDataSendSyncTask(syncParamDTO);
    }
}
