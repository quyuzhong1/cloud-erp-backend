package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.plm.rocketmq.sync.dmp.SyncProductService;
import com.erp.server.plm.rocketmq.sync.wms.WmsSyncProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @CreateTime: 2023-05-19  15:19
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private SyncProductService syncProductService;

    @Autowired
    private WmsSyncProductService wmsSyncProductService;

    @PostMapping("/test")
    public ApiResult<Void> test() {
        syncProductService.syncProductSkuToDmp();
        wmsSyncProductService.syncProductInfoToDmp();
        wmsSyncProductService.syncProductSkuToWms();
        wmsSyncProductService.syncProductSkuSaleToWms();
        return success();
    }

}