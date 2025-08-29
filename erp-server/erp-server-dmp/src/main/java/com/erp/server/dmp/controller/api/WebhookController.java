package com.erp.server.dmp.controller.api;

import cn.hutool.json.JSONUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.WebhookServiceEnum;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.*;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.factory.WebhookHandlerFactory;
import com.erp.server.dmp.handler.WebhookHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName WebhookController
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("WebHook接收管理")
@RequestMapping("/webhook")
public class WebhookController extends BaseController {
    @Resource
    private CfgAppClientService cfgAppClientService;
    // 预先约定的Secret
    private static final String SECRET_KEY = "your_secret_key";
    // 允许的时间偏差（秒）
    private static final long MAX_AGE = 5 * 60L; // 5 minutes
    private final WebhookHandlerFactory webhookHandlerFactory;

    public WebhookController(WebhookHandlerFactory webhookHandlerFactory) {
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    /**
     * 接收Webhook请求
     * @param serviceFlag 服务名称
     * @param data 传递数据
     * @param headers 请求头
     * @return
     */
    @PostMapping("/receive/{serviceFlag}")
    public String receiveWebhook(@PathVariable("serviceFlag") String serviceFlag,
                                 @RequestBody String data,
                                 @RequestHeader Map<String, String> headers) {
        log.info("========接收到webhook接口请求=======start");
        log.info("receiveWebhook:serviceFlag:{},data:{},headers:{}",serviceFlag,data,headers);
        // 解析请求中的服务标识，进行不同的处理
        String service = getService(serviceFlag, headers, data);
        // 根据不同平台的Webhook内容做处理
        WebhookHandler handler = webhookHandlerFactory.getHandler(service);
        //安全校验
        handler.verify(data,headers,serviceFlag);
        //业务处理
        String result = handler.process(data,headers,serviceFlag);
        log.info("========接收到webhook接口请求=======end");
        return result;
    }

    private String getService(String serviceFlag, Map<String, String> headers, String data) {
        WebhookServiceEnum serviceEnum = WebhookServiceEnum.getByName(serviceFlag);
        if (Objects.nonNull(serviceEnum)){
            return serviceEnum.getCode();
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return "";
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取请求的完整URL
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        String platform = headers.get("X-Platform");  // 假设平台信息通过头部传递
        return "";
    }

    @PostMapping("/listAllFulfillmentOrders")
    public void listAllFulfillmentOrdersTest(@RequestParam("shopId") String shopId) throws ApiException, LWAException {
        String queryStartDate = "2025-08-01T00:00:00";
        String nextToken = null;
//        String shopId = "1925396781138022402";
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 初始化API
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        ListAllFulfillmentOrdersResponse response = api.listAllFulfillmentOrders(queryStartDate, nextToken);
        System.out.println("listAllFulfillmentOrdersTest");
        System.out.println(JSONUtil.toJsonStr(response));
        // TODO: test validations
    }
    @PostMapping("/getFulfillmentOrder")
    public void getFulfillmentOrder(@RequestParam("shopId") String shopId, @RequestParam("orderId") String orderId) throws ApiException, LWAException {
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 初始化API
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        GetFulfillmentOrderResponse response = api.getFulfillmentOrder(orderId);
        System.out.println("getFulfillmentOrderTest");
        System.out.println(JSONUtil.toJsonStr(response));
        // TODO: test validations
    }

    @PostMapping("/createFulfillmentOrder")
    public void createFulfillmentOrder(@RequestParam("shopId") String shopId) throws ApiException, LWAException {
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 初始化API
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        CreateFulfillmentOrderRequest body = new CreateFulfillmentOrderRequest();
//        String str = "{\"sellerFulfillmentOrderId\":\"WFHD250827000016\",\"destinationAddress\":{\"stateOrRegion\":\"Ontario\",\"city\":\"Cobourg\",\"phone\":\"17665216640\",\"countryCode\":\"CA\",\"postalCode\":\"K9A 0R4\",\"name\":\"zzz\",\"addressLine1\":\"17665216640\",\"addressLine2\":\"\",\"addressLine3\":\"\",\"districtOrCounty\":\"\"},\"displayableOrderDate\":\"2025-08-27T07:29:36.706Z\",\"shippingSpeedCategory\":\"Standard\",\"displayableOrderId\":\"408-4194613-9811562\",\"id\":\"1960605603013390337\",\"shopId\":\"1739494918432231426\",\"items\":[{\"sellerFulfillmentOrderItemId\":\"WFHD250827000016\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X001KDI9TD\",\"sellerSku\":\"2961-EU2\",\"perUnitDeclaredValue\":{\"currencyCode\":\"USD\",\"value\":\"19.95\"}}],\"displayableOrderComment\":\"测试发货\",\"fulfillmentPolicy\":\"FillOrKill\"}";
//        String str = "{\"sellerFulfillmentOrderId\":\"WFHD250827000016\",\"destinationAddress\":{\"stateOrRegion\":\"Ontario\",\"city\":\"Cobourg\",\"phone\":\"17665216640\",\"countryCode\":\"CA\",\"postalCode\":\"K9A 0R4\",\"name\":\"zzz\",\"addressLine1\":\"17665216640\",\"addressLine2\":\"\",\"addressLine3\":\"\",\"districtOrCounty\":\"\"},\"displayableOrderDate\":\"2025-08-27T07:29:36.706Z\",\"shippingSpeedCategory\":\"Standard\",\"displayableOrderId\":\"408-4194613-9811562\",\"id\":\"1960605603013390337\",\"shopId\":\"1739494918432231426\",\"items\":[{\"sellerFulfillmentOrderItemId\":\"2961-EU2\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X001KDI9TD\",\"sellerSku\":\"2961-EU2\",\"perUnitDeclaredValue\":{\"currencyCode\":\"USD\",\"value\":\"19.95\"}}],\"featureConstraints\":[{\"featureName\":\"BLANK_BOX\",\"featureFulfillmentPolicy\":\"NotRequired\"}],\"displayableOrderComment\":\"测试发货\",\"fulfillmentPolicy\":\"FillOrKill\"}\n";
//        String str = "\t\t{\"sellerFulfillmentOrderId\":\"PO-211-14724900291190697\",\"marketplaceId\":\"ATVPDKIKX0DER\",\"displayableOrderId\":\"PO-211-14724900291190697\",\"displayableOrderDate\":1754444217000,\"displayableOrderComment\":\"感谢您的订购\",\"shippingSpeedCategory\":\"Standard\",\"destinationAddress\":{\"name\":\"Jason zozosky\",\"addressLine1\":\"7617 W TAPPS HWY E\",\"addressLine2\":\"2ND FIFTH WHEEL DOOR\",\"city\":\"BONNEY LAKE\",\"stateOrRegion\":\"WA\",\"postalCode\":\"98391-8686\",\"countryCode\":\"US\",\"phone\":\"131475607974701\"},\"fulfillmentAction\":\"Ship\",\"fulfillmentPolicy\":\"FillAllAvailable\",\"receivedDate\":1754444284000,\"fulfillmentOrderStatus\":\"Complete\",\"statusUpdatedDate\":1754620895000,\"notificationEmails\":[\"exlgz3lxqd1206d@u.shipping.temuemail.com\"],\"featureConstraints\":[{\"featureName\":\"BLANK_BOX\",\"featureFulfillmentPolicy\":\"NotRequired\"}],\"items\":[{\"sellerSku\":\"3313-N-M5\",\"sellerFulfillmentOrderItemId\":\"PO-211-14724900291190697-0\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X003TFLG25\",\"orderItemDisposition\":\"Sellable\",\"cancelledQuantity\":0,\"unfulfillableQuantity\":0,\"estimatedShipDate\":1754549999000,\"estimatedArrivalDate\":1754895599000,\"perUnitDeclaredValue\":{\"currencyCode\":\"USD\",\"value\":\"19.95\"}}]}";
//        String str = "{\"sellerFulfillmentOrderId\":\"PO-211-14724900291190696\",\"marketplaceId\":\"ATVPDKIKX0DER\",\"displayableOrderId\":\"PO-211-14724900291190697\",\"displayableOrderDate\":\"2025-08-27T07:29:36.706Z\",\"displayableOrderComment\":\"感谢您的订购\",\"shippingSpeedCategory\":\"Standard\",\"destinationAddress\":{\"name\":\"Jason zozosky\",\"addressLine1\":\"7617 W TAPPS HWY E\",\"addressLine2\":\"2ND FIFTH WHEEL DOOR\",\"city\":\"BONNEY LAKE\",\"stateOrRegion\":\"WA\",\"postalCode\":\"98391-8686\",\"countryCode\":\"US\",\"phone\":\"131475607974701\"},\"fulfillmentAction\":\"Ship\",\"fulfillmentPolicy\":\"FillAllAvailable\",\"receivedDate\":1754444284000,\"fulfillmentOrderStatus\":\"Complete\",\"statusUpdatedDate\":1754620895000,\"notificationEmails\":[\"exlgz3lxqd1206d@u.shipping.temuemail.com\"],\"featureConstraints\":[{\"featureName\":\"BLANK_BOX\",\"featureFulfillmentPolicy\":\"NotRequired\"}],\"items\":[{\"sellerSku\":\"3313-N-M5\",\"sellerFulfillmentOrderItemId\":\"PO-211-14724900291190696-0\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X003TFLG25\",\"orderItemDisposition\":\"Sellable\",\"cancelledQuantity\":0,\"unfulfillableQuantity\":0,\"estimatedShipDate\":1754549999000,\"estimatedArrivalDate\":1754895599000,\"perUnitDeclaredValue\":{\"currencyCode\":\"USD\",\"value\":\"19.95\"}}]}";
        String str = "{\"sellerFulfillmentOrderId\":\"WFHD250827000016\",\"destinationAddress\":{\"stateOrRegion\":\"Ontario\",\"city\":\"Cobourg\",\"phone\":\"17665216640\",\"countryCode\":\"CA\",\"postalCode\":\"K9A 0R4\",\"name\":\"zzz\",\"addressLine1\":\"17665216640\",\"addressLine2\":\"\",\"addressLine3\":\"\",\"districtOrCounty\":\"\"},\"displayableOrderDate\":\"2025-08-27T07:29:36.706Z\",\"shippingSpeedCategory\":\"Standard\",\"displayableOrderId\":\"408-4194613-9811562\",\"id\":\"1960605603013390337\",\"shopId\":\"1739494918432231426\",\"items\":[{\"sellerFulfillmentOrderItemId\":\"WFHD250827000016\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X003TFLG25\",\"sellerSku\":\"3313-N-M5\",\"perUnitDeclaredValue\":{\"currencyCode\":\"USD\",\"value\":\"19.95\"}}],\"displayableOrderComment\":\"测试发货\",\"fulfillmentPolicy\":\"FillOrKill\"}\n";
        body = JSONUtil.toBean(str,CreateFulfillmentOrderRequest.class);
        try {
            ApiResponse<CreateFulfillmentOrderResponse> fulfillmentOrderWithHttpInfo = api.createFulfillmentOrderWithHttpInfo(body);
            System.out.println("createFulfillmentOrderTest");
            System.out.println(JSONUtil.toJsonStr(fulfillmentOrderWithHttpInfo));
        }catch (ApiException e){
            System.out.println(JSONUtil.toJsonStr(e.getResponseBody()));
        }

        // TODO: test validations
    }

    @PostMapping("/cancelFulfillmentOrder")
    public void cancelFulfillmentOrder(@RequestParam("shopId") String shopId, @RequestParam("orderId") String orderId) throws ApiException, LWAException {
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 初始化API
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        ApiResponse<CancelFulfillmentOrderResponse> cancelFulfillmentOrderResponseApiResponse = api.cancelFulfillmentOrderWithHttpInfo(orderId);
        System.out.println("cancelFulfillmentOrderTest");
        System.out.println(JSONUtil.toJsonStr(cancelFulfillmentOrderResponseApiResponse));
        // TODO: test validations
    }
}
