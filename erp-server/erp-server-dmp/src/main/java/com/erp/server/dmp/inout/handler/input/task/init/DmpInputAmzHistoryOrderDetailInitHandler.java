package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItem;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
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
public class DmpInputAmzHistoryOrderDetailInitHandler extends DmpInputAmzHistoryOrderAbstractApiInitHandler {
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取上一级mongo数据
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollUtil.isEmpty(findMongoData)) {
            return Collections.emptyList();
        }
        // 店铺信息
        String shopId = parseShopId(findMongoData);
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
        List<ShopInfoEntity> relatedshopInfoList = shopInfoFeign.getRelatedShopById(shopInfo);
        AmazonShopInfoDTO shopInfoDTO = initShopInfoDTO(shopInfo,  relatedshopInfoList);

        List<JSONObject> curJsonList = new LinkedList<>();

        List<String> uniqueIdList = findMongoData.stream()
                .map(e -> e.getOrDefault("uniqueId", "").toString())
                .distinct()
                .collect(Collectors.toList());

        List<PlatformAmazonOrderDTO> orderMongoData = findAllOrderMongoData(uniqueIdList);
        if (CollectionUtils.isEmpty(orderMongoData)){
            ServiceException.runError("找不到mongo订单明细");
        }
        for (PlatformAmazonOrderDTO orderMongoDatum : orderMongoData) {
            for (OrderItem detail : orderMongoDatum.getDetails()) {
                JSONObject jsonObject = setAmazonOrderIdAndToJsonObject(orderMongoDatum.getOrder(),
                        detail,
                        orderMongoDatum.getOrder().getAmazonOrderId(),
                        orderMongoDatum.getPlatformShopCode(),
                        shopInfoDTO
                );
                curJsonList.add(jsonObject);
            }
        }

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(curJsonList)));
    }

    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Order entity, OrderItem orderItem, String amazonOrderId, String platformShopCode, AmazonShopInfoDTO shopInfoDTO) {
        AmazonShopInfoDTO.ShopNameDTO shopNameDTO = shopInfoDTO.getMarketplaceShopIdMap().get(entity.getMarketplaceId());
        JSONObject json = (JSONObject) JSON.toJSON(orderItem);
        json.put("amazonOrderId", amazonOrderId);
        json.put("platformShopCode", platformShopCode);
        json.put("shopId", shopNameDTO.getShopId());
        json.put("shopName", shopNameDTO.getShopName());
        return json;
    }

}
