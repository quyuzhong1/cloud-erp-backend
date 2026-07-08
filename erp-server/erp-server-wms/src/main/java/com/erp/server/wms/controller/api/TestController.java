
package com.erp.server.wms.controller.api;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.UserContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.wms.mapper.SoOutstockDetailMapper;
import com.erp.server.wms.rocketmq.consumer.*;
import com.erp.server.wms.schedule.ThirdWarehouseRefreshTokenJob;
import com.erp.server.wms.sdk.delivery.ShopifyShipOrder;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.erp.server.wms.service.impl.SoOutstockServiceImpl;
import com.sdk.third.lingxing.dto.OrderFastOutboundPackageDTO;
import com.sdk.third.lingxing.dto.Result;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import com.sdk.wms.weishi.dto.request.WeiShiStockRequest;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.dto.response.WeiShiWarehouseResp;
import com.sdk.wms.weishi.service.WeiShiService;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private ShopifyShipOrder shopifyShipOrder;

    @Resource
    private WeiShiService weiShiService;

    @Resource
    private ThirdWarehouseRefreshTokenJob job;

    @Resource
    private SyncPddSoOutConsumer syncPddSoOutConsumer;

    @Resource
    private SoOutstockDetailMapper soOutstockDetailMapper;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @Resource
    private ThirdWarehouseDeliveryDetailService thirdWarehouseDeliveryDetailService;

    @Resource
    private RestCloudPlatformNewInventoryConsumerService restCloudPlatformNewInventoryConsumerService;

    @Resource
    private PlatformNewInboundConsumerService platformNewInboundConsumerService;

    @Resource
    private PlatformNewOutboundConsumerService platformNewOutboundConsumerService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    private PlatformFbaShipmentConsumerService platformFbaShipmentConsumerService;

    @Resource
    private SoOutstockServiceImpl soOutstockServiceIml;

    @Resource
    private PlatformOutboundConsumerService<?> platformOutboundConsumerService;

    private static final String THIRD_WAREHOUSE_EMPTY_RESPONSE = "第三方仓接口返回为空";
    private static final String THIRD_WAREHOUSE_TIMEOUT_TEST_REMARK = "测试三方仓超时";
    private static final String THIRD_WAREHOUSE_TIMEOUT_TEST_PATH = "/test/thirdWarehouseTimeout";

    @Value("${server.port:9090}")
    private String serverPort;

    @GetMapping("/test")
    public void select() throws Exception {
//        String a = TraceContext.traceId();
//        System.out.println(a);
        try {
            String response = OkHttpUtils.doGet("http://127.0.0.1:" + serverPort + THIRD_WAREHOUSE_TIMEOUT_TEST_PATH, null, null);

        } catch(Exception e){
            System.out.println(123);
        }
        //        String a = "{\"shipmentCreateTime\":\"2026-04-09T11:18:39.103\",\"fbaShipmentId\":\"FBA15G8DBHF5\",\"labelType\":\"卖家标签\",\"packType\":\"\",\"isSta\":true,\"platformShipmentStatus\":\"READY_TO_SHIP\",\"countryId\":\"CN\",\"platform\":\"Amazon\",\"platformUpdateTime\":\"2026-04-09T11:18:39.103\",\"fulfillmentCenter\":\"TPB7\",\"deliveryFromAddress\":\"518100 CN Zhejiang Wenzhou No.140, Zhongxing Rd., Bantian St., Longgang Dist., VIJIM Select\",\"dmpOutputTaskRecordDataId\":\"FBA15G8DBHF5\",\"dmpSyncTaskId\":\"2042079697498484738\",\"dmpOutputTaskRecordId\":\"2042079716679036930\",\"detailList\":[{\"deliveryQty\":0,\"fnSku\":\"X0011I3205\",\"fbaShipmentId\":\"FBA15G8DBHF5\",\"receiveDate\":\"2026-04-09T11:18:39.104\",\"receiveQty\":0,\"sellerSku\":\"3028-J-6\",\"declareQty\":36},{\"deliveryQty\":0,\"fnSku\":\"X0014QCVIN\",\"fbaShipmentId\":\"FBA15G8DBHF5\",\"receiveDate\":\"2026-04-09T11:18:39.104\",\"receiveQty\":0,\"sellerSku\":\"2784-JP6\",\"declareQty\":16}],\"name\":\"FBA STA (2026/04/08 09:02)-TPB7\",\"shopId\":\"1735553314990329858\",\"deliveryStatus\":\"unShipped\",\"uniqueId\":\"FBA15G8DBHF5\"}";
//        platformFbaShipmentConsumerService.handle(a);
//
//        List<OrderFastOutboundPackageDTO.PackageInfo> packageList = new ArrayList<>();
//        OrderFastOutboundPackageDTO.PackageInfo packageInfo = new OrderFastOutboundPackageDTO.PackageInfo();
//        packageInfo.setGlobalOrderNo("103564263733612298");
//        packageInfo.setLogisticsTypeId("9-24");
//        packageInfo.setWaybillNo("438374296942");
//        packageInfo.setTrackingNo("438374296942");
//        packageInfo.setWid(Long.valueOf("41"));
//        Result result = LingxingApiUtils.fastOutbound(Arrays.asList(packageInfo));
//        System.out.println(JSONUtil.toJsonStr(result));
    }

    @GetMapping("/antuOutboundDtoDetailList")
    public ApiResult<Map<String, Object>> antuOutboundDtoDetailList() {
        String soId = "2056286841518997506";
        String json = "{"
                + "\"warehousePlatformType\":\"overseasWarehouse\","
                + "\"provider\":\"antu\","
                + "\"platform\":\"antu\","
                + "\"orderCode\":\"ANTU-TEST-260518-000001\","
                + "\"referenceNo\":\"JT20260518161401\","
                + "\"swOrderNumber\":\"JT20260518161401\","
                + "\"orderStatus\":\"shipped\","
                + "\"thirdOrderStatus\":\"已发货\","
                + "\"outBoundTime\":\"2026-05-18T16:20:00\","
                + "\"trackNo\":\"ANTU-TEST-TRACK-260518000005\","
                + "\"warehouseCode\":\"BR01\","
                + "\"shippingMethod\":\"TEST\","
                + "\"carrierName\":\"TEST\","
                + "\"detailList\":[{\"platformSkuNo\":\"3PL-1C-TEST\",\"qty\":2}]"
                + "}";

        ApiResult<?> handleResult = platformOutboundConsumerService.handle(json);
        SoOutstockEntity outstock = soOutstockService.getBySoId(soId);
        Map<String, Object> result = new HashMap<>();
        result.put("handleResult", handleResult);
        result.put("soId", soId);
        result.put("outstockId", Objects.isNull(outstock) ? null : outstock.getId());
        result.put("outstockCode", Objects.isNull(outstock) ? null : outstock.getCode());
        result.put("payload", json);
        return success(result);
    }

}
