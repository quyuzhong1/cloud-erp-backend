package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.Box;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundPlanSummary;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListInboundPlansResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ListShipmentBoxesResponse;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * FBA入库计划装箱信息
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmazonFbaInboundBoxesInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 系统时间
        LocalDateTime systemBeginTime = dmpInputTaskEntity.getStartTime();

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData("platformShopCode", "platformShopCode", PannoEnum.EQ, shopInfoDTO.getPlatformShopCode()));
        paramDataList.add(new ParamData("lastUpdatedAtSystemDate", "lastUpdatedAtSystemDate", PannoEnum.GTE, systemBeginTime));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, "amazon_fbaInboundPlan_data");
        if (CollUtil.isEmpty(findMongoData)) {
            log.warn("未找到更新时间对应入库计划记录:taskDetailId={}", dmpInputTaskEntity.getId());
            return Collections.emptyList();
        }

        List<JSONObject> dataList = new ArrayList<>();

        for (Map<String, Object> findMongo : findMongoData) {
            // 入库计划ID
            String inboundPlanId = checkAndGetMongoValue(findMongo, "inboundPlanId");
            // 货件ID
            String shipmentId = checkAndGetMongoValue(findMongo, "shipmentId");
            // 请求亚马逊接口
            List<Box> sourceResultList = requestAmazonFbaInboundBoxes(inboundPlanId, shipmentId, shopInfoDTO, dmpInputTaskEntity.getExtendJson());
            // 补充信息
            List<JSONObject> resultList = sourceResultList.stream().map(e -> fillDataToJsonObject(e, inboundPlanId, shipmentId)).collect(Collectors.toList());
            dataList.addAll(resultList);
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(dataList)));
    }

    /**
     * 填充信息
     */
    private JSONObject fillDataToJsonObject(Box sourceEntity, String inboundPlanId, String shipmentId) {
        JSONObject json = (JSONObject) JSON.toJSON(sourceEntity);
        json.put("inboundPlanId", inboundPlanId);
        json.put("shipmentId", shipmentId);
        return json;
    }

    /**
     * 请求亚马逊入库计划装箱信息
     *
     * @param inboundPlanId 入库计划ID
     * @param shopInfoDTO   店铺信息
     * @param extendJson    请求参数
     * @return 装箱信息列表
     */
    public List<Box> requestAmazonFbaInboundBoxes(String inboundPlanId, String shipmentId, AmazonShopInfoDTO shopInfoDTO, String extendJson) {
        // 默认请求速率
        AmazonRequestTypeRateLimiterEnum requestTypeEnum = AmazonRequestTypeRateLimiterEnum.FBA_INBOUND_PLAN_BOXES;
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_GROUP_ID_PREFIX, shopInfoDTO.getPlatformShopCode(), requestTypeEnum);
        // 获取动态速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            String msg = StrUtil.format("【亚马逊入库计划装箱信息分页】 platformShopCode={}, 存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            throw new ServiceException(msg);
        }

        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);

        // 当前接口速率
        String rateLimitStr = requestTypeEnum.getRateLimit();
        try {
            Integer pageSize = 1000;
            String paginationToken = null;
            ApiResponse<ListShipmentBoxesResponse> apiResponse = api.listShipmentBoxesWithHttpInfo(inboundPlanId, shipmentId, pageSize, paginationToken);
            ListShipmentBoxesResponse data = apiResponse.getData();
            // 最后数据列表
            LinkedList<Box> sourceResultList = new LinkedList<>(data.getBoxes());

            paginationToken = data.getPagination().getNextToken();
            // 解析当前请求响应接口当前速率
            rateLimitStr = AmazonSpApiRateLimitUtils.parseRateLimit(apiResponse, rateLimitStr);

            while (StringUtils.isNotBlank(paginationToken)) {
                ApiResponse<ListShipmentBoxesResponse> currentResp = api.listShipmentBoxesWithHttpInfo(inboundPlanId, shipmentId, pageSize, paginationToken);
                ListShipmentBoxesResponse curData = currentResp.getData();
                paginationToken = curData.getPagination().getNextToken();
                sourceResultList.addAll(curData.getBoxes());
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
     * 校验和获取指定字段
     */
    private String checkAndGetMongoValue(Map<String, Object> nongoObjectMap, String mongoFieldName) {
        Object reportDocumentIdObj = nongoObjectMap.get(mongoFieldName);
        if (null == reportDocumentIdObj) {
            String msg = StrUtil.format("未找到{}:taskId={}", mongoFieldName, dmpInputTaskEntity.getId());
            ServiceException.runError(msg);
        }
        return (String) reportDocumentIdObj;

    }
}
