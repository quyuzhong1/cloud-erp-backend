package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.GetOrdersResponse;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputAmzOrderApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取店铺信息
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        // 校验当前是否429限流
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getDictCountryCode(), BusinessTypeEnum.ORDER.getCode());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            String msg = StrUtil.format("【订单拉取】 PlatformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            throw new ServiceException(msg);
        }

        // 转换请求参数
        // 当前站点
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 是否检查当前时间
        Boolean autoCheckNow = dmpCfgInputEntity.parseExtendAutoCheckNow();
        // 开始时间
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        // 结束时间
        LocalDateTime endTime = checkAndConvertEntTime(autoCheckNow);

        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_LIST.getRateLimit();
        // 根据亚马逊的响应时间记录下次执行开始时间
        LocalDateTime nextStartTime;
        // 亚马逊订单下载
        OrdersV0Api api = OrdersV0Api.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        // 正式环境请求
        String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(startTime).toString();
        String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(endTime).toString();
        try {
            // 请求全站点
            List<String> marketplaceIds = new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
            // 发起请求
            ApiResponse<GetOrdersResponse> ordersWithHttpInfo = api.getOrdersWithHttpInfo(marketplaceIds,
                    null, null, lastUpdatedAfter, lastUpdatedBefore, null, null, null, null, null, 100,
                    null, null, null, null, null, null, null, null, null, null, null);
            List<String> limitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
            rateLimitStr = limitArray.get(0);
            GetOrdersResponse orders = ordersWithHttpInfo.getData();
            // 亚马逊接口响应时间UTC转换8区
            LocalDateTime parse = LocalDateTime.parse(orders.getPayload().getLastUpdatedBefore(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            nextStartTime = DateUtil.utcSamePlus8(parse);

            List<Order> orderList = new LinkedList<>(orders.getPayload().getOrders());
            String currentNextToken = orders.getPayload().getNextToken();
            int currentSize = orders.getPayload().getOrders().size();
            while (StringUtils.isNotBlank(currentNextToken) && currentSize == 100) {
                GetOrdersResponse currentResp = api.getOrders(marketplaceIds, null, null, null, null, null, null, null, null, null, 100, null, null, currentNextToken, null, null, null, null, null, null, null, null);
                orderList.addAll(currentResp.getPayload().getOrders());
                currentNextToken = currentResp.getPayload().getNextToken();
                currentSize = currentResp.getPayload().getOrders().size();
                List<String> currentLimitArray = ordersWithHttpInfo.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                rateLimitStr = currentLimitArray.get(0);
            }
            // TODO
            // 反写下次任务开始时间

            // 返回下载源数据
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(orderList)));
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new RuntimeException("请求亚马逊SP-APi订单api异常失败,body=" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new RuntimeException("请求亚马逊SP-APi订单失败,body=" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 检查并转换结束时间
     */
    public LocalDateTime checkAndConvertEntTime(Boolean autoCheckNow) {
        LocalDateTime endTime = dmpCfgInputDetailEntity.getNextTime();

        // 正常任务对比当前时间(最大间隙取1个小时)自动补充中断情况
        if (DmpInputTaskTaskTypeEnum.NORMAL.getCode().equalsIgnoreCase(dmpCfgInputDetailEntity.getTaskType())
                && null != autoCheckNow
                && autoCheckNow
        ){
            // 期望的目标时间 = 当前时间-延时时间
            LocalDateTime targetNow = LocalDateTime.now().minusSeconds(dmpCfgInputDetailEntity.getDealyTime());
            // 间隔分钟
            long minutesDifference = Duration.between(targetNow, dmpCfgInputDetailEntity.getNextTime()).toMinutes();

            if (minutesDifference >= 60) {
                // 间隔时间超过480分钟按480分钟间隔拉取
                endTime = dmpCfgInputDetailEntity.getNextTime().plusMinutes(60);
            } else if (minutesDifference > dmpCfgInputDetailEntity.getIntervalTime() * 2) {
                // 正常任务结束时间超过间隔时间2倍按当时期望时间
                endTime = targetNow;
            }
        }
        return endTime;
    }


}
