package com.sdk.oms.shopee.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.product.request.ProductRequest;
import com.sdk.oms.shopee.dto.product.response.*;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

/**
 * @author zdy
 * @ClassName ShopeeProductService
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Slf4j
@Component
@Service
public class ShopeeProductService {

    public static void main(String[] args) {
        ShopeeProductService shopeeProductService = new ShopeeProductService();
        //产品列表
        ProductRequest productRequest = ProductRequest.builder()
                .host(host)
                .offset(0)
                .token(shop_access_token)
                .shopId(shop_id)
                .partnerId(partner_id)
                .tmpPartnerKey(tmp_partner_key)
                .timeTo(null)
                .timeFrom(null)
                .itemId(1883822L)
                .build();
//        List<ModelInfo> modelList = shopeeProductService.getModelList(productRequest);
//        System.out.println(modelList);
        List<ShopeeProductInfo> productList = new ArrayList<>(0);
        shopeeProductService.getAllProduct(productRequest, productList);
//        JSONObject response = productList.getResponse();
//        JSONArray list = (JSONArray) response.get("item");
//        List<Item> items = JSONObject.parseArray(list.toJSONString(), Item.class);
//        ShopeeProduct shopeeProduct = ShopeeProduct.builder().build();
//        int total_count = (int) response.get("total_count");
//        Boolean has_next_page = (Boolean) response.get("has_next_page");
//        String next = (String) response.get("next");
        System.out.println(productList.size());
//        System.out.println(total_count);
//        System.out.println(has_next_page);
//        System.out.println(next);

//        ShopeeResponse productItemBaseInfo = shopeeProductService.getProductItemBaseInfo(host, shop_access_token, shop_id, partner_id, tmp_partner_key, "22982747521");
//        ShopeeResponse productItemExtraInfo = shopeeProductService.getProductItemExtraInfo(host, shop_access_token, shop_id, partner_id, tmp_partner_key, "22982747521");

    }

    public void getAllProduct(ProductRequest productRequest, List<ShopeeProductInfo> shopeeProductInfos) {
        log.info("获取产品订单：{}", productRequest);
        ShopeeResponse productList = this.getProductList(productRequest);
        if (Objects.isNull(productList) || Objects.isNull(productList.getResponse())) {
            return;
        }
        JSONObject response = productList.getResponse();

        String error = response.getStr("error");
        if (StringUtils.isNotEmpty(error)) {
            return;
        }
        JSONArray jsonArray = response.getJSONArray("item");
        if (Objects.isNull(jsonArray)){
            return;
        }
        //目录列表
        List<Item> items = JSONUtil.toList(jsonArray, Item.class);
        //获取item明细
        List<Long> itemIds = items.stream().map(Item::getItemId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(itemIds)) {
            return;
        }
        productRequest.setItemIdList(StringUtils.join(itemIds, ","));
        //获取产品基础信息
        ShopeeResponse productItemBaseInfo = this.getProductItemBaseInfo(productRequest);
        JSONObject responseBaseInfo = productItemBaseInfo.getResponse();
        if (Objects.isNull(responseBaseInfo)) {
            return;
        }
        //循环填充
        JSONArray listBase = responseBaseInfo.getJSONArray("item_list");
        List<ItemInfo> itemInfos = JSONUtil.toList(listBase, ItemInfo.class);
        if (CollectionUtils.isEmpty(itemInfos)) {
            return;
        }
        //获取产品model列表
        for (Long itemId : itemIds){
            if (Objects.isNull(itemId)){
                return;
            }
            productRequest.setItemId(itemId);
            List<ModelInfo> modelList = getModelList(productRequest);
            //匹配对应的
            if (CollectionUtils.isEmpty(modelList)){
                continue;
            }
            //匹配
            ItemInfo itemInfo = itemInfos.stream().filter(e -> Objects.nonNull(e.getItemId()) && e.getItemId().compareTo(itemId) == 0).findFirst().orElse(null);
            if (Objects.isNull(itemInfo)){
                continue;
            }

            for (ModelInfo modelInfo : modelList){
                ShopeeProductInfo shopeeProductInfo = new ShopeeProductInfo();
                shopeeProductInfo.setModelInfo(modelInfo);
                shopeeProductInfo.setItemInfo(itemInfo);
                shopeeProductInfos.add(shopeeProductInfo);
            }
        }
        //是否还有数据
        boolean hasNextPage = response.getBool("has_next_page");
        if (hasNextPage) {
            Integer next_offset = response.getInt("next_offset");
            productRequest.setOffset(next_offset);
            getAllProduct(productRequest, shopeeProductInfos);
        }
//        System.out.println(responseBaseInfo);
//        ShopeeResponse productItemExtraInfo = this.getProductItemExtraInfo(host, token, shopId, partner_id, tmp_partner_key, StringUtils.join(itemIds, ","));
//        JSONObject responseExtraInfo = productItemExtraInfo.getResponse();
//        System.out.println(responseExtraInfo);
    }

    public ShopeeResponse getProductList(ProductRequest productRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_list";
        long timestamp = System.currentTimeMillis() / 1000L;
        paramMap.put("timestamp", timestamp);
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, productRequest.getToken(), productRequest.getPartnerId(),
                productRequest.getTmpPartnerKey(), productRequest.getShopId()));
        paramMap.put("shop_id", productRequest.getShopId());
        paramMap.put("partner_id", productRequest.getPartnerId());
        paramMap.put("access_token", productRequest.getToken());
        paramMap.put("offset", productRequest.getOffset());
        paramMap.put("page_size", pageSize);
//        if (Objects.nonNull(productRequest.getTimeFrom())) {
//            paramMap.put("update_time_from", productRequest.getTimeFrom());
//        }
//        if (Objects.nonNull(productRequest.getTimeTo())) {
//            paramMap.put("update_time_to", productRequest.getTimeTo());
//        }
        List<String> status = new ArrayList<>();
        status.add("NORMAL");
        status.add("DELETED");
        status.add("UNLIST");
        status.add("BANNED");
        paramMap.put("item_status", status);
        return ShopeeApiUtils.sendGet(productRequest.getHost() + path, paramMap);
    }

    public ShopeeResponse getProductItemBaseInfo(ProductRequest productRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_base_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, productRequest.getToken(), productRequest.getPartnerId(),
                productRequest.getTmpPartnerKey(), productRequest.getShopId()));
        paramMap.put("shop_id", productRequest.getShopId());
        paramMap.put("partner_id", productRequest.getPartnerId());
        paramMap.put("access_token", productRequest.getToken());
        paramMap.put("item_id_list", productRequest.getItemIdList());
        paramMap.put("need_complaint_policy", true);
        paramMap.put("need_tax_info", true);
        return ShopeeApiUtils.sendGet(productRequest.getHost() + path, paramMap);
    }

    public ShopeeResponse getProductItemExtraInfo(String host, String token, long shopId, long partner_id, String tmp_partner_key, String itemIdList) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_extra_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, token, partner_id, tmp_partner_key, shopId));
        paramMap.put("shop_id", shopId);
        paramMap.put("partner_id", partner_id);
        paramMap.put("access_token", token);
        paramMap.put("item_id_list", itemIdList);
        return ShopeeApiUtils.sendGet(host + path, paramMap);
    }

    /**
     * 获取产品model
     * @param productRequest
     * @return
     */
    public List<ModelInfo> getModelList(ProductRequest productRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_model_list";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, productRequest.getToken(), productRequest.getPartnerId(),
                productRequest.getTmpPartnerKey(), productRequest.getShopId()));
        paramMap.put("shop_id", productRequest.getShopId());
        paramMap.put("partner_id", productRequest.getPartnerId());
        paramMap.put("access_token", productRequest.getToken());
        paramMap.put("item_id", productRequest.getItemId());
        ShopeeResponse shopeeResponse = ShopeeApiUtils.sendGet(productRequest.getHost() + path, paramMap);
        if (Objects.isNull(shopeeResponse) || Objects.isNull(shopeeResponse.getResponse())) {
            return null;
        }
        JSONObject response = shopeeResponse.getResponse();
        String error = response.getStr("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取产品明细参数异常：{}", error);
            return null;
        }
        //打印列表
        JSONArray modelList = response.getJSONArray("model");
        return JSONUtil.toList(modelList, ModelInfo.class);
    }
}
