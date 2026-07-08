package com.erp.server.oms.controller.api;

import com.common.core.controller.BaseController;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.oms.dht.SyncDhtService;
import com.erp.server.oms.rocketmq.consumer.PlatformB2bOrderRestCloudConsumerService;
import com.erp.server.oms.rocketmq.consumer.PlatformOrderConsumerService;
import com.erp.server.oms.schedule.HistoryReceiptJob;
import com.erp.server.oms.schedule.SoB2cRetryJob;
import com.erp.server.oms.sdk.authorize.PDDAuthorize;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoB2cLabelService;
import com.erp.server.oms.service.impl.SoB2cServiceImpl;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

//    @Resource
//    private TfFiscalService tfFiscalService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SoB2cRetryJob job;

    @Resource
    private SoB2cLabelService service;


    @Resource
    private SyncDhtService syncDhtService;

    @Resource
    private HistoryReceiptJob historyReceiptJob;

    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    @Resource
    private PDDAuthorize pddAuthorize;

    @Resource
    private PlatformB2bOrderRestCloudConsumerService platformB2bOrderRestCloudConsumerService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private PlatformOrderConsumerService platformOrderConsumerService;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

//    @Resource
//    private PlatformFulfillmentJob platformFulfillmentJob;

    @Resource
    private SoB2cServiceImpl soB2cService;

    @GetMapping("/test")
    public void select(@RequestParam(value = "shippingProviderId") String shippingProviderId,@RequestParam(value = "trackingNumber") String trackingNumber,@RequestParam(value = "packageId") String packageId) throws Exception {
        soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getSignOrderError, "12")
                .eq(SoB2cEntity::getId, "1739999456293556225")
                .update();
        soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getSignOrderError, "123")
                .eq(SoB2cEntity::getId, "1739999456293556225")
                .update(new SoB2cEntity());
    }

}
