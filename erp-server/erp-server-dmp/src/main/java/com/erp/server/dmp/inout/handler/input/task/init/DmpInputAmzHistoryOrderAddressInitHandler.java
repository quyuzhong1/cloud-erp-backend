package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzHistoryOrderAddressInitHandler extends DmpInputAmzHistoryOrderAbstractApiInitHandler {
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取上一级mongo数据
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollUtil.isEmpty(findMongoData)) {
            return Collections.emptyList();
        }

        List<JSONObject> curJsonList = new LinkedList<>();

        List<String> uniqueIdList = findMongoData.stream()
                .map(e -> e.getOrDefault("uniqueId", "").toString())
                .distinct()
                .collect(Collectors.toList());

        List<PlatformAmazonOrderDTO> orderMongoData = findAllOrderMongoData(uniqueIdList);
        if (CollectionUtils.isEmpty(orderMongoData)) {
            ServiceException.runError("找不到mongo订单明细");
        }
        for (PlatformAmazonOrderDTO orderMongoDatum : orderMongoData) {
            Address shippingAddress = orderMongoDatum.getOrder().getShippingAddress();
            if (null == shippingAddress) {
                continue;
            }
            BuyerInfo buyerInfo = orderMongoDatum.getOrder().getBuyerInfo();
            JSONObject jsonObject = setAmazonOrderIdAndToJsonObject(shippingAddress,
                    buyerInfo,
                    orderMongoDatum.getOrder().getAmazonOrderId(),
                    orderMongoDatum.getPlatformShopCode()
            );
            curJsonList.add(jsonObject);
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(curJsonList)));
    }


    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Address address, BuyerInfo buyerInfo, String amazonOrderId, String platformShopCode) {
        JSONObject json = (JSONObject) JSON.toJSON(address);
        json.put("amazonOrderId", amazonOrderId);
        json.put("platformShopCode", platformShopCode);
        JSONObject jsonBuyer = (JSONObject) JSON.toJSON(buyerInfo);
        json.put("buyerInfo", jsonBuyer);
        return json;
    }



}
