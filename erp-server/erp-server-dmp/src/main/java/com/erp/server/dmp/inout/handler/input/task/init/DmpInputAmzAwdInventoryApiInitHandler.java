package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpFbaInventoryEntity;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.AwdApi;
import com.erp.sdk.oms.amz.spapi.api.FbaInventoryApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.awd.AwdOutStockInventorySummary;
import com.erp.sdk.oms.amz.spapi.model.awd.InventoryDetails;
import com.erp.sdk.oms.amz.spapi.model.awd.InventoryListing;
import com.erp.sdk.oms.amz.spapi.model.awd.InventorySummary;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpFbaInventoryService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wtr
 * @Date: 2025/12/24 18:24
 * @Param:
 * @Return:
 * @Description:
 **/
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzAwdInventoryApiInitHandler extends DmpInputAmzCommonInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();

        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_INVENTORY;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 校验速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【亚马逊FBA库存查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        AwdApi api = AmazonSpApiInitUtils.create(AwdApi.class, shopInfoDTO, false);

        try {
            InventoryListing inventoryListing = api.listInventory(null,null,null,null,200);

            if (Objects.isNull(inventoryListing)){
                return Collections.emptyList();
            }

            if (inventoryListing.getInventory().isEmpty()) {
                return Collections.emptyList();
            }

            List<com.erp.sdk.oms.amz.spapi.model.awd.InventorySummary> inventory = inventoryListing.getInventory();
            boolean isHasNextPage = true;
            while (isHasNextPage) {
                if (StringUtils.isNotBlank(inventoryListing.getNextToken())) {
                    InventoryListing nextPage = api.listInventory(null, null, null, inventoryListing.getNextToken(), 200);
                    if (Objects.nonNull(nextPage) && CollectionUtils.isNotEmpty(nextPage.getInventory())) {
                        inventory.addAll(nextPage.getInventory());
                        inventoryListing.setNextToken(nextPage.getNextToken());
                    } else {
                        isHasNextPage = false;
                    }
                } else {
                    isHasNextPage = false;
                }
            }

            // 转换数据
            List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
            for (InventorySummary summary : inventory) {
                AwdOutStockInventorySummary outSummary = new AwdOutStockInventorySummary();
                outSummary.setSku(summary.getSku());
                outSummary.setTotalInboundQuantity(summary.getTotalInboundQuantity());
                outSummary.setTotalOnhandQuantity(summary.getTotalOnhandQuantity());

                if (summary.getInventoryDetails() != null) {
                    InventoryDetails details = summary.getInventoryDetails();
                    outSummary.setAvailableDistributableQuantity(details.getAvailableDistributableQuantity());
                    outSummary.setReplenishmentQuantity(details.getReplenishmentQuantity());
                    outSummary.setReservedDistributableQuantity(details.getReservedDistributableQuantity());
                } else {
                    outSummary.setAvailableDistributableQuantity(0);
                    outSummary.setReplenishmentQuantity(0);
                    outSummary.setReservedDistributableQuantity(0);
                }

                DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
                dto.setMsg(JSON.toJSONString(outSummary));
                resultList.add(dto);
            }

            return resultList;
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊FBA库存查询】 platformShopCode={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            if (!StringUtils.isBlank(e.getMessage())){
                // 其他异常信息
                throw new ServiceException("亚马逊FBA库存查询失败:" + ExceptionUtil.stacktraceToString(e));
            }
            JSONObject jsonObject = JSON.parseObject(e.getResponseBody());
            JSONArray errorJsonArray = jsonObject.getJSONArray("errors");
            if (CollectionUtils.isEmpty(errorJsonArray)){
                // 其他异常信息
                throw new ServiceException("亚马逊AWD库存查询失败:"  + ExceptionUtil.stacktraceToString(e));
            }
            JSONObject errorObj = errorJsonArray.getJSONObject(0);
            String errorMsg = errorObj.getString("message");
            throw new ServiceException("[Amazon SP-APi] 亚马逊AWD库存查询失败:" + errorMsg);
        }catch (LWAException e) {
            //处理 LWA 认证异常
            log.error("【亚马逊AWD库存查询】LWA认证失败, platformShopCode={}", shopInfoDTO.getPlatformShopCode(), e);
            throw new ServiceException("亚马逊AWD库存查询失败: 认证异常", e);

        }

    }
}