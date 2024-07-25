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

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

//  根据店铺ID获取授权
        ShopifyShopInfoDTO tokenDTO = ShopSdkServer.getTokenAndDomainByShopId(nextLevelId);
        if (null == tokenDTO) {
            log.error("[Shopify产品下载]从缓存中获取shopify token 失败: shopId={}", nextLevelId);
            return Collections.emptyList();
        }
        String shopifyShopDomain = tokenDTO.getShopDomain();
        String accessToken = tokenDTO.getAccessToken();

// Shopify产品下载所有(SDK已分页查询所有)
        ShopifyProducts products = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken).getProducts();
        if (CollectionUtils.isEmpty(products.values())) {
            return Collections.emptyList();
        }


        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(products.values()));
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
    }


}
