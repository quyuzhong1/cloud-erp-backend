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
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import com.sdk.oms.shopify.api.rest.model.ShopifyTransaction;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Scope("prototype")
public class ShopifyOrderTransactionsApiInitHandler extends DmpInputInitHandler {


    @Resource
    private ShopifyRestClientService shopifyRestClientService;

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

        List<String> orderIds = findMongoData.stream().map(req -> req.get("orderId").toString()).distinct().collect(Collectors.toList());

        ShopifyShopInfoDTO shopInfoDTO = shopSdkServer.getTokenAndDomainByShopId(findMongoData.get(0).get("nextLevelId").toString());
        if (null == shopInfoDTO) {
            log.error("[Shopify订单交易信息下载]从缓存中获取shopify token 失败: shopId={}", findMongoData.get(0).get("nextLevelId").toString());
            throw new ServiceException();
        }

        for (String orderId : orderIds) {
            List<ShopifyTransaction> transactionList = shopifyRestClientService.getShopifyRestClient(shopInfoDTO.getShopDomain(), shopInfoDTO.getAccessToken())
                    .getOrderTransactions(orderId);
            if (CollectionUtils.isEmpty(transactionList)) {
                continue;
            }

            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSONArray.toJSONString(transactionList));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        }
        return dmpInputTaskInitDTOList;
    }
}
