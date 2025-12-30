package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportSpecification;
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
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportCreatedApiInitHandler extends DmpInputAmzReportCommonApiInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;


    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String extendJson = dmpCfgInputEntity.getExtendJson();
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (null == extendObj) {
            ServiceException.runError("extendJson参数为空");
        }
        String reportType = extendObj.getString("reportType");

        // 获取店铺信息
        String shopId = dmpInputTaskEntity.getNextLevelId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            ServiceException.runError("未找到店铺授权:" + shopId);
        }

        // 允许指定报告ID
        String detailExtendJson = dmpInputTaskEntity.getExtendJson();
        if (StringUtils.isNotBlank(detailExtendJson)) {
            JSONObject detailExtendObj = JSONObject.parseObject(detailExtendJson);
            String reportId = detailExtendObj.getString("reportId");
            if (StringUtils.isNotBlank(reportId)){
                return convertDmpInputTaskInitDTOS(reportId, reportType, shopInfoDTO);
            }
        }

        List<String> marketplaceIds = checkReportIsMergeMarketplace(reportType, shopInfoDTO);

        String marketplaceId = marketplaceIds.stream().findFirst().orElse(null);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceId);
        if (null == marketplaceEnum) {
            String msg = StrUtil.format("未找到Marketplace枚举类型,cfgInputId={}, marketplaceId={}", dmpCfgInputEntity.getId(), marketplaceIds);
            ServiceException.runError(msg);
        }


        // 从缓存获取
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_CREATE;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj){
            String msg = StrUtil.format("【亚马逊创建报告】 platformShopCode={}, 报告类型={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode(), reportType);
            log.warn(msg);
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        ReportsApi reportsApi = AmazonSpApiInitUtils.create(ReportsApi.class, shopInfoDTO, false);

        // 获取同组报告类型配置
        // 请求创建中报告预计时间
        Long estimatedWaitSecond = requestAndCheckWaitTime(reportType, reportsApi, shopInfoDTO);
        if (estimatedWaitSecond > 0) {
            AmazonCreateReportResultDTO waitDto = AmazonCreateReportResultDTO.wait(estimatedWaitSecond);
            log.warn("【亚马逊创建报告】 监测到有处理中的报告: platformShopCode={}, 报告类型={}, 亚马逊正在处理的报告ID={}, 等待时间={}",
                    shopInfoDTO.getPlatformShopCode(),
                    reportType,
                    waitDto.getReportId(),
                    waitDto.getEstimatedWaitSecond());
            // 下次时间
            DmpInputInitResponse dmpInputInitResponse = (DmpInputInitResponse) dmpResponse;
            dmpInputInitResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        // 组合请求参数
        CreateReportSpecification body = createReportSpecificationParam(reportType, marketplaceIds);
        ApiResponse<CreateReportResponse> reportWithHttpInfo = null;
        try {
            // 请求亚马逊创建报告接口
            reportWithHttpInfo = reportsApi.createReportWithHttpInfo(body);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊创建报告】 platformShopCode={},reportType={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode(), reportType);
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 创建报告失败:body=" + e.getMessage());
        }
        CreateReportResponse reportResponse = reportWithHttpInfo.getData();
        String reportId = reportResponse.getReportId();
        if (null == reportId) {
            ServiceException.runError("请求亚马逊创建报告失败：body=" + JSONUtil.toJsonStr(reportResponse));
        }
        // 设置到缓存(已完成或结束删除)
//      redisUtil.set(key, reportId);
        // 组合响应
        return convertDmpInputTaskInitDTOS(reportId, reportType, shopInfoDTO);
    }



}
