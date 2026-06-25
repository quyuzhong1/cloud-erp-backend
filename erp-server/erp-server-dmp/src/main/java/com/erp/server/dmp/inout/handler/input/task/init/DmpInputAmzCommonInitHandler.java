package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Resource
    protected RedisUtil redisUtil;

    /**
     * 子任务 nextLevelId 会变成上游 mongoId，沿父任务链回溯拿根任务。
     */
    protected DmpInputTaskEntity resolveRootTaskByTaskChain() {
        return dmpInputTaskService.findRootTaskInChain(dmpInputTaskEntity);
    }

    /**
     * 子任务 nextLevelId 会变成上游 mongoId，沿父任务链回溯拿根任务店铺ID。
     */
    protected String resolveAuthShopIdByTaskChain() {
        DmpInputTaskEntity rootTask = resolveRootTaskByTaskChain();
        return rootTask == null ? "" : StringUtils.defaultString(rootTask.getNextLevelId());
    }

    protected String buildRateLimitKey(AmazonShopInfoDTO shopInfoDTO, AmazonRequestTypeRateLimiterEnum requestType) {
        return StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(),
                shopInfoDTO.getPlatformShopCode(), requestType.getBusinessTypeName());
    }

    protected boolean isRateLimited(String limitKey) {
        return redisUtil.get(limitKey) != null;
    }

    protected void disableNextStatus(DmpInputTaskResponse dmpResponse) {
        if (dmpResponse instanceof DmpInputInitResponse) {
            ((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
        }
    }

    protected void applyRateLimitBackoff(AmazonRequestTypeRateLimiterEnum requestType, String limitKey) {
        BigDecimal timeout = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(requestType.getRateLimit()), 8, RoundingMode.DOWN));
        redisUtil.set(limitKey, requestType.getRateLimit(), timeout.longValue());
    }

    protected boolean handleRateLimitAndCheckNeedStop(ApiException e,
                                                    AmazonRequestTypeRateLimiterEnum requestType,
                                                    String limitKey,
                                                    AmazonShopInfoDTO shopInfoDTO,
                                                    DmpInputTaskResponse dmpResponse,
                                                    String businessDesc) {
        if (e.getCode() != 429) {
            return false;
        }
        applyRateLimitBackoff(requestType, limitKey);
        log.warn("【{}】platformShopCode={},存在429等待恢复:放弃当前请求任务", businessDesc, shopInfoDTO.getPlatformShopCode());
        disableNextStatus(dmpResponse);
        return true;
    }

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
    protected boolean orderOtherCheckCanDoNextRequest(Map<String, Object>  mainMongo, String amazonOrderId) {
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
