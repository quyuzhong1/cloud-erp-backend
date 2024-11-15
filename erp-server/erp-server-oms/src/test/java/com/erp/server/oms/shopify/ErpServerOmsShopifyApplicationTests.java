package com.erp.server.oms.shopify;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.*;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClient;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.graphql.model.ShopifyOrderResponse;
import com.sdk.oms.shopify.api.rest.ShopifyRestClient;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.*;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shopify单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerOmsShopifyApplicationTests {

    @Resource
    private CfgOperateLogFieldService logFieldService;
    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Resource
    private ShopifyGraphQLClientService shopifyGraphQLClientService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private ShopAuthService shopAuthService;
    @Resource
    private ShopSdkServer shopSdkServer;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Test
    public void addLogField() {

        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(SoB2cEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("payTime").setFieldName("付款时间").setClassPath(classPath).setType(0).setEnumClass(""));
        logFieldService.saveBatch(logFields);
        String ss=   QcBillStatusEnum.getByCode(QcBillStatusEnum.FINISH_QC.getCode()).getName();
        System.out.println(ss);
    }


    @Test
    public void shopifyOrders() {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";
        ShopifyPage<ShopifyOrder> orders = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getOrders(2);
        String nextPageInfo = orders.getNextPageInfo();
        System.out.println("有分页=" + nextPageInfo);

        ShopifyPage<ShopifyOrder> ordersList = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getOrders(10);
        String nextPageInfo2 = ordersList.getNextPageInfo();
        System.out.println("无分页=" + nextPageInfo2);

        System.out.println("订单结果：\n" + JSONUtil.toJsonStr(orders));
    }

    @Test
    public void filterShopifyOrders() {
        String accessToken = "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";
        String shopifyShopDomain = "luna-shop-test.myshopify.com";

        System.setProperty("socksProxyHost", "127.0.0.1");
        System.setProperty("socksProxyPort", "7890");

        //1、通过检索订单列表，按指定条件获取订单ID，订单付款状态：部分付款，已付款，部分退款，已退款，已作废；订单创建时间：当天\
//        OffsetDateTime lastOffSetTime = OffsetDateTime.parse("2023-11-27T00:00:00+08:00");
        OffsetDateTime lastOffSetTime = OffsetDateTime.parse("2024-10-15T00:00:00+08:00");
        OffsetDateTime nextOffSetTime = OffsetDateTime.parse("2024-10-17T14:05:00+08:00");


        List<ShopifyOrder> shopifyOrders = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getAllUpdatedOrdersCreatedBefore(lastOffSetTime, nextOffSetTime, null);
//                .getUpdatedOrdersCreatedBefore(lastOffSetTime, nextOffSetTime, null, 200);
//        .getOrders(lastOffSetTime, nextOffSetTime, 200);
        System.out.println("订单结果：\n" + JSONUtil.toJsonStr(shopifyOrders));
    }


    @Test
    public void filterShopifyProduct() {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";
        //

        ShopifyPage<ShopifyProduct> products = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getProducts(2);

        System.out.println("商品结果：\n" + JSONUtil.toJsonStr(products));

    }

    @Test
    public void addShopRedis() {
        String shopId = "1701425155244298242";
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (null == shopInfo) {
            log.error("未找到店铺信息:{}", shopId);
            return;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (null == authEntity) {
            log.error("未找到店铺授权信息:{}", shopId);
            return;
        }
        // platform-token:平台名称:店铺ID
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopId);

        ShopifyShopInfoDTO dto = new ShopifyShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(authEntity.getAccessToken())
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 店铺全域名: SHOP_NAME.myshopify.com
                .setShopDomain(shopInfo.getDomain().concat(ShopifyConstant.DOMAIN));
        redisUtil.set(tokenKey, dto);
    }

    @Test
    public void getFulfillmentOrdersFromOrder() {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";
        // 订单单号ID
//        String orderId = "5484776030507";
        String orderId = "5484775899435";

        List<ShopifyFulfillmentOrder> fulfillmentOrdersFromOrder = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getFulfillmentOrdersFromOrder(orderId);

        System.out.println("订单ShopifyFulfillmentOrder结果：\n" + JSONUtil.toJsonStr(fulfillmentOrdersFromOrder));
        // [{"id":"6424810193195","shopId":"82946982187","orderId":"5484776030507","assignedLocationId":"92033024299","requestStatus":"unsubmitted","status":"closed","supportedActions":[],"destination":{"id":"6053425381675","address1":"151 O'Connor St","city":"Ottawa","company":"Snowdevil","country":"Canada","email":"karine.ruby@example.com","lastName":"","phone":"+16135550114","zip":"K2P2L8"},"lineItems":[{"id":"14363854733611","shopId":"82946982187","fulfillmentOrderId":"6424810193195","quantity":1,"lineItemId":"14266416759083","inventoryItemId":"48904852767019","fulfillableQuantity":0,"variantId":"46854120538411"}],"fulfillAt":1694484000000,"fulfillmentHolds":[],"deliveryMethod":{"id":"550833684779","methodType":"shipping"},"createdAt":1694485868000,"updatedAt":1698121790000,"assignedLocation":{"address1":"123 Main St","city":"Toronto","countryCode":"CA","locationId":"92033024299","name":"My Custom Location","phone":"555-5555","province":"Ontario","zip":"A1A 1A1"},"merchantRequests":[]}]
    }

    @Test
    public void verifyTheInstallationRequest(){

        return;
    }


    @Test
    public void givenSomeShopifyFulfillmentCreationRequestWhenCreatingShopifyFulfillmentThenCreateAndReturnFulfillmentWithFulfillmentOrderApi() throws Exception {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";

        ShopifyRestClient shopifySdk = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken);

        final String lineItemId = "14395089027371";
        final String fulfillmentOrderId = "6434836119851";
        final long quantity = 1L;

        final ShopifyLineItem lineItem = new ShopifyLineItem();
        lineItem.setLineItemId(lineItemId);
        lineItem.setQuantity(1L);

        ShopifyLineItemsByFulfillmentOrder order = new ShopifyLineItemsByFulfillmentOrder();
        order.setFulfillmentOrderId(fulfillmentOrderId);
        List<ShopifyFulfillmentOrderPayloadLineItem> items = new LinkedList<>();
        ShopifyFulfillmentOrderPayloadLineItem item = new ShopifyFulfillmentOrderPayloadLineItem();
        item.setQuantity(quantity);
        item.setId(lineItemId);
        items.add(item);
        order.setFulfillmentOrderLineItems(items);

        List<ShopifyLineItemsByFulfillmentOrder> orderList = new LinkedList<>();
        orderList.add(order);

        ShopifyFulfillmentPayload payload = new ShopifyFulfillmentPayload();
        ShopifyTrackingInfo trackingInfo = new ShopifyTrackingInfo();
        trackingInfo.setNumber("MS15626789");
        trackingInfo.setUrl("https://www.my-shipping-company.com?tracking_number=MS1562678");

        payload.setLineItemsByFulfillmentOrder(orderList);
        payload.setTrackingInfo(trackingInfo);

        ShopifyFulfillmentPayloadRoot request = new ShopifyFulfillmentPayloadRoot();
        request.setFulfillment(payload);
        System.out.println("请求参数：\n" + JSONUtil.toJsonStr(request));
        final ShopifyFulfillment actualShopifyFulfillment = shopifySdk.createFulfillment(request);

        System.out.println("givenSomeShopifyFulfillmentCreationRequestWhenCreatingShopifyFulfillmentThenCreateAndReturnFulfillmentWithFulfillmentOrderApi");
        System.out.println(JSONUtil.toJsonStr(actualShopifyFulfillment));
    }

    @Test
    public void transaction() {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";
        String orderId = "5484775768363";
        List<ShopifyTransaction> transactionList = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getOrderTransactions(orderId);
        System.out.println("订单支付信息结果：\n" + JSONUtil.toJsonStr(transactionList));
    }

    @Test
    public void getFulfillmentList() {
        String accessToken = "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";
        String shopifyShopDomain = "jim-shop-test.myshopify.com";
        // 订单单号ID
//        String orderId = "5484776030507";


        ShopifyFulfillmentServicesRoot fulfillmentServices = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getFulfillmentServices();

        System.out.println("订单fulfillmentServices结果：\n" + JSONUtil.toJsonStr(fulfillmentServices));
        // {"fulfillmentServices":[{"id":"66455634219","name":"Snow City Warehouse","serviceName":"Snow City Warehouse","handle":"snow-city-warehouse","fulfillmentOrdersOptIn":true,"includePendingStock":false,"locationId":"92033057067","callbackUrl":"https://mock.shop/","trackingSupport":false,"inventoryManagement":false,"adminGraphqlApiId":"gid://shopify/ApiFulfillmentService/66455634219","permitsSkuSharing":true}]}
    }

    @Test
    public void shipOrderTest() {
        String mainId = "1739995625497300993";
        //检查销售订单是否存在
        SoB2cEntity mainEntity = soB2cService.getById(mainId);
        if (ObjectUtil.isEmpty(mainEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (!"Shopify".equalsIgnoreCase(mainEntity.getDictPlatform())) {
            throw new ServiceException("非Shopify平台");
        }
        //检查销售订单物流信息是否存在
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cLogisticsService.listByMainIds(Collections.singletonList(mainEntity.getId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.get(0);

        //检查销售订单详情是否存在
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(mainId);
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        if (soB2cDetailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
            throw new ServiceException("平台来源详情ID为空");
        }
        Map<String, SoB2cDetailEntity> detailEntityMap = soB2cDetailEntityList.stream().collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, Function.identity()));

        String shopId = mainEntity.getShopId();
        ShopifyShopInfoDTO shopInfoDTO = shopSdkServer.getTokenAndDomainByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[Shopify标记发货]从缓存中获取shopify token 失败: shopId={}", shopId);
            throw new ServiceException();
        }
        // 请求相关信息
        String platformOrderId = mainEntity.getPlatformCode();
        String shopifyShopDomain = shopInfoDTO.getShopDomain();
        String accessToken = shopInfoDTO.getAccessToken();
        // 初始化客户端
        ShopifyRestClient shopifyRestClient = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken);
        // Retrieves a list of fulfillment orders for a specific order
        List<ShopifyFulfillmentOrder> fulfillmentOrdersFromOrderList = shopifyRestClient.getFulfillmentOrdersFromOrder(platformOrderId);
        if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)) {
            throw new ServiceException("找不到Shopify发货单");
        }
        // 未签收的单
        fulfillmentOrdersFromOrderList = fulfillmentOrdersFromOrderList.stream().filter(e -> e.getStatus().equalsIgnoreCase("open")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fulfillmentOrdersFromOrderList)){
            log.warn("Shopify 忽略表发货, 订单已标记, platformCode={}, fulfillment={}", platformOrderId, JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
            return;
        }

        // 校验不为空
        if (fulfillmentOrdersFromOrderList.stream().anyMatch(e -> CollectionUtils.isEmpty(e.getLineItems()))) {
            log.error("[Shopify标记发货]Shopify数据异常: json={}", JSONUtil.toJsonStr(fulfillmentOrdersFromOrderList));
            throw new ServiceException("Shopify数据异常：详情LineItems为空");
        }


        // 需要根据配送服务分组请求参数
        for (ShopifyFulfillmentOrder fulfillmentOrder : fulfillmentOrdersFromOrderList) {
            // 组合请求参数
            List<ShopifyLineItemsByFulfillmentOrder> orderList = new LinkedList<>();
            List<ShopifyFulfillmentOrderPayloadLineItem> items = new LinkedList<>();
            // 组合请求参数
            ShopifyLineItemsByFulfillmentOrder orderRequestDTO = new ShopifyLineItemsByFulfillmentOrder();
            for (ShopifyFulfillmentOrderLineItem lineItem : fulfillmentOrder.getLineItems()) {
                SoB2cDetailEntity detailEntity = detailEntityMap.get(lineItem.getLineItemId());
                if (null == detailEntity) {
                    continue;
                }
                ShopifyFulfillmentOrderPayloadLineItem item = new ShopifyFulfillmentOrderPayloadLineItem();
                item.setQuantity(detailEntity.getQty());
                item.setId(lineItem.getId());
                items.add(item);
            }
            orderRequestDTO.setFulfillmentOrderId(fulfillmentOrder.getId());
            orderRequestDTO.setFulfillmentOrderLineItems(items);
            orderList.add(orderRequestDTO);

            ShopifyFulfillmentPayload payload = new ShopifyFulfillmentPayload();
            ShopifyTrackingInfo trackingInfo = new ShopifyTrackingInfo();
            trackingInfo.setNumber(logisticsEntity.getTrackNo());
            trackingInfo.setUrl("");
            trackingInfo.setCompany(logisticsEntity.getCode());
            payload.setLineItemsByFulfillmentOrder(orderList);
            payload.setTrackingInfo(trackingInfo);
            ShopifyFulfillmentPayloadRoot request = new ShopifyFulfillmentPayloadRoot();
            request.setFulfillment(payload);
            log.warn("[Shopify标记发货]platformCode={},创建Fulfillment参数：,dto={}",platformOrderId, JSONUtil.toJsonStr(request));
            // Creates a fulfillment for one or many fulfillment orders
            final ShopifyFulfillment actualShopifyFulfillment = shopifyRestClient.createFulfillment(request);
            log.warn("[Shopify标记发货] platformCode={},创建Fulfillment结果：{}", platformOrderId, JSONUtil.toJsonStr(actualShopifyFulfillment));
            if (null == actualShopifyFulfillment) {
                throw new ServiceException("Shopify创建Fulfillment失败");
            }
        }
    }

    @Test
    public void shopifyGraphQLReturnTest() {

        ShopifyGraphQLClient shopifyGraphQLClient = shopifyGraphQLClientService.getShopifyGraphQLClient("luna-shop-test.myshopify.com", "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");
        // 退款
        String orderReturn = shopifyGraphQLClient.getOrderReturn("5530425884864");
        // 退货
//        String orderReturn = shopifyGraphQLClient.getOrderReturn("5533574234304");
        System.out.println("订单退货信息结果");
        System.out.println(orderReturn);
    }

    @Test
    public void shopifyReturnTest() {
        ShopifyRestClient shopifyRestClient = shopifyRestClientService.getShopifyRestClient("luna-shop-test.myshopify.com", "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");
        // 退款
//        String orderReturn = shopifyRestClient.getOrderRefunds("5530425884864");
        // 退货
//        String orderReturn = shopifyRestClient.getOrderRefunds("5533574234304");
        String orderReturn = shopifyRestClient.getOrderRefunds("5630271684800");
        System.out.println("订单退货信息结果");
        System.out.println(orderReturn);
    }



}
