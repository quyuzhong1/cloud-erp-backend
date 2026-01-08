package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.dto.AmazonLimitInfoDTO;
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
public class DmpInputAmzReportCreatedOrQueryApiInitHandler extends DmpInputAmzReportCommonApiInitHandler {
    
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
        if (StringUtils.isBlank(reportType)){
            ServiceException.runError("报告类型为空");
        }
        // 配置信息
        boolean checkNewDateEndTime = extendObj.getBooleanValue("checkNewDateEndTime");
        String marketplaceIdsType = extendObj.getOrDefault("marketplaceIdsType", "").toString();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(dmpInputTaskEntity.getNextLevelId());

        // 请求亚马逊接口
        ReportsApi reportsApi = AmazonSpApiInitUtils.create(ReportsApi.class, shopInfoDTO, false);
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 是否创建报告
        boolean hasCreateReport = false;
        String taskExtendJson = dmpInputTaskEntity.getExtendJson();
        if (StringUtils.isNotBlank(taskExtendJson)) {
            JSONObject taskExtendObj = JSONObject.parseObject(taskExtendJson);
            Boolean createReport = taskExtendObj.getBoolean("hasCreateReport");
            if (null != createReport) {
                hasCreateReport = createReport;
            }
        }

        // 校验速率
        AmazonLimitInfoDTO amazonLimitInfoDTO = checkAndLimitInfo(AmazonRequestTypeRateLimiterEnum.REPORTS_QUERY, shopInfoDTO.getPlatformShopCode());
        if (amazonLimitInfoDTO.isLimitFlag()) {
            log.warn("【亚马逊创建报告后查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        if (!hasCreateReport) {
            // 不执行创建报告
            // 查询最新报告
            return queryNewReport((DmpInputInitResponse) dmpResponse, reportType, marketplaceEnum, reportsApi, amazonLimitInfoDTO.getRateLimitStr(), amazonLimitInfoDTO.getLimitKey(), shopInfoDTO, checkNewDateEndTime, marketplaceIdsType);
        } else {
            JSONObject reportIdObj = null;
            String redisKey = CharSequenceUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, reportType, dmpInputTaskEntity.getId());
            Object reportInfoObj = redisUtil.get(redisKey);
            if (null == reportInfoObj) {
                // 执行创建报告
                List<DmpInputTaskInitDTO> inputTaskInitDTOS = creatReport(dmpRequest, dmpResponse, reportType, shopInfoDTO, marketplaceEnum, reportsApi);
                DmpInputTaskInitDTO dmpInputTaskInitDTO = inputTaskInitDTOS.get(0);
                String msg = dmpInputTaskInitDTO.getMsg();
                redisUtil.set(redisKey, msg, 7200);
                reportIdObj = JSONObject.parseObject(msg);
            } else {
                reportIdObj = JSONObject.parseObject(reportInfoObj.toString());
            }
            String reportId = reportIdObj.getString("reportId");
            // 直接查询报告
            return querySpecReportIds((DmpInputInitResponse) dmpResponse, reportType, reportId, reportsApi, amazonLimitInfoDTO.getRateLimitStr(), amazonLimitInfoDTO.getLimitKey(), shopInfoDTO, checkNewDateEndTime);
        }
    }

    private List<DmpInputTaskInitDTO> creatReport(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse, String reportType, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketplaceEnum, ReportsApi reportsApi) {
        List<String> marketplaceIds = checkReportIsMergeMarketplace(reportType, shopInfoDTO);
        
        // 默认请求速率配置
        AmazonLimitInfoDTO amazonLimitInfoDTO = checkAndLimitInfo(AmazonRequestTypeRateLimiterEnum.REPORTS_CREATE, shopInfoDTO.getPlatformShopCode());
        if (amazonLimitInfoDTO.isLimitFlag()) {
            log.warn("【亚马逊创建报告】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        
        String rateLimitStr = amazonLimitInfoDTO.getRateLimitStr();
        String limitKey = amazonLimitInfoDTO.getLimitKey();

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
        // 组合响应
        return convertDmpInputTaskInitDTOS(reportId, reportType, shopInfoDTO);
    }




}
