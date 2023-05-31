package com.erp.server.oms.controller.feign;

import com.erp.server.oms.kingdee.SyncKingdeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("feign/syncKingdee")
public class OmsSyncKingdeeFeignController {

    @Resource
    private SyncKingdeeService syncKingdeeService;

    @PostMapping("/updateBusinessSyncKingdeeStatus")
    public void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params) {
        syncKingdeeService.updateBusinessSyncKingdeeStatus(params);
    }
}
