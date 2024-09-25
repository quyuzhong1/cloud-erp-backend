package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlanSummary;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListInboundPlansResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiRateLimitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * 亚马逊FBA入库计划列表
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmazonFbaInboundPlanApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        String extendJson = dmpInputTaskEntity.getExtendJson();

        // 请求数据
        List<InboundPlanSummary> sourceResultList = requestAmazonFbaInboundPlan(shopInfoDTO, extendJson);

        // 拼接来源信息
        List<JSONObject> resultList = sourceResultList.stream().map(e -> fillDataToJsonObject(e, shopInfoDTO)).collect(Collectors.toList());

        // 组合响应
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(resultList)));

    }

    /**
     * 请求亚马逊FBA入库计划接口
     * @param shopInfoDTO 店铺信息
     * @param extendJson  dmp_input_task的扩展参数
     * @return 所有响应数据
     */
    public List<InboundPlanSummary> requestAmazonFbaInboundPlan(AmazonShopInfoDTO shopInfoDTO, String extendJson) {
        // 默认请求速率
        AmazonRequestTypeRateLimiterEnum requestTypeEnum = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN;
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_GROUP_ID_PREFIX, shopInfoDTO.getPlatformShopCode(), requestTypeEnum);
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            String msg = StrUtil.format("【亚马逊入库计划分页】 platformShopCode={}, 存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            throw new ServiceException(msg);
        }

        // 查询参数
        // 状态 ACTIVE/VOIDED/SHIPPED
        String status = null;
        // 排序字段 LAST_UPDATED_TIME/CREATION_TIME
        String sortBy = null;
        // 正顺 ASC/DESC
        String sortOrder = null;
        if (StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if (null != parseObject) {
                status = parseObject.getString("status");
                sortBy = parseObject.getString("sortBy");
                sortOrder = parseObject.getString("sortOrder");
            }
        }
        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);

        // 当前接口速率
        String rateLimitStr = requestTypeEnum.getRateLimit();
        try {
            Integer pageSize = 30;
            String paginationToken = null;
            ApiResponse<ListInboundPlansResponse> apiResponse = api.listInboundPlansWithHttpInfo(pageSize, paginationToken, status, sortBy, sortOrder);
            ListInboundPlansResponse data = apiResponse.getData();
            // 最后数据列表
            List<InboundPlanSummary> sourceResultList = new LinkedList<>(data.getInboundPlans());

            paginationToken = data.getPagination().getNextToken();
            // 解析当前请求响应接口当前速率
            rateLimitStr = AmazonSpApiRateLimitUtils.parseRateLimit(apiResponse, rateLimitStr);

            while (StringUtils.isNotBlank(paginationToken)) {
                ApiResponse<ListInboundPlansResponse> currentResp = api.listInboundPlansWithHttpInfo(pageSize, paginationToken, null, null, null);
                ListInboundPlansResponse curData = currentResp.getData();
                paginationToken = curData.getPagination().getNextToken();
                sourceResultList.addAll(curData.getInboundPlans());
                // 解析当前请求响应接口当前速率
                rateLimitStr = AmazonSpApiRateLimitUtils.parseRateLimit(currentResp, rateLimitStr);
            }
            return sourceResultList;
        } catch (Exception e) {
            if (e instanceof ApiException) {
                ApiException apiError = (ApiException) e;
                if (429 == apiError.getCode()) {
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 填充其他信息
     */
    private JSONObject fillDataToJsonObject(InboundPlanSummary sourceEntity, AmazonShopInfoDTO shopInfoDTO) {
        JSONObject json = (JSONObject) JSON.toJSON(sourceEntity);
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        // 创建时间转换系统时区
        LocalDateTime systemZoneCreateTime = sourceEntity.getCreatedAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        json.put("createdAtSystemDate", systemZoneCreateTime);
        // 更新时间转换系统时区
        LocalDateTime systemZoneUpdateTime = sourceEntity.getLastUpdatedAt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        json.put("lastUpdatedAtSystemDate", systemZoneUpdateTime);
        return json;
    }


}
