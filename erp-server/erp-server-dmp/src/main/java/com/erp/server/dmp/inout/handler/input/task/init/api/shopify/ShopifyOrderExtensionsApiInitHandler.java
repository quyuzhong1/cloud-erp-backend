package com.erp.server.dmp.inout.handler.input.task.init.api.shopify;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClient;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.graphql.model.ShopifyOrderResponse;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class ShopifyOrderExtensionsApiInitHandler extends DmpInputInitHandler {
    /**
     * 国家与个人税号 字段map
     * key 国家code
     * val 税号标签title
     */
    private static final Map<String, String> countryTaxMap;

    static {
        countryTaxMap = new HashMap<>();
        countryTaxMap.put("BR", "CPF/CNPJ");
    }


    @Resource
    private ShopifyGraphQLClientService shopifyGraphQLClientService;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<Map<String, Object>> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (CollectionUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        ShopifyShopInfoDTO shopInfoDTO = shopSdkServer.getTokenAndDomainByShopId(findMongoData.get(0).get("nextLevelId").toString());
        if (null == shopInfoDTO) {
            log.error("[Shopify订单拓展信息下载]从缓存中获取shopify token 失败: shopId={}", findMongoData.get(0).get("nextLevelId").toString());
            throw new ServiceException();
        }


        for (Map<String, Object> findMongoDatum : findMongoData) {
            Object shippingAddressObj = findMongoDatum.get("shippingAddress");
            if (ObjectUtils.isEmpty(shippingAddressObj)) {
                continue;
            }
            Map<String, Object> shippingAddressMap = (Map<String, Object>) shippingAddressObj;
            String country = String.valueOf(shippingAddressMap.get("countryCode"));
            if (!countryTaxMap.containsKey(country)) {
                continue;
            }
            String orderId = resolveShopifyOrderId(findMongoDatum);
            if (StringUtils.isBlank(orderId)) {
                log.warn("[Shopify订单拓展信息]跳过缺少orderId/id的订单, parentTaskId={}, mongoKeys={}",
                        dmpInputTaskEntity.getParentTaskId(), findMongoDatum.keySet());
                continue;
            }
            String taxTitle = countryTaxMap.get(country);
            ShopifyGraphQLClient shopifyGraphQLClient = shopifyGraphQLClientService.getShopifyGraphQLClient(
                    shopInfoDTO.getShopDomain(), shopInfoDTO.getAccessToken());
            ShopifyOrderResponse order = shopifyGraphQLClient.getOrderLocalizationExtensions(orderId);
            if (order == null) {
                log.warn("[Shopify订单拓展信息]GraphQL查询失败, orderId={}, shopId={}", orderId, shopInfoDTO.getId());
                continue;
            }
            List<ShopifyOrderResponse.Data.Node.LocalizationExtensions.Nodes> nodes = Optional.of(order)
                    .map(ShopifyOrderResponse::getData)
                    .map(ShopifyOrderResponse.Data::getNode)
                    .map(ShopifyOrderResponse.Data.Node::getLocalizationExtensions)
                    .map(ShopifyOrderResponse.Data.Node.LocalizationExtensions::getNodes)
                    .orElse(new ArrayList<>());
            String taxNo = nodes.stream()
                    .filter(v -> taxTitle.equals(v.getTitle()))
                    .map(ShopifyOrderResponse.Data.Node.LocalizationExtensions.Nodes::getValue)
                    .findFirst()
                    .orElse("");
            order.setOrderId(orderId);
            order.setTaxNo(taxNo);

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(order));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
        return dmpInputTaskInitDTOList;
    }

    private static String resolveShopifyOrderId(Map<String, Object> mongoOrder) {
        for (String key : Arrays.asList("orderId", "id", "order_id")) {
            Object value = mongoOrder.get(key);
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                return value.toString();
            }
        }
        return null;
    }
}
