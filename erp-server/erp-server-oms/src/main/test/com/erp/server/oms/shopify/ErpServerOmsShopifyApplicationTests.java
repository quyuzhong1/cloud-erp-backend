package com.erp.server.oms.shopify;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyPage;
import com.sdk.oms.shopify.api.rest.model.ShopifyProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.OffsetDateTime;

/**
 *
 */
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

    @Test
    public void shopifyOrders() {
        String accessToken = "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";
        String shopifyShopDomain = "grays-test.myshopify.com";
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
        String shopifyShopDomain = "grays-test.myshopify.com";

        //1、通过检索订单列表，按指定条件获取订单ID，订单付款状态：部分付款，已付款，部分退款，已退款，已作废；订单创建时间：当天\
        OffsetDateTime minTime = OffsetDateTime.parse("2023-08-23T17:07:31+08:00");

        ShopifyPage<ShopifyOrder> orders = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getOrders(minTime);
        System.out.println("订单结果：\n" + JSONUtil.toJsonStr(orders));
    }


    @Test
    public void filterShopifyProduct() {
        String accessToken = "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";
        String shopifyShopDomain = "grays-test.myshopify.com";
        //

        ShopifyPage<ShopifyProduct> products = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getProducts(2);
        System.out.println("订单结果：\n" + JSONUtil.toJsonStr(products));

    }


}
