package com.erp.server.dmp.inout.handler.input.task.init;

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
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentInfo;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
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
import java.util.Collections;
import java.util.LinkedList;
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

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String queryShopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(queryShopId);
        // 指定FBA货件号
        List<String> fbaShipmentIds = new LinkedList<>();
        String extendJson = dmpInputTaskEntity.getExtendJson();
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 目标店铺
        String shopId = shopInfoDTO.getDictCountryCode();
        String shopName = shopInfoDTO.getDictCountryCode();

        // 兼容手动拉取参数
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject jsonObject = JSONObject.parseObject(extendJson);
            JSONArray jsonArray = jsonObject.getJSONArray("shipmentCodeList");
            if (CollectionUtils.isNotEmpty(jsonArray)){
                fbaShipmentIds = jsonArray.stream().map(Object::toString).collect(Collectors.toList());
            }
            String specShopId = jsonObject.getString("shopId");
            if (StringUtils.isNotBlank(specShopId)){
                shopId = specShopId;
                Map.Entry<String, AmazonShopInfoDTO.ShopNameDTO> entry = shopInfoDTO.getMarketplaceShopIdMap()
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getShopId().equalsIgnoreCase(specShopId))
                        .findFirst()
                        .orElse(null);
                if (null != entry){
                    shopName = entry.getValue().getShopName();
                    marketPlaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(entry.getKey());
                }
            }
        }

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
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        if (CollectionUtils.isNotEmpty(fbaShipmentIds)) {
            // 根据货件单号查询FBA货件
            return queryListByShipmentIds(fbaShipmentIds, shopInfoDTO, marketPlaceEnum, rateLimitStr, limitKey, dmpResponse, shopId, shopName);
        } else {
            // 根据时间区间查询FBA货件
            return queryListByDateRange(shopInfoDTO, marketPlaceEnum, rateLimitStr, limitKey, dmpResponse);
        }

    }

    /**
     * 根据货件单号查询FBA货件
     */
    private List<DmpInputTaskInitDTO> queryListByShipmentIds(List<String> shipmentIdList, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketPlaceEnum, String rateLimitStr, String limitKey, DmpInputTaskResponse dmpResponse, String shopId, String shopName) {
        try {
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            String queryType = AmazonFbaQueryTypeEnum.SHIPMENT.getCode();
            String marketplaceId = marketPlaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            // 请求亚马逊接口
            GetShipmentsResponse shipments = api.getShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, null, null, null);

            InboundShipmentList responseList = shipments.getPayload().getShipmentData();
            List<JSONObject> curJsonList = responseList.stream()
                    .map(e -> fillDataAndToJsonObject(e, shopInfoDTO.getPlatformShopCode(), shopId, shopName))
                    .collect(Collectors.toList());
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【FBA货件列表拉取】按货件号 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("指定货件单号查询亚马逊FBA货件失败：API异常：" + JSONUtil.toJsonStr(e));
        }

    }

    /**
     * 根据时间区间查询FBA货件
     */
    private List<DmpInputTaskInitDTO> queryListByDateRange(AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketPlaceEnum, String rateLimitStr, String limitKey, DmpInputTaskResponse dmpResponse) {
        // 开始时间
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        // 结束时间
        LocalDateTime endTime = dmpInputTaskEntity.getEndTime();

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
                    .map(e -> fillDataAndToJsonObject(e, shopInfoDTO.getPlatformShopCode(), shopInfoDTO.getId(), shopInfoDTO.getName()))
                    .collect(Collectors.toList());
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【FBA货件列表拉取】 按时间区间 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("时间区间查询亚马逊FBA货件列表失败：API异常：" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 设置亚马逊货件ID和转换JSON
     */
    private JSONObject fillDataAndToJsonObject(InboundShipmentInfo item, String platformShopCode, String shopId, String shopName) {
        JSONObject json = (JSONObject) JSON.toJSON(item);
        json.put("platformShopCode", platformShopCode);
        json.put("shopId", shopId);
        json.put("shopName", shopName);
        return json;
    }

}
