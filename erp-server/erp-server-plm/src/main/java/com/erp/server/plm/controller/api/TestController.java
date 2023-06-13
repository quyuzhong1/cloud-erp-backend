package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.plm.rocketmq.sync.wms.WmsSyncProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @CreateTime: 2023-05-30  14:47
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private WmsSyncProductService wmsSyncProductService;

    @PostMapping("/test")
    public ApiResult test() {
        wmsSyncProductService.syncProductInfoToWms();
        wmsSyncProductService.syncProductSkuToWms();
        wmsSyncProductService.syncProductSkuSaleToWms();
        return success();
    }

}