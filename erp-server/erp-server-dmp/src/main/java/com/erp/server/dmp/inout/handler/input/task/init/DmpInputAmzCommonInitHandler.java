package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 亚马逊公共init
 *
 * @author Administrator
 */
@Slf4j
@Service
public abstract class DmpInputAmzCommonInitHandler extends DmpInputInitHandler {


    /**
     * 获取上一级mongo数据
     */
    protected List<Map<String, Object>> getParentStorageMongoData() {
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isBlank(parentStorageName)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        return mongoService.findMongoData(paramDataList, parentStorageName);
    }

    /**
     * 解析当前店铺ID
     */
    protected String parseShopId(List<Map<String, Object>> findMongoData) {
        Object shopIdObj = findMongoData.get(0).get("shopId");
        if (null == shopIdObj) {
            ServiceException.runError("未找到店铺ID/nextLevelId");
        }
        // 店铺ID
        return (String) shopIdObj;
    }

    /**
     * 解析当前数据获取主订单ID 查询mongo主订单数据
     */
    protected List<Map<String, Object>> getMainOrderMongoDate(List<Map<String, Object>> findMongoData, String platformShopCode) {
        List<ParamData> paramDataList = new ArrayList<>();
        List<String> orderIdList = findMongoData.stream()
                .map(f -> f.getOrDefault("amazonOrderId", "").toString())
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderIdList)){
            ServiceException.runError("数据异常未找到mongo主订单数据:taskId=" + dmpInputTaskEntity.getId());
        }
        paramDataList.add(new ParamData("amazonOrderId", "amazonOrderId", PannoEnum.IN, orderIdList));
        paramDataList.add(new ParamData("platformShopCode", "platformShopCode", PannoEnum.EQ, platformShopCode));
        List<Map<String, Object>> mainMongoDataList = mongoService.findMongoData(paramDataList, "amazon_order_data");
        if (CollUtil.isEmpty(mainMongoDataList)) {
            ServiceException.runError("未找到主单数据, taskId=" + dmpInputTaskEntity.getId());
        }
        return mainMongoDataList;
    }


    /**
     * 校验和获取指定字段
     */
    protected String checkAndGetMongoValue(Map<String, Object> nongoObjectMap, String mongoFieldName) {
        Object reportDocumentIdObj = nongoObjectMap.get(mongoFieldName);
        if (null == reportDocumentIdObj) {
            String msg = StrUtil.format("未找到{}:taskId={}", mongoFieldName, dmpInputTaskEntity.getId());
            ServiceException.runError(msg);
        }
        return (String) reportDocumentIdObj;
    }


    /**
     * 当前主单信息
     */
    protected static Map<String, Object> checkAndGetMainMongoMap(List<Map<String, Object>> mainMongoDataList, String amazonOrderId) {
        Map<String, Object> mainMongo = mainMongoDataList.stream().filter(f -> f.get("amazonOrderId").toString().equalsIgnoreCase(amazonOrderId)).findFirst().orElse(null);
        // 检查来源
        if (null == mainMongo) {
            String msg = StrUtil.format("主订单信息为空:{}", amazonOrderId);
            throw new ServiceException(msg);
        }
        return mainMongo;
    }

    /**
     * 订单明细后
     * 检查是否执行下一步请求
     */
    protected boolean orderOtherCheckCanDoNextRequest(List<Map<String, Object>>  mainMongoDataList, String amazonOrderId) {
        Map<String, Object> mainMongo = checkAndGetMainMongoMap(mainMongoDataList, amazonOrderId);
        // 配送渠道
        String fulfillmentChannel = checkAndGetMongoValue(mainMongo, "fulfillmentChannel");
        // 销售渠道
        String salesChannel = checkAndGetMongoValue(mainMongo, "salesChannel");
        // 跳过FBA配送/多渠道订单拉取地址
        if (!Order.FulfillmentChannelEnum.MFN.getValue().equalsIgnoreCase(fulfillmentChannel)
                || salesChannel.contains("Non-Amazon")
        ){
            log.warn("FBA配送/多渠道订单拉取地址跳过， amazonOrderId={}", amazonOrderId);
            return true;
        }
        return false;
    }
}
