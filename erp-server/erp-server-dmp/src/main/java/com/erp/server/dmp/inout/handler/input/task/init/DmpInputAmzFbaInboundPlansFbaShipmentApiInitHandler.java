package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonInboundPlanSortByEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonInboundPlanSortOrderEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonInboundPlanStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlanSummary;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListInboundPlansResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FBA InboundPlan 列表拉取 - 拉取FBA货件前置处理器。
 * <p>
 * 代码审查说明（审查问题2，intentional）：本 Handler 为 Inbound Plan 链路统一入口，定时同步与
 * {@code pullInboundPlanShipment} 手动 hotfix 均先 {@code listInboundPlans}，再按
 * {@link #parseLookbackMinutes()} 时间窗过滤。手动 {@code shipmentCodeList} 仅在后续 getShipment 阶段生效，
 * 无法像旧版 {@code getShipments(shipmentIdList)} 绕过计划列表直查；窗口外计划下的货件需扩大 lookbackMinutes
 * 或后续迭代专用直拉 Init。此为架构已知限制，勿当缺陷修复。
 * 手动 hotfix（{@code shipmentCodeList}）对 429、空计划列表等与后续 Init 节点一致执行 fail-fast。
 * lookbackMinutes 时间窗过滤依赖 {@code sortOrder=DESC} 才能提前终止分页；{@link #parseSortOrder(String)} 会将 ASC 回退为 DESC。
 */
@Slf4j
@Service("dmpInputAmzFbaInboundPlansFbaShipmentApiInitHandler")
@Scope("prototype")
public class DmpInputAmzFbaInboundPlansFbaShipmentApiInitHandler extends DmpInputAmzCommonInitHandler {

    private static final int LIST_INBOUND_PLANS_PAGE_SIZE = 30;

    /** 连续无时间戳计划达到该阈值时停止分页，避免 listInboundPlans 过量调用 */
    private static final int MAX_CONSECUTIVE_NULL_PLAN_TIME = LIST_INBOUND_PLANS_PAGE_SIZE * 3;

    /** 连续整页均无有效时间戳时停止分页 */
    private static final int MAX_PAGES_WITHOUT_VALID_PLAN_TIME = 3;

    /**
     * dmp_cfg_input_convert.convert_class 配置值，与类名保持一致。
     */
    public static final String CONVERT_CLASS = DmpInputAmzFbaInboundPlansFbaShipmentApiInitHandler.class.getSimpleName();

    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("未找到店铺授权ID:taskId=" + dmpInputTaskEntity.getId());
        }
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }

        boolean manualPull = hasManualShipmentCodeFilter();
        AmazonRequestTypeRateLimiterEnum requestType = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN;
        String limitKey = buildRateLimitKey(shopInfoDTO, requestType);
        if (isRateLimited(limitKey)) {
            if (manualPull) {
                throw new ServiceException("手动拉取FBA入库计划列表失败: Amazon API 429 限流等待恢复, taskId=" + dmpInputTaskEntity.getId());
            }
            log.warn("【FBA入库计划货件拉取】platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            disableNextStatus(dmpResponse);
            return Collections.emptyList();
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
        String extendJson = dmpInputTaskEntity.getExtendJson();
        List<String> inboundPlanStatusList = parseStatusList(extendJson);
        String sortBy = parseSortBy(extendJson);
        String sortOrder = parseSortOrder(extendJson);
        // 审查问题2（intentional）：手动/定时共用 lookbackMinutes 时间窗，shipmentCodeList 无法跳过此过滤
        int lookbackMinutes = parseLookbackMinutes();
        OffsetDateTime thresholdTime = OffsetDateTime.now().minusMinutes(lookbackMinutes);

        Map<String, Integer> statusCountMap = new LinkedHashMap<>();
        Map<String, InboundPlanSummary> inboundPlanSummaryMap = new LinkedHashMap<>();
        for (String status : inboundPlanStatusList) {
            String nextToken = null;
            int currentStatusCount = 0;
            boolean reachedOlderData = false;
            boolean stopPagination = false;
            int consecutiveNullPlanTime = 0;
            int pagesWithoutValidPlanTime = 0;
            do {
                try {
                    ListInboundPlansResponse response = api.listInboundPlans(
                            LIST_INBOUND_PLANS_PAGE_SIZE, nextToken, status, sortBy, sortOrder);
                    List<InboundPlanSummary> inboundPlans = response != null ? response.getInboundPlans() : null;
                    boolean pageHasValidPlanTime = false;
                    if (CollUtil.isNotEmpty(inboundPlans)) {
                        for (InboundPlanSummary inboundPlan : inboundPlans) {
                            OffsetDateTime planTime = resolvePlanTime(inboundPlan);
                            if (planTime == null) {
                                consecutiveNullPlanTime++;
                                log.warn("【FBA入库计划拉取】跳过计划: lastUpdatedAt/createdAt 均为空, inboundPlanId={}",
                                        inboundPlan.getInboundPlanId());
                                if (consecutiveNullPlanTime >= MAX_CONSECUTIVE_NULL_PLAN_TIME) {
                                    log.warn("【FBA入库计划拉取】连续{}条计划无时间戳，停止分页, status={}, taskId={}",
                                            consecutiveNullPlanTime, status, dmpInputTaskEntity.getId());
                                    stopPagination = true;
                                    break;
                                }
                                continue;
                            }
                            consecutiveNullPlanTime = 0;
                            pageHasValidPlanTime = true;
                            if (planTime.isBefore(thresholdTime)) {
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
                    if (reachedOlderData || stopPagination) {
                        break;
                    }
                    if (CollUtil.isNotEmpty(inboundPlans) && !pageHasValidPlanTime) {
                        pagesWithoutValidPlanTime++;
                        if (pagesWithoutValidPlanTime >= MAX_PAGES_WITHOUT_VALID_PLAN_TIME) {
                            log.warn("【FBA入库计划拉取】连续{}页计划均无有效时间戳，停止分页, status={}, taskId={}",
                                    pagesWithoutValidPlanTime, status, dmpInputTaskEntity.getId());
                            break;
                        }
                    } else if (pageHasValidPlanTime) {
                        pagesWithoutValidPlanTime = 0;
                    }
                    nextToken = response != null && response.getPagination() != null ? response.getPagination().getNextToken() : null;
                } catch (ApiException e) {
                    if (e.getCode() == 429 && manualPull) {
                        applyRateLimitBackoff(requestType, limitKey);
                        disableNextStatus(dmpResponse);
                        throw new ServiceException("手动拉取FBA入库计划列表失败: Amazon API 429 限流, taskId=" + dmpInputTaskEntity.getId());
                    }
                    if (handleRateLimitAndCheckNeedStop(e, requestType, limitKey, shopInfoDTO, dmpResponse, "FBA入库计划列表拉取")) {
                        if (manualPull) {
                            throw new ServiceException("手动拉取FBA入库计划列表失败: Amazon API 429 限流, taskId=" + dmpInputTaskEntity.getId());
                        }
                        return Collections.emptyList();
                    }
                    log.error("【FBA入库计划列表拉取】platformShopCode={}, taskId={}, status={}, Amazon API 异常",
                            shopInfoDTO.getPlatformShopCode(), dmpInputTaskEntity.getId(), status, e);
                    throw new ServiceException("拉取FBA入库计划列表失败，请稍后重试");
                } catch (LWAException e) {
                    log.error("【FBA入库计划列表拉取】platformShopCode={}, taskId={}, status={}, LWA 授权异常",
                            shopInfoDTO.getPlatformShopCode(), dmpInputTaskEntity.getId(), status, e);
                    throw new ServiceException("拉取FBA入库计划列表失败，请稍后重试");
                }
            } while (StrUtil.isNotBlank(nextToken));
            statusCountMap.put(status, currentStatusCount);
        }

        List<InboundPlanSummary> inboundPlanSummaryList = new ArrayList<>(inboundPlanSummaryMap.values());
        if (CollUtil.isEmpty(inboundPlanSummaryList)) {
            log.info("【FBA入库计划货件拉取】platformShopCode={},状态统计={},无符合时间范围的计划", shopInfoDTO.getPlatformShopCode(), JSON.toJSONString(statusCountMap));
            if (manualPull) {
                throw new ServiceException("手动拉取FBA入库计划列表失败: 无符合时间范围的计划, lookbackMinutes="
                        + lookbackMinutes + ", threshold=" + thresholdTime + ", taskId=" + dmpInputTaskEntity.getId());
            }
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
            if (manualPull) {
                throw new ServiceException("手动拉取FBA入库计划列表失败: 未解析到有效计划ID, taskId=" + dmpInputTaskEntity.getId());
            }
            return Collections.emptyList();
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(inboundPlanDataList)));
    }

    private List<String> parseStatusList(String extendJson) {
        if (StringUtils.isBlank(extendJson)) {
            return AmazonInboundPlanStatusEnum.defaultSyncStatusCodes();
        }
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (extendObj == null) {
            return AmazonInboundPlanStatusEnum.defaultSyncStatusCodes();
        }
        JSONArray statusArray = extendObj.getJSONArray("statusList");
        if (CollUtil.isNotEmpty(statusArray)) {
            List<String> statusList = statusArray.toJavaList(String.class);
            List<String> validatedList = statusList.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(status -> AmazonInboundPlanStatusEnum.fromCode(status.trim()))
                    .filter(Objects::nonNull)
                    .map(AmazonInboundPlanStatusEnum::getCode)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(validatedList)) {
                return validatedList;
            }
        }
        String status = extendObj.getString("status");
        AmazonInboundPlanStatusEnum statusEnum = AmazonInboundPlanStatusEnum.fromCode(status);
        if (statusEnum != null) {
            return Collections.singletonList(statusEnum.getCode());
        }
        return AmazonInboundPlanStatusEnum.defaultSyncStatusCodes();
    }

    private String parseSortBy(String extendJson) {
        AmazonInboundPlanSortByEnum defaultSortBy = AmazonInboundPlanSortByEnum.LAST_UPDATED_TIME;
        if (StringUtils.isBlank(extendJson)) {
            return defaultSortBy.getCode();
        }
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (extendObj == null) {
            return defaultSortBy.getCode();
        }
        AmazonInboundPlanSortByEnum sortByEnum = AmazonInboundPlanSortByEnum.fromCode(extendObj.getString("sortBy"));
        return sortByEnum == null ? defaultSortBy.getCode() : sortByEnum.getCode();
    }

    /**
     * lookbackMinutes 时间窗仅在与 DESC 组合时可提前 break；ASC 会从最旧记录起逐页扫描，易触发 429。
     */
    private String parseSortOrder(String extendJson) {
        AmazonInboundPlanSortOrderEnum defaultSortOrder = AmazonInboundPlanSortOrderEnum.DESC;
        if (StringUtils.isBlank(extendJson)) {
            return defaultSortOrder.getCode();
        }
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (extendObj == null) {
            return defaultSortOrder.getCode();
        }
        AmazonInboundPlanSortOrderEnum sortOrderEnum = AmazonInboundPlanSortOrderEnum.fromCode(extendObj.getString("sortOrder"));
        if (sortOrderEnum == null) {
            return defaultSortOrder.getCode();
        }
        if (sortOrderEnum == AmazonInboundPlanSortOrderEnum.ASC) {
            log.warn("【FBA入库计划拉取】sortOrder=ASC 无法配合 lookbackMinutes 提前终止分页，已回退为 DESC, taskId={}",
                    dmpInputTaskEntity.getId());
            return defaultSortOrder.getCode();
        }
        return sortOrderEnum.getCode();
    }

    private OffsetDateTime resolvePlanTime(InboundPlanSummary inboundPlan) {
        if (inboundPlan == null) {
            return null;
        }
        OffsetDateTime planTime = inboundPlan.getLastUpdatedAt();
        if (planTime == null) {
            planTime = inboundPlan.getCreatedAt();
        }
        return planTime;
    }

    /**
     * lookbackMinutes 优先，兼容 lookbackDays（按天换算分钟）
     */
    private int parseLookbackMinutes() {
        int defaultLookbackMinutes = 7 * 24 * 60;
        String lookbackMinutesStr = String.valueOf(defaultLookbackMinutes);
        String extendJson = dmpCfgInputEntity.getExtendJson();
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if (parseObject != null) {
                String sourceLookbackMinutes = parseObject.getString("lookbackMinutes");
                if (StringUtils.isNotBlank(sourceLookbackMinutes)) {
                    lookbackMinutesStr = sourceLookbackMinutes;
                } else {
                    String sourceLookbackDays = parseObject.getString("lookbackDays");
                    if (StringUtils.isNotBlank(sourceLookbackDays)) {
                        try {
                            int lookbackDays = Integer.parseInt(sourceLookbackDays);
                            if (lookbackDays > 0) {
                                return lookbackDays * 24 * 60;
                            }
                        } catch (Exception ignore) {
                            return defaultLookbackMinutes;
                        }
                    }
                }
            }
        }
        try {
            int lookbackMinutes = Integer.parseInt(lookbackMinutesStr);
            return lookbackMinutes > 0 ? lookbackMinutes : defaultLookbackMinutes;
        } catch (Exception ignore) {
            return defaultLookbackMinutes;
        }
    }
}
