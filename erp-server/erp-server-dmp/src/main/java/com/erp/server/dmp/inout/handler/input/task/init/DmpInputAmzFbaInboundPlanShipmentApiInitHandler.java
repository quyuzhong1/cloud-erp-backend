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
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlanSummary;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListInboundPlansResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FBA InboundPlan 列表拉取（仅调用 listInboundPlans，一个 API 一个任务）
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanShipmentApiInitHandler extends DmpInputInitHandler {

    private static final List<String> DEFAULT_STATUS_LIST = Arrays.asList("ACTIVE", "SHIPPED");

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (redisUtil.get(limitKey) != null) {
            log.warn("【FBA入库计划货件拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        List<String> inboundPlanStatusList = parseStatusList(dmpInputTaskEntity.getExtendJson());
        int lookbackDays = parseLookbackDays();
        OffsetDateTime thresholdTime = OffsetDateTime.now().minusDays(lookbackDays);

        Map<String, Integer> statusCountMap = new LinkedHashMap<>();
        Map<String, InboundPlanSummary> inboundPlanSummaryMap = new LinkedHashMap<>();
        for (String status : inboundPlanStatusList) {
            String nextToken = null;
            int currentStatusCount = 0;
            boolean reachedOlderData = false;
            do {
                try {
                    ListInboundPlansResponse response = api.listInboundPlans(30, nextToken, status, "LAST_UPDATED_TIME", "DESC");
                    List<InboundPlanSummary> inboundPlans = response != null ? response.getInboundPlans() : null;
                    if (CollUtil.isNotEmpty(inboundPlans)) {
                        for (InboundPlanSummary inboundPlan : inboundPlans) {
                            OffsetDateTime lastUpdatedAt = inboundPlan.getLastUpdatedAt();
                            if (lastUpdatedAt == null || lastUpdatedAt.isBefore(thresholdTime)) {
                                reachedOlderData = true;
                                break;
                            }
                            String inboundPlanId = inboundPlan.getInboundPlanId();
                            if (StrUtil.isBlank(inboundPlanId)) {
                                continue;
                            }
                            currentStatusCount++;
                            inboundPlanSummaryMap.putIfAbsent(inboundPlanId, inboundPlan);
                        }
                    }
                    if (reachedOlderData) {
                        break;
                    }
                    nextToken = response != null && response.getPagination() != null ? response.getPagination().getNextToken() : null;
                } catch (ApiException e) {
                    if (handleRateLimitAndCheckNeedStop(e, requestType, limitKey, shopInfoDTO, dmpResponse, "FBA入库计划列表拉取")) {
                        return Collections.emptyList();
                    }
                    throw new ServiceException("拉取FBA入库计划列表失败:" + e.getMessage());
                } catch (LWAException e) {
                    throw new ServiceException("拉取FBA入库计划列表失败:" + e.getMessage());
                }
            } while (StrUtil.isNotBlank(nextToken));
            statusCountMap.put(status, currentStatusCount);
        }

        List<InboundPlanSummary> inboundPlanSummaryList = new ArrayList<>(inboundPlanSummaryMap.values());
        if (CollUtil.isEmpty(inboundPlanSummaryList)) {
            log.info("【FBA入库计划货件拉取】platformShopCode={},状态统计={},无符合时间范围的计划", shopInfoDTO.getPlatformShopCode(), JSON.toJSONString(statusCountMap));
            return Collections.emptyList();
        }

        List<JSONObject> inboundPlanDataList = new ArrayList<>();
        for (InboundPlanSummary inboundPlanSummary : inboundPlanSummaryList) {
            String inboundPlanId = inboundPlanSummary.getInboundPlanId();
            if (StringUtils.isBlank(inboundPlanId)) {
                continue;
            }
            inboundPlanDataList.add((JSONObject) JSON.toJSON(inboundPlanSummary));
        }

        log.info("【FBA入库计划拉取】platformShopCode={},状态统计={},计划数={},阈值={}",
                shopInfoDTO.getPlatformShopCode(),
                JSON.toJSONString(statusCountMap),
                inboundPlanSummaryList.size(),
                thresholdTime);

        if (CollUtil.isEmpty(inboundPlanDataList)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(inboundPlanDataList)));
    }

    private List<String> parseStatusList(String extendJson) {
        if (StringUtils.isBlank(extendJson)) {
            return DEFAULT_STATUS_LIST;
        }
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (extendObj == null) {
            return DEFAULT_STATUS_LIST;
        }
        JSONArray statusArray = extendObj.getJSONArray("statusList");
        if (CollUtil.isNotEmpty(statusArray)) {
            List<String> statusList = statusArray.toJavaList(String.class);
            if (CollUtil.isNotEmpty(statusList)) {
                return statusList;
            }
        }
        String status = extendObj.getString("status");
        if (StringUtils.isNotBlank(status)) {
            return Collections.singletonList(status);
        }
        return DEFAULT_STATUS_LIST;
    }

    /**
     * lookbackDays 配置读取方式对齐 DmpInputLxFbaShipmentApiInitHandler(limitSecond)
     */
    private int parseLookbackDays() {
        String lookbackDaysStr = "7";
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if (parseObject != null) {
                String sourceLookbackDays = parseObject.getString("lookbackDays");
                if (StringUtils.isNotBlank(sourceLookbackDays)) {
                    lookbackDaysStr = sourceLookbackDays;
                }
            }
        }
        try {
            int lookbackDays = Integer.parseInt(lookbackDaysStr);
            return lookbackDays > 0 ? lookbackDays : 7;
        } catch (Exception ignore) {
            return 7;
        }
    }

    private String buildRateLimitKey(AmazonShopInfoDTO shopInfoDTO, AmazonRequestTypeRateLimiterEnum requestType) {
        return StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(),
                shopInfoDTO.getPlatformShopCode(), requestType.getBusinessTypeName());
    }

    private boolean handleRateLimitAndCheckNeedStop(ApiException e,
                                                    AmazonRequestTypeRateLimiterEnum requestType,
                                                    String limitKey,
                                                    AmazonShopInfoDTO shopInfoDTO,
                                                    DmpInputTaskResponse dmpResponse,
                                                    String businessDesc) {
        if (e.getCode() != 429) {
            return false;
        }
        BigDecimal timeout = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(requestType.getRateLimit()), 8, RoundingMode.DOWN));
        redisUtil.set(limitKey, requestType.getRateLimit(), timeout.longValue());
        log.warn("【{}】platformShopCode={},存在429等待恢复:放弃当前请求任务", businessDesc, shopInfoDTO.getPlatformShopCode());
        disableNextStatus(dmpResponse);
        return true;
    }

    private void disableNextStatus(DmpInputTaskResponse dmpResponse) {
        if (dmpResponse instanceof DmpInputInitResponse) {
            ((DmpInputInitResponse) dmpResponse).setDoNextStatus(false);
        }
    }
}
