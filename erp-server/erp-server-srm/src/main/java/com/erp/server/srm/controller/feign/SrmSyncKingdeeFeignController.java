package com.erp.server.srm.controller.feign;

import com.common.business.dto.DmpSyncMqDTO;
import com.erp.server.srm.kingdee.SyncKingdeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("feign/srmSyncKingdee")
public class SrmSyncKingdeeFeignController {

    @Resource
    private SyncKingdeeService syncKingdeeService;

    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params) {
        syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }

    /**
     * 新中台查询同步
     * @param syncParamDTO
     * @return
     */
    @PostMapping("/newFindDataSendSyncTask")
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        return syncKingdeeService.newFindDataSendSyncTask(syncParamDTO);
    }
}
