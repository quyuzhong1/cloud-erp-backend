package com.erp.server.dmp.inout.handler.input.task.init.api.shopify;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTikTokApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyPage;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.OrderDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrdersBean;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Scope("prototype")
public class ShopifyOrderApiInitHandler implements DmpInputApiInitHandler {
    public static final String ORDER_ID_LIST = "orderIdList";
    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        // Shopify订单下载
        ShopifyShopInfoDTO shopInfoDTO = shopSdkServer.getTokenAndDomainByShopId(nextLevelId);
        if (null == shopInfoDTO) {
            log.error("[Shopify订单下载]从缓存中获取shopify token 失败: shopId={}", nextLevelId);
            return Collections.emptyList();
        }

        List<ShopifyOrder> orders;
        // 兼容指定订单IDS查询
        List<String> orderIds = checkAndGetOrderIds(dmpInputApiInitRequest.getTaskExtendJson());
        if (CollectionUtils.isEmpty(orderIds)){
             // 根据时间区间查询
            orders = getOrdersByTimes(dmpInputApiInitRequest, shopInfoDTO);
        } else {
            // 根据订单IDS查询
            orders = getByOrderIds(shopInfoDTO, orderIds);
        }

        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(orders));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
    }



    /**
     * 根据时间区间查询
     */
    private List<ShopifyOrder> getOrdersByTimes(DmpInputApiInitRequest dmpInputApiInitRequest, ShopifyShopInfoDTO shopInfoDTO) {
        String shopifyShopDomain = shopInfoDTO.getShopDomain();
        String accessToken = shopInfoDTO.getAccessToken();

        ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
        // 上次执行时间
        OffsetDateTime lastOffSetTime = dmpInputApiInitRequest.getStartTime().atOffset(zoneOffset);
        // 下次执行时间
        OffsetDateTime nextOffSetTime = dmpInputApiInitRequest.getEndTime().atOffset(zoneOffset);

        // Shopify产品下载所有(SDK已分页查询所有)
        return shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getAllUpdatedOrdersCreatedBefore(lastOffSetTime, nextOffSetTime, null);
    }

    /**
     * 根据订单IDS查询
     */
    private List<ShopifyOrder> getByOrderIds(ShopifyShopInfoDTO shopInfoDTO, List<String> orderIds) {
        String shopifyShopDomain = shopInfoDTO.getShopDomain();
        String accessToken = shopInfoDTO.getAccessToken();
        return shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getOrderByIds(orderIds);
    }

    /**
     * 校验和获取订单IDS
     */
    public List<String> checkAndGetOrderIds(String extendJson) {
        if(StringUtils.isBlank(extendJson)) {
            return Collections.emptyList();
        }
        JSONObject parseObject = JSON.parseObject(extendJson);
        if(null == parseObject) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = parseObject.getJSONArray(ORDER_ID_LIST);
        if (CollectionUtils.isEmpty(jsonArray)){
            return Collections.emptyList();
        }
        return jsonArray.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
