package com.sdk.wms.goodcang.service;


import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClient;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.graphql.model.ShopifyOrderResponse;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyAddress;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import com.sdk.oms.shopify.dto.PlatformShopifyOrderDTO;
import com.sdk.oms.shopify.handler.ShopifyOrderHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ShopifyOrderHandler.class,ShopifyRestClientService.class, ShopifyGraphQLClientService.class})
public class ShopifyServiceTest {

    @Resource
    private ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Resource
    private ShopifyOrderHandler shopifyOrderHandler;

    @Test
    public void shopifyGraphQLTest() {
//        PlatformShopifyOrderDTO platformShopifyOrderDTO = new PlatformShopifyOrderDTO();
//        ShopifyOrder b = new ShopifyOrder();
//        b.setOrderId("5722925924573");
//        b.setShippingAddress();
//        platformShopifyOrderDTO.setShopifyOrder(b);
//        shopifyOrderHandler.downloadDetail(platformShopifyOrderDTO,null);
        List<ShopifyTransaction> transactionList = shopifyRestClientService.getShopifyRestClient("agimbalgear.myshopify.com", "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")
                .getOrderTransactions("5722925924573");
        System.out.println(transactionList);
        ShopifyGraphQLClient shopifyGraphQLClient = shopifyGraphQLClientService.getShopifyGraphQLClient("agimbalgear.myshopify.com", "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");
        ShopifyOrderResponse order = shopifyGraphQLClient.getOrderLocalizationExtensions("5722925924573");
        System.out.println(order);
    }

}