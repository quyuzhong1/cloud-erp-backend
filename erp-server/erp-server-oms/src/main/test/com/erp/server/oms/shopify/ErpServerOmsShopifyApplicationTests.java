package com.erp.server.oms.shopify;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyPage;
import com.sdk.oms.shopify.api.rest.model.ShopifyProduct;
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
import java.time.OffsetDateTime;

/**
 * Shopify单元测试
 *
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
        OffsetDateTime minTime = OffsetDateTime.parse("2023-08-23T17:07:31+08:00");

        ShopifyPage<ShopifyOrder> orders = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getOrders(minTime);
        System.out.println("订单结果：\n" + JSONUtil.toJsonStr(orders));
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
        if (null == shopInfo){
            log.error("未找到店铺信息:{}", shopId);
            return;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (null == authEntity){
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

}
