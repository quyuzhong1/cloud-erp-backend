package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.Dict;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.GetOrderItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItem;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItemList;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderItemsList;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderDetailInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取上一级mongo数据
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollUtil.isEmpty(findMongoData)) {
            return Collections.emptyList();
        }
        // 店铺信息
        String shopId = parseShopId(findMongoData);
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        for (Map<String, Object> findMongo : findMongoData) {
            String amazonOrderId = checkAndGetMongoValue(findMongo, "amazonOrderId");
            // 主表店铺ID(已解析成功的ID)
            String currentShopId = checkAndGetMongoValue(findMongo, "shopId");
            // 唯一键
            String uniqueId = CharSequenceUtil.format("{}_{}", amazonOrderId, shopId);

            // 检查来源
            // 缓存获取结果
            String amazonOrderIdResultKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS.getBusinessTypeName(), uniqueId);
            Object resultObj = redisUtil.get(amazonOrderIdResultKey);
            if (null != resultObj) {
                List<JSONObject> curItemList = JSONUtil.toList(resultObj.toString(), JSONObject.class);
                dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curItemList)));
                continue;
            }

            AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ITEMS;
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
            // 默认请求速率配置
            // 获取动态速率
            Object limitObj = redisUtil.get(limitKey);
            if (null != limitObj) {
                log.warn("【订单明细拉取】 amazonOrderId={}, platformShopCode={},存在429等待恢复:放弃当前请求任务", amazonOrderId, shopInfoDTO.getPlatformShopCode());
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            OrdersV0Api ordersVoApi = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
            OrderItemList curOrderItems = null;
            try {
                // 查询订单详情
                ApiResponse<GetOrderItemsResponse> itemResponse = ordersVoApi.getOrderItemsWithHttpInfo(amazonOrderId, null);
                List<String> limitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = limitArray.get(0);
                GetOrderItemsResponse orderItems = itemResponse.getData();

                String currentNextToken = orderItems.getPayload().getNextToken();
                OrderItemList resultOrderItemsList = orderItems.getPayload().getOrderItems();
                while (StringUtils.isNotBlank(currentNextToken)) {
                    ApiResponse<GetOrderItemsResponse> currentOrderItemsResp = ordersVoApi.getOrderItemsWithHttpInfo(amazonOrderId, currentNextToken);
                    GetOrderItemsResponse currentOrderItems = currentOrderItemsResp.getData();
                    List<String> currentLimitArray = itemResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                    rateLimitStr = currentLimitArray.get(0);
                    resultOrderItemsList.addAll(currentOrderItems.getPayload().getOrderItems());
                    currentNextToken = currentOrderItems.getPayload().getNextToken();
                }
                curOrderItems = resultOrderItemsList;
            } catch (ApiException e) {
                if (429 == e.getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                    log.warn("【DmpInputAmzOrderDetailInitHandler】查询亚马逊订单详情本次首次429限流:{}", shopInfoDTO.getPlatformShopCode());
                    DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                    initDmpResponse.setDoNextStatus(false);
                    return Collections.emptyList();
                }
                throw new ServiceException("查询亚马逊订单详情失败：API异常：" + JSONUtil.toJsonStr(e));
            } catch (Exception e) {
                throw new ServiceException("查询亚马逊订单详情失败：" + JSONUtil.toJsonStr(e));
            }
            if (CollectionUtils.isEmpty(curOrderItems)) {
                continue;
            }
            List<JSONObject> curJsonList = curOrderItems.stream().map(e -> setAmazonOrderIdAndToJsonObject(e, amazonOrderId, shopInfoDTO.getPlatformShopCode(), currentShopId)).collect(Collectors.toList());
            dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
            // 缓存倒redis
            redisUtil.set(amazonOrderIdResultKey, JSONArray.toJSONString(curJsonList), 600);
            log.warn("查询亚马逊订单详情成功, amazonOrderId={}, platformShopCode={}", amazonOrderId, shopInfoDTO.getPlatformShopCode());
        }
        return dmpInputTaskInitDTOList;
    }

    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(OrderItem orderItem, String amazonOrderId, String platformShopCode, String currentShopId) {
        JSONObject json = (JSONObject) JSON.toJSON(orderItem);
        json.put("amazonOrderId", amazonOrderId);
        json.put("platformShopCode", platformShopCode);
        json.put("shopId", currentShopId);
        return json;
    }

}
