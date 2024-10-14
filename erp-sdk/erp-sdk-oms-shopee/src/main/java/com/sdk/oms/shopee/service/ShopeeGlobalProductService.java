package com.sdk.oms.shopee.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.sdk.oms.shopee.constants.ShopeeConstants;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.global.request.GlobalProductRequest;
import com.sdk.oms.shopee.dto.global.response.GlobalItem;
import com.sdk.oms.shopee.dto.global.response.GlobalItemInfo;
import com.sdk.oms.shopee.dto.product.request.ProductRequest;
import com.sdk.oms.shopee.dto.product.response.Item;
import com.sdk.oms.shopee.dto.product.response.ItemInfo;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sdk.oms.shopee.constants.ShopeeConstants.pageSize;

/**
 * @author zdy
 * @ClassName ShopeeGlobalProductService
 * @description: TODO
 * @date 2023年11月29日
 * @version: 1.0
 */
@Component
@Slf4j
public class ShopeeGlobalProductService {
    public static void main(String[] args) {
        ShopeeGlobalProductService service = new ShopeeGlobalProductService();
        GlobalProductRequest productRequest = GlobalProductRequest.builder()
                .merchantId(ShopeeConstants.merchant_id)
                .partnerId(ShopeeConstants.partner_id)
                .tmpPartnerKey(ShopeeConstants.tmp_partner_key)
                .token(ShopeeConstants.merchant_access_token)
                .host(ShopeeConstants.host)
                .offset(null)
                .build();
        ShopeeResponse productList = service.getGlobalProductList(productRequest);
        JSONObject response = productList.getResponse();
        String offset = response.getStr("offset");
        Boolean hasNextPage = response.getBool("has_next_page");
        Integer totalCount = response.getInt("total_count");
        JSONArray globalItemList = response.getJSONArray("global_item_list");
        List<GlobalItem> items = JSONUtil.toList(globalItemList, GlobalItem.class);
        List<Long> itemIds = items.stream().map(GlobalItem::getItemId).collect(Collectors.toList());
        productRequest.setItemIdList(StringUtils.join(itemIds, ","));
        ShopeeResponse globalProductInfo = service.getGlobalProductInfo(productRequest);
        JSONObject response1 = globalProductInfo.getResponse();
        JSONArray itemList = response1.getJSONArray("global_item_list");
        List<GlobalItemInfo> globalItemInfos = JSONUtil.toList(itemList, GlobalItemInfo.class);
        System.out.println(globalItemInfos.size());
    }

    public ShopeeResponse getGlobalProductList(GlobalProductRequest productRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/global_product/get_global_item_list";
        long timestamp = System.currentTimeMillis() / 1000L;
        paramMap.put("timestamp", timestamp);
        paramMap.put("sign", ShopeeApiUtils.getMerchantSign(path, productRequest.getToken(), productRequest.getPartnerId(),
                productRequest.getTmpPartnerKey(), productRequest.getMerchantId()));
        paramMap.put("merchant_id", productRequest.getMerchantId());
        paramMap.put("partner_id", productRequest.getPartnerId());
        paramMap.put("access_token", productRequest.getToken());
        if (StringUtils.isNotBlank(productRequest.getOffset())){
            paramMap.put("offset", productRequest.getOffset());
        }
        paramMap.put("page_size", 20);
        return ShopeeApiUtils.sendGet(productRequest.getHost() + path, paramMap);
    }

    public ShopeeResponse getGlobalProductInfo(GlobalProductRequest productRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        String path = "/api/v2/global_product/get_global_item_info";
        paramMap.put("timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        paramMap.put("sign", ShopeeApiUtils.getMerchantSign(path, productRequest.getToken(), productRequest.getPartnerId(),
                productRequest.getTmpPartnerKey(), productRequest.getMerchantId()));
        paramMap.put("merchant_id", productRequest.getMerchantId());
        paramMap.put("partner_id", productRequest.getPartnerId());
        paramMap.put("access_token", productRequest.getToken());
        paramMap.put("global_item_id_list", productRequest.getItemIdList());
        return ShopeeApiUtils.sendGet(productRequest.getHost() + path, paramMap);
    }
    public void getAllProduct(GlobalProductRequest productRequest, List<GlobalItemInfo> itemInfos) {
        log.info("获取产品订单：{}", productRequest);
        ShopeeResponse productList = this.getGlobalProductList(productRequest);
        if (Objects.isNull(productList) || Objects.isNull(productList.getResponse())) {
            return;
        }
        JSONObject response = productList.getResponse();
        String error = response.getStr("error");
        if (StringUtils.isNotEmpty(error)) {
            return;
        }
        JSONArray jsonArray = (JSONArray) response.get("global_item_list");
        if (Objects.isNull(jsonArray)){
            return;
        }
        //目录列表
        List<GlobalItem> items = JSONUtil.toList(jsonArray, GlobalItem.class);
        //获取item明细
        List<Long> itemIds = items.stream().map(GlobalItem::getItemId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(itemIds)) {
            productRequest.setItemIdList(StringUtils.join(itemIds, ","));
            ShopeeResponse productItemBaseInfo = this.getGlobalProductInfo(productRequest);
            JSONObject responseBaseInfo = productItemBaseInfo.getResponse();
            if (Objects.nonNull(responseBaseInfo)) {
                //循环填充
                JSONArray listBase = responseBaseInfo.getJSONArray("global_item_list");
                List<GlobalItemInfo> list = JSONUtil.toList(listBase, GlobalItemInfo.class);
                if (CollectionUtils.isNotEmpty(list)) {
                    itemInfos.addAll(list);
                }
            }
        }
        //是否还有数据
        boolean hasNextPage = response.getBool("has_next_page");
        if (hasNextPage) {
            String offset = response.getStr("offset");
            productRequest.setOffset(offset);
            getAllProduct(productRequest, itemInfos);
        }
    }
}
