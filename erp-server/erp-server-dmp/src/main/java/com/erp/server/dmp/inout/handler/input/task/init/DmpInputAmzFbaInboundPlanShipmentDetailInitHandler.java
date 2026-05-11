package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItem;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于主任务货件拉取 item 明细（给 dmp_fba_shipment_detail 子任务使用）
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanShipmentDetailInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> parentMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(parentMongoData)) {
            log.warn("FBA入库计划货件明细主任务taskId={},结果为空明细无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }

        String shopId = resolveAuthShopIdByTaskChain();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("未找到店铺授权ID:taskId=" + dmpInputTaskEntity.getId());
        }
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_DETAIL;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (redisUtil.get(limitKey) != null) {
            log.warn("【FBA入库计划货件明细拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        List<JSONObject> allItemList = new ArrayList<>();

        for (Map<String, Object> parentMongo : parentMongoData) {
            String shipmentId = getShipmentId(parentMongo);
            if (StringUtils.isBlank(shipmentId)) {
                log.warn("跳过明细拉取，原因=shipmentId为空, data={}", JSON.toJSONString(parentMongo));
                continue;
            }
            String marketplaceId = getMarketplaceId(parentMongo, shopInfoDTO);
            if (StringUtils.isBlank(marketplaceId)) {
                throw new ServiceException("未找到marketplaceId, shipmentId=" + shipmentId);
            }
            String cacheKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, requestType.getBusinessTypeName(), shipmentId);
            Object cacheData = redisUtil.get(cacheKey);
            if (cacheData != null) {
                List<JSONObject> cacheItemList = JSONArray.parseArray(cacheData.toString(), JSONObject.class);
                if (CollUtil.isNotEmpty(cacheItemList)) {
                    allItemList.addAll(cacheItemList);
                }
                continue;
            }

            try {
                GetShipmentItemsResponse shipmentItemsResponse = api.getShipmentItemsByShipmentId(shipmentId, marketplaceId);
                List<InboundShipmentItem> itemDataList = shipmentItemsResponse != null
                        && shipmentItemsResponse.getPayload() != null
                        && shipmentItemsResponse.getPayload().getItemData() != null
                        ? shipmentItemsResponse.getPayload().getItemData()
                        : Collections.emptyList();
                List<JSONObject> curItemJsonList = new ArrayList<>();
                for (InboundShipmentItem itemData : itemDataList) {
                    curItemJsonList.add((JSONObject) JSON.toJSON(itemData));
                }
                redisUtil.set(cacheKey, JSON.toJSONString(curItemJsonList), 300);
                allItemList.addAll(curItemJsonList);
            } catch (ApiException e) {
                if (e.getCode() == 429) {
                    BigDecimal timeout = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(requestType.getRateLimit()), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, requestType.getRateLimit(), timeout.longValue());
                    log.warn("【FBA入库计划货件明细拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                    disableNextStatus(dmpResponse);
                    return Collections.emptyList();
                }
                log.warn("跳过明细拉取，shipmentId={}, marketplaceId={}, error={}", shipmentId, marketplaceId, e.getMessage());
            } catch (Exception e) {
                log.warn("跳过明细拉取，shipmentId={}, marketplaceId={}, error={}", shipmentId, marketplaceId, e.getMessage());
            }
        }

        if (CollUtil.isEmpty(allItemList)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allItemList)));
    }

    private String getShipmentId(Map<String, Object> parentMongo) {
        String shipmentId = parentMongo.getOrDefault("shipmentConfirmationId",
                parentMongo.getOrDefault("shipmentId", "")).toString();
        return StringUtils.trimToEmpty(shipmentId);
    }

    private String getMarketplaceId(Map<String, Object> parentMongo, AmazonShopInfoDTO shopInfoDTO) {
        String marketplaceId = parentMongo.getOrDefault("marketplaceId", "").toString();
        if (StringUtils.isNotBlank(marketplaceId)) {
            return marketplaceId;
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        if (marketplaceEnum == null) {
            return "";
        }
        return marketplaceEnum.getMarketplaceId();
    }

    private String resolveAuthShopIdByTaskChain() {
        DmpInputTaskEntity currentTask = dmpInputTaskEntity;
        int guard = 0;
        while (currentTask != null && StringUtils.isNotBlank(currentTask.getParentTaskId()) && guard++ < 20) {
            DmpInputTaskEntity parentTask = dmpInputTaskService.getById(currentTask.getParentTaskId());
            if (parentTask == null) {
                break;
            }
            currentTask = parentTask;
        }
        if (currentTask == null) {
            return "";
        }
        return StringUtils.defaultString(currentTask.getNextLevelId());
    }

    private String buildRateLimitKey(AmazonShopInfoDTO shopInfoDTO, AmazonRequestTypeRateLimiterEnum requestType) {
        return StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(),
                shopInfoDTO.getPlatformShopCode(), requestType.getBusinessTypeName());
    }

    private void disableNextStatus(DmpInputTaskResponse dmpResponse) {
        if (dmpResponse instanceof DmpInputInitResponse) {
            ((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
        }
    }
}
