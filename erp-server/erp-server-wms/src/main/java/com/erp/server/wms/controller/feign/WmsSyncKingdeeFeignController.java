package com.erp.server.wms.controller.feign;

import com.erp.server.wms.kingdee.SyncKingdeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/23 10:47
 */
@RestController
@RequestMapping("feign/syncKingdee")
public class WmsSyncKingdeeFeignController {

    @Resource
    private SyncKingdeeService syncKingdeeService;

    /**
     * @param params
     * @description: 更新业务状态
     * @author Will
     * @date: 2023/3/10 15:46
     */
    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params) {
        syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }

}
