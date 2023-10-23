package com.sdk.oms.shopee.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.dto.PlatformProductDTO;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.product.response.Item;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        List<PlatformProductDTO> productList = shopeeProductService.getAllProduct(host, shop_access_token, shop_id, partner_id, tmp_partner_key);
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
    public List<PlatformProductDTO> getAllProduct(String host, String token, long shopId, long partner_id, String tmp_partner_key){
//        ShopeeProductService shopeeProductService = new ShopeeProductService();
        ShopeeResponse productList = this.getProductList(host, token, shopId, partner_id, tmp_partner_key);
        JSONObject response = productList.getResponse();
        String error = response.getString("error");
        if (StringUtils.isNotEmpty(error)){
            return Collections.emptyList();
        }
        JSONArray jsonArray = (JSONArray) response.get("item");
        //目录列表
        List<Item> items = JSONObject.parseArray(jsonArray.toJSONString(), Item.class);
        //获取item明细
        List<Long> itemIds = items.stream().map(Item::getItemId).collect(Collectors.toList());
        ShopeeResponse productItemBaseInfo = this.getProductItemBaseInfo(host, token, shopId, partner_id, tmp_partner_key, StringUtils.join(itemIds, ","));
        JSONObject responseBaseInfo = productItemBaseInfo.getResponse();
        //循环填充
        JSONArray listBase = responseBaseInfo.getJSONArray("item_list");
        List<PlatformProductDTO> list = new ArrayList<>();
        listBase.forEach(o->{
            JSONObject jsonObject = (JSONObject) o;
            jsonObject.getJSONObject("description_info").getJSONObject("extended_description").getJSONArray("field_list").get(0);
            Long updateTime = jsonObject.getLong("update_time");
            Instant instant = Instant.ofEpochMilli(updateTime);
            ZoneId zone = ZoneId.systemDefault();
            PlatformProductDTO dto = new PlatformProductDTO()
                    .setPlatformType("platform")
                    .setPlatformProductNo(jsonObject.getString("item_sku"))
                    .setPlatformProductName(jsonObject.getString("item_name"))
                    .setProductPacking(jsonObject.getJSONObject("dimension").toJSONString())
                    .setProductSpec(jsonObject.getString("category_id"))
                    .setProductImageUrl(jsonObject.getJSONObject("image").getJSONArray("image_url_list").get(0).toString())
                    .setPlatformUpdateTime(LocalDateTime.ofInstant(instant, zone));
            list.add(dto);
        });
//        System.out.println(responseBaseInfo);
//        ShopeeResponse productItemExtraInfo = this.getProductItemExtraInfo(host, token, shopId, partner_id, tmp_partner_key, StringUtils.join(itemIds, ","));
//        JSONObject responseExtraInfo = productItemExtraInfo.getResponse();
//        System.out.println(responseExtraInfo);


        return list;
    }

    public ShopeeResponse getProductList(String host, String token, long shopId, long partner_id, String tmp_partner_key) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_list";
        long timest = System.currentTimeMillis() / 1000L;
        Long time_from = timest - (3600 * 24 * 14);
        Long time_to = timest;

        paramMap.put("timestamp", timest);
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, token, partner_id, tmp_partner_key, shopId));
        paramMap.put("shop_id", shopId);
        paramMap.put("partner_id", partner_id);
        paramMap.put("access_token", token);

        paramMap.put("offset", 0);
        paramMap.put("page_size", 10);
//        paramMap.put("update_time_from", time_from);
//        paramMap.put("update_time_to", time_to);
        paramMap.put("item_status", "NORMAL");
        return ShopeeApiUtils.sendGet(host + path, paramMap);
    }

    public ShopeeResponse getProductItemBaseInfo(String host, String token, long shopId, long partner_id, String tmp_partner_key, String itemIdList) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_base_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, token,partner_id, tmp_partner_key,shopId));
        paramMap.put("shop_id", shopId);
        paramMap.put("partner_id", partner_id);
        paramMap.put("access_token", token);
        paramMap.put("item_id_list", itemIdList);
        paramMap.put("need_complaint_policy", true);
        paramMap.put("need_tax_info", true);
        return ShopeeApiUtils.sendGet(host + path, paramMap);
    }

    public ShopeeResponse getProductItemExtraInfo(String host, String token, long shopId, long partner_id, String tmp_partner_key, String itemIdList) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/product/get_item_extra_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(path, token,partner_id, tmp_partner_key,shopId));
        paramMap.put("shop_id", shopId);
        paramMap.put("partner_id", partner_id);
        paramMap.put("access_token", token);
        paramMap.put("item_id_list", itemIdList);
        return ShopeeApiUtils.sendGet(host + path, paramMap);
    }

}
