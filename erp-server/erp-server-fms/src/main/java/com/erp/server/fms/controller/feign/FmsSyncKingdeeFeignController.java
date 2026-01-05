package com.erp.server.fms.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.server.fms.service.SyncTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("feign/srmSyncKingdee")
public class FmsSyncKingdeeFeignController {

    @Resource
    private SyncTaskService syncTaskService;

    /**
     * 更新状态
     * @author will
     * @date 2025/12/30 16:29
     * @param params
     * @return void
     */
    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params) {
        syncTaskService.updateBusinessSyncKingdeeStatus(params);
    }

    /**
     * 新中台查询同步
     * @param syncParamDTO
     * @return
     */
    @PostMapping("/newFindDataSendSyncTask")
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        return syncTaskService.newFindDataSendSyncTask(syncParamDTO);
    }
}
