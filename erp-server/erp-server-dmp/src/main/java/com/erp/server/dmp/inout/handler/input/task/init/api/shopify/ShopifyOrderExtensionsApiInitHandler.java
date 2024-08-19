package com.erp.server.dmp.inout.handler.input.task.init.api.shopify;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClient;
import com.sdk.oms.shopify.api.graphql.ShopifyGraphQLClientService;
import com.sdk.oms.shopify.api.graphql.model.ShopifyOrderResponse;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            //特定国家需要查询税号

            Object shippingAddressObj = findMongoDatum.get("shippingAddress");
            if (ObjectUtils.isNotEmpty(shippingAddressObj)) {
                Map<String, Object> shippingAddressMap = (Map<String, Object>) shippingAddressObj;

                Object country = shippingAddressMap.get("countryCode");
                log.error("ShopifyOrderTransactionsApiInitHandler国家：" + country + "  是否计算" + countryTaxMap.containsKey(country));
                if (countryTaxMap.containsKey(country)) {
                    String taxTitle = countryTaxMap.get(country);
                    System.setProperty("socksProxyHost", "127.0.0.1");
                    System.setProperty("socksProxyPort", "7890");
                    ShopifyGraphQLClient shopifyGraphQLClient = shopifyGraphQLClientService.getShopifyGraphQLClient(shopInfoDTO.getShopDomain(), shopInfoDTO.getAccessToken());
                    ShopifyOrderResponse order = shopifyGraphQLClient.getOrderLocalizationExtensions(findMongoDatum.get("orderId").toString());

                    List<ShopifyOrderResponse.Data.Node.LocalizationExtensions.Nodes> nodes = Optional.of(order)
                            .map(ShopifyOrderResponse::getData)
                            .map(ShopifyOrderResponse.Data::getNode)
                            .map(ShopifyOrderResponse.Data.Node::getLocalizationExtensions)
                            .map(ShopifyOrderResponse.Data.Node.LocalizationExtensions::getNodes)
                            .orElse(new ArrayList<>());
                    String taxNo = nodes.stream().filter(v -> taxTitle.equals(v.getTitle())).map(v -> v.getValue()).findFirst().orElse("");
                    order.setOrderId(findMongoDatum.get("orderId").toString());
                    order.setTaxNo(taxNo);

                    log.error("ShopifyOrderTransactionsApiInitHandler返回值：" + order);
                    DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
                    dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(order));
                    dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
                }

            }
        }
        return dmpInputTaskInitDTOList;
    }
}
