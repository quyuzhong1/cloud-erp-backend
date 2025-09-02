package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.FulfillmentOrder;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.GetFulfillmentOrderResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.GetFulfillmentOrderResult;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.ListAllFulfillmentOrdersResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFulFillOrderApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String queryShopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(queryShopId);
        // 指定FBA货件号
        List<String> platformCodeList = new ArrayList<>();
        String extendJson = dmpInputTaskEntity.getExtendJson();
//        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 目标店铺
        String shopId = shopInfoDTO.getDictCountryCode();
        String shopName = shopInfoDTO.getDictCountryCode();

        // 兼容手动拉取参数
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject jsonObject = JSONObject.parseObject(extendJson);
            JSONArray jsonArray = jsonObject.getJSONArray("platformCodeList");
            if (CollectionUtils.isNotEmpty(jsonArray)) {
                platformCodeList = jsonArray.stream().map(Object::toString).collect(Collectors.toList());
            }
            String specShopId = jsonObject.getString("shopId");
            if (StringUtils.isNotBlank(specShopId)) {
                shopId = specShopId;
                Map.Entry<String, AmazonShopInfoDTO.ShopNameDTO> entry = shopInfoDTO.getMarketplaceShopIdMap()
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getShopId().equalsIgnoreCase(specShopId))
                        .findFirst()
                        .orElse(null);
                if (null != entry) {
                    shopName = entry.getValue().getShopName();
//                    marketPlaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(entry.getKey());
                }
            }
        }

        // 限流信息相关
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FULFILL_ORDER;
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();
        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 默认请求速率配置
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【亚马逊履行订单拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        if (CollUtil.isNotEmpty(platformCodeList)) {
            // 根据货件单号查询FBA货件
            return queryListByPlatformCodeList(platformCodeList, shopInfoDTO, rateLimitStr, limitKey, dmpResponse, shopId, shopName);
        } else {
            // 根据时间区间查询FBA货件
            return queryListByDateRange(shopInfoDTO, rateLimitStr, limitKey, dmpResponse, shopId, shopName);
        }

    }

    /**
     * 根据货件单号查询FBA货件
     */
    private List<DmpInputTaskInitDTO> queryListByPlatformCodeList(List<String> platformCodeList, AmazonShopInfoDTO shopInfoDTO, String rateLimitStr, String limitKey, DmpInputTaskResponse dmpResponse, String shopId, String shopName) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        for (String platformCode : platformCodeList) {
            try {
                // 请求亚马逊接口
                GetFulfillmentOrderResponse response = api.getFulfillmentOrder(platformCode);
                GetFulfillmentOrderResult payload = response.getPayload();
                if (Objects.nonNull(payload)) {
                    JSONObject curJson = fillDataAndToJsonObject(payload, shopInfoDTO.getPlatformShopCode(), shopId, shopName);
                    dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJson)));
                }
            } catch (ApiException | LWAException e) {
                if (e instanceof ApiException && 429 == ((ApiException) e).getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                    log.warn("【亚马逊履行订单拉取】按单号 platformCode={},存在429等待恢复:放弃当前请求任务", platformCode);
                    // 触发限流不执行当前
                    DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                    initDmpResponse.setDoNextStatus(false);
                }
            }
            // 间隔100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("【亚马逊履行订单拉取】按单号 platformCode={},线程睡眠异常", platformCode, e);
            }
        }
        return dmpInputTaskInitDTOList;
    }

    /**
     * 根据时间区间查询FBA货件
     */
    private List<DmpInputTaskInitDTO> queryListByDateRange(AmazonShopInfoDTO shopInfoDTO, String rateLimitStr, String limitKey, DmpInputTaskResponse dmpResponse, String shopId, String shopName) {
        // 开始时间
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        try {
            FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
            // 正式环境请求
            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(startTime).toString();
            ListAllFulfillmentOrdersResponse ordersResponse = api.listAllFulfillmentOrders(lastUpdatedAfter, null);
            List<FulfillmentOrder> fulfillmentOrders = ordersResponse.getPayload().getFulfillmentOrders();
            if (CollectionUtil.isNotEmpty(fulfillmentOrders)) {
                List<String> platformCodeList = fulfillmentOrders.stream().map(FulfillmentOrder::getSellerFulfillmentOrderId).collect(Collectors.toList());
                return queryListByPlatformCodeList(platformCodeList, shopInfoDTO, rateLimitStr, limitKey, dmpResponse, shopId, shopName);
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ServiceException("时间区间查询亚马逊发货订单列表失败：API异常：" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 设置亚马逊货件ID和转换JSON
     */
    private JSONObject fillDataAndToJsonObject(GetFulfillmentOrderResult result, String platformShopCode, String shopId, String shopName) {
        JSONObject json = (JSONObject) JSON.toJSON(result);
        json.put("code", result.getFulfillmentOrder().getSellerFulfillmentOrderId());
        json.put("platformShopCode", platformShopCode);
        json.put("shopId", shopId);
        json.put("shopName", shopName);
        return json;
    }

}
