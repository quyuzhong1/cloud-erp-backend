package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentInfo;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.AmazonDownloadService;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaShipmentApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private AmazonDownloadService amazonDownloadService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 限流信息相关
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT;
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();
        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 默认请求速率配置
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【FBA货件列表拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            String msg = StrUtil.format("【FBA货件列表拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            throw new ServiceException(msg);
        }

        // 是否检查当前时间
        Boolean autoCheckNow = dmpCfgInputEntity.parseExtendAutoCheckNow();
        // 开始时间
        LocalDateTime startTime = dmpCfgInputDetailEntity.getLastTime();
        // 结束时间
        LocalDateTime endTime = checkAndConvertEntTime(autoCheckNow);

        // 仓储中心配置
        Map<String, String> centerMap = amazonDownloadService.feignQueryFulfillmentCenterlist(Collections.emptyList())
                    .stream()
                    .collect(Collectors.toMap(CfgAmzFulfillmentCenterEntity::getCode, CfgAmzFulfillmentCenterEntity::getCountry));

        try {
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            String queryType = AmazonFbaQueryTypeEnum.DATE_RANGE.getCode();
            String marketplaceId = marketPlaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            // 正式环境请求
            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(startTime).toString();
            String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(endTime).toString();
            InboundShipmentList responseList = api.getAllShipments(queryType, marketplaceId, shipmentStatusList, null, lastUpdatedAfter, lastUpdatedBefore, null);
            List<JSONObject> curJsonList = responseList.stream()
                    .map(e -> fillDataAndToJsonObject(e, shopInfoDTO, centerMap))
                    .collect(Collectors.toList());
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
            }
            throw new ServiceException("查询亚马逊订单详情失败：API异常：" + JSONUtil.toJsonStr(e));
        }

    }

    /**
     * 设置亚马逊货件ID和转换JSON
     */
    private JSONObject fillDataAndToJsonObject(InboundShipmentInfo item, AmazonShopInfoDTO shopInfoDTO, Map<String, String> centerMap) {
        JSONObject json = (JSONObject) JSON.toJSON(item);
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        String shopId = shopInfoDTO.getId();
        String shopName = shopInfoDTO.getName();
        // 按仓储中心解析当前店铺
        String centerCountry = centerMap.get(item.getDestinationFulfillmentCenterId());
        if (StringUtils.isBlank(centerCountry)){
            log.warn("未找到仓储中心: platformShopCode={}, center={}", shopInfoDTO.getPlatformShopCode(), item.getDestinationFulfillmentCenterId());
        } else {
            Map<String, AmazonShopInfoDTO.ShopNameDTO> marketplaceShopIdMap = shopInfoDTO.getMarketplaceShopIdMap();
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(centerCountry);
            AmazonShopInfoDTO.ShopNameDTO shopNameDTO = marketplaceShopIdMap.get(marketplaceEnum.getMarketplaceId());
            shopId = shopNameDTO.getShopId();
            shopName = shopNameDTO.getShopName();
        }
        json.put("shopId", shopId);
        json.put("shopName", shopName);
        return json;
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
        ) {
            // 期望的目标时间 = 当前时间-延时时间
            LocalDateTime targetNow = LocalDateTime.now().minusSeconds(dmpCfgInputDetailEntity.getDealyTime());
            // 间隔分钟
            long minutesDifference = Duration.between(targetNow, dmpCfgInputDetailEntity.getNextTime()).toMinutes();

            if (minutesDifference >= 480) {
                // 间隔时间超过480分钟按480分钟间隔拉取
                endTime = dmpCfgInputDetailEntity.getNextTime().plusMinutes(480);
            } else if (minutesDifference > dmpCfgInputDetailEntity.getIntervalTime() * 2) {
                // 正常任务结束时间超过间隔时间2倍按当时期望时间
                endTime = targetNow;
            }
        }
        return endTime;
    }

}
