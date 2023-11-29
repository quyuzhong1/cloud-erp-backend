package com.erp.server.oms.shopify;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClient;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.*;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        String shopifyShopDomain = "jim-shop-test.myshopify.com";

        //1、通过检索订单列表，按指定条件获取订单ID，订单付款状态：部分付款，已付款，部分退款，已退款，已作废；订单创建时间：当天\
//        OffsetDateTime lastOffSetTime = OffsetDateTime.parse("2023-11-27T00:00:00+08:00");
        OffsetDateTime nextOffSetTime = OffsetDateTime.parse("2023-09-23T00:00:00+08:00");
        OffsetDateTime lastOffSetTime = OffsetDateTime.parse("2023-08-22T00:00:00+08:00");


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
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopId);

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
        String orderId = "5493983314219";

        List<ShopifyFulfillmentOrder> fulfillmentOrdersFromOrder = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getFulfillmentOrdersFromOrder(orderId);

        System.out.println("订单ShopifyFulfillmentOrder结果：\n" + JSONUtil.toJsonStr(fulfillmentOrdersFromOrder));
        // [{"id":"6424810193195","shopId":"82946982187","orderId":"5484776030507","assignedLocationId":"92033024299","requestStatus":"unsubmitted","status":"closed","supportedActions":[],"destination":{"id":"6053425381675","address1":"151 O'Connor St","city":"Ottawa","company":"Snowdevil","country":"Canada","email":"karine.ruby@example.com","lastName":"","phone":"+16135550114","zip":"K2P2L8"},"lineItems":[{"id":"14363854733611","shopId":"82946982187","fulfillmentOrderId":"6424810193195","quantity":1,"lineItemId":"14266416759083","inventoryItemId":"48904852767019","fulfillableQuantity":0,"variantId":"46854120538411"}],"fulfillAt":1694484000000,"fulfillmentHolds":[],"deliveryMethod":{"id":"550833684779","methodType":"shipping"},"createdAt":1694485868000,"updatedAt":1698121790000,"assignedLocation":{"address1":"123 Main St","city":"Toronto","countryCode":"CA","locationId":"92033024299","name":"My Custom Location","phone":"555-5555","province":"Ontario","zip":"A1A 1A1"},"merchantRequests":[]}]
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
        lineItem.setId(lineItemId);
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
}
