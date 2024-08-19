package com.erp.server.dmp.inout.handler.input.task.init.api.shopify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.sdk.oms.shopee.dto.global.response.GlobalItem;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyProduct;
import com.sdk.oms.shopify.api.rest.model.ShopifyProducts;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Scope("prototype")
public class ShopifyProductApiInitHandler implements DmpInputApiInitHandler {

    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        //  根据店铺ID获取授权
        ShopifyShopInfoDTO tokenDTO = shopSdkServer.getTokenAndDomainByShopId(nextLevelId);
        if (null == tokenDTO) {
            log.error("[Shopify产品下载]从缓存中获取shopify token 失败: shopId={}", nextLevelId);
            return Collections.emptyList();
        }
        String shopifyShopDomain = tokenDTO.getShopDomain();
        String accessToken = tokenDTO.getAccessToken();

//        System.setProperty("socksProxyHost", "127.0.0.1");
//        System.setProperty("socksProxyPort", "7890");
        // Shopify产品下载所有(SDK已分页查询所有)
        ShopifyProducts products = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getProducts();
        if (CollectionUtils.isEmpty(products.values())) {
            return Collections.emptyList();
        }
/*
        String json = "[{'image' : {" +
                "'metafields' : []," +
                "'productId' : '7646915592384'," +
                "'variantIds' : []," +
                "'id' : '36865064468672'," +
                "'position' : 1," +
                "'source' : 'https://cdn.shopify.com/s/files/1/0628/1229/1264/products/Main_b9e0da7f-db89-4d41-83f0-7f417b02831d.jpg?v=1701168210'" +
                "}," +
                "'images' : [" +
                "{" +
                "'metafields' : []," +
                "'productId' : '7646915592384'," +
                "'variantIds' : []," +
                "'id' : '36865064468672'," +
                "'position' : 1," +
                "'source' : 'https://cdn.shopify.com/s/files/1/0628/1229/1264/products/Main_b9e0da7f-db89-4d41-83f0-7f417b02831d.jpg?v=1701168210'" +
                "}" +
                "]," +
                "'publishedAt' : '2023-11-28T05:43:30-05:00'," +
                "'handle' : 'the-3p-fulfilled-snowboard'," +
                "'variants' : [" +
                "{" +
                "'inventoryManagement' : 'shopify'," +
                "'productId' : '7646915592384'," +
                "'taxable' : true," +
                "'inventoryQuantity' : 20," +
                "'inventoryPolicy' : 'DENY'," +
                "'available' : 0," +
                "'weight' : '0.0'," +
                "'adminGraphqlApiId' : 'gid://shopify/ProductVariant/44229886345408'," +
                "'title' : 'Default Title'," +
                "'oldInventoryQuantity' : 20," +
                "'inventoryItemId' : 46179919429824," +
                "'createdAt' : '2023-11-28T18:43:30'," +
                "'requiresShipping' : true," +
                "'price' : '2629.95'," +
                "'fulfillmentService' : 'manual'," +
                "'option1' : 'Default Title'," +
                "'id' : '44229886345408'," +
                "'position' : 1," +
                "'grams' : 0," +
                "'sku' : 'sku-hosted-1'," +
                "'updatedAt' : '2023-11-28T18:43:35'," +
                "'weightUnit' : 'kg'" +
                "}" +
                "]," +
                "'adminGraphqlApiId' : 'gid://shopify/Product/7646915592384'," +
                "'title' : 'The 3p Fulfilled Snowboard'," +
                "'tags' : [" +
                "'Sport'," +
                "'Accessory'," +
                "'Winter'" +
                "]," +
                "'createdAt' : '2023-11-28T18:43:30'," +
                "'vendor' : 'luna-shop-test'," +
                "'options' : [" +
                "{" +
                "'productId' : '7646915592384'," +
                "'values' : [" +
                "'Default Title'" +
                "]," +
                "'name' : 'Title'," +
                "'id' : '9846242345152'," +
                "'position' : 1" +
                "}" +
                "]," +
                "'sortedOptionNames' : [" +
                "'Title'" +
                "]," +
                "'id' : '7646915592384'," +
                "'productType' : ''," +
                "'publishedScope' : 'web'," +
                "'status' : 'active'," +
                "'updatedAt' : '2023-11-29T09:54:42'" +
                "}]";

        // 解析JSON对象
        JSONArray jsonObject = JSON.parseArray(json);

        // 获取images数组并转换为List<ShopifyProduct>
        List<ShopifyProduct> shopifyProductsList = jsonObject.toJavaList(ShopifyProduct.class);

        // 构造ShopifyProducts对象
        ShopifyProducts products = new ShopifyProducts(shopifyProductsList);*/

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(products.values()));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
    }
}
