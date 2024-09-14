package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.orders.Address;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderBuyerInfo;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderAddressInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private AmazonOrderHandler amazonOrderHandler;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取上一级mongo数据
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollUtil.isEmpty(findMongoData)) {
            return Collections.emptyList();
        }
        // 解析到当前店铺ID
        String shopId = parseShopId(findMongoData);
        // 需要查询的店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);

        // 主单信息
        List<Map<String, Object>> mainMongoDataList = getMainOrderMongoDate(findMongoData, shopInfoDTO.getPlatformShopCode());

        List<JSONObject> addressJsonList = new LinkedList<>();
        for (Map<String, Object> mongoData : findMongoData) {
            // 主单ID
            String amazonOrderId = checkAndGetMongoValue(mongoData, "amazonOrderId");
            // 检查是否查询
            if (orderOtherCheckCanDoNextRequest(mainMongoDataList, amazonOrderId)) {
                log.warn("FBA或多渠道订单不获取地址信息:{}", amazonOrderId);
                continue;
            }
            OrdersV0Api ordersVoApi = AmazonSpApiInitUtils.create(OrdersV0Api.class, shopInfoDTO, false);

            // 生成RDT权限获取地址信息
            // amazon-rdt-token:店铺ID:订单ID
            String rdtToken = amazonOrderHandler.queryAndGetRDT(amazonOrderId, shopInfoDTO);

            // 修改x-amz-access-token的token
            ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);

            // 查询买家信息
            OrderBuyerInfo buyerInfo = amazonOrderHandler.downloadBuyer(shopInfoDTO.getPlatformShopCode(), amazonOrderId, ordersVoApi);

            // 设置地址
            Address shippingAddress = amazonOrderHandler.downloadAddress(shopInfoDTO.getPlatformShopCode(), amazonOrderId, ordersVoApi);

            // 合并转json
            JSONObject jsonObject = setAmazonOrderIdAndToJsonObject(shippingAddress, buyerInfo, amazonOrderId, shopInfoDTO.getPlatformShopCode());

            addressJsonList.add(jsonObject);
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(addressJsonList)));
    }


    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Address address, OrderBuyerInfo buyerInfo, String amazonOrderId, String platformShopCode) {
        JSONObject json = (JSONObject) JSON.toJSON(address);
        json.put("amazonOrderId", amazonOrderId);
        json.put("platformShopCode", platformShopCode);
        JSONObject jsonBuyer = (JSONObject) JSON.toJSON(buyerInfo);
        json.put("buyerInfo", jsonBuyer);
        return json;
    }
}
