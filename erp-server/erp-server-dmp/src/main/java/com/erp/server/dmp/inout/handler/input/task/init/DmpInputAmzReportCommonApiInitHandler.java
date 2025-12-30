package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.dto.AmazonLimitInfoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceIdsTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportSpecification;
import com.erp.sdk.oms.amz.spapi.model.reports.GetReportsResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public abstract class DmpInputAmzReportCommonApiInitHandler extends DmpInputAmzCommonInitHandler{

    @Resource
    protected RedisUtil redisUtil;
    @Resource
    protected DmpAmzReportInfoService dmpAmzReportInfoService;
    @Resource
    protected CfgAmzReportTypeService cfgAmzReportTypeService;
    @Resource
    protected AmzReportHandleService amzReportHandleService;

    /**
     * 通过配置marketplaceIdsType获取当前请求站点IDS
     */
    protected List<String> checkAndGetMarketplaceIdsByCfgType(String marketplaceIdsType, AmazonMarketplaceEnum marketplaceEnum, AmazonShopInfoDTO shopInfoDTO) {
        // 无指定站点
        if (AmazonMarketplaceIdsTypeEnum.NONE.getCode().equalsIgnoreCase(marketplaceIdsType)){
            return null;
        }
        // 所有授权站点
        if (AmazonMarketplaceIdsTypeEnum.ALL_AUTH.getCode().equalsIgnoreCase(marketplaceIdsType)){
            return new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
        }
        // 默认当前站点
        return Collections.singletonList(marketplaceEnum.getMarketplaceId());
    }


    /**
     * 检查和转换
     */
    protected List<DmpInputTaskInitDTO> checkAndConvert(String reportType, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime, Report report) {
        if (checkNewDateEndTime) {
            DmpAmzReportInfoEntity newReport = dmpAmzReportInfoService.getOneByNewEndDate(reportType, shopInfoDTO.getPlatformShopCode(), String.join(",", report.getMarketplaceIds()));
            if (null != newReport) {
                // 当前数据最新时间
                OffsetDateTime offsetDateEndDateTime = DateUtil.parseOffsetDateTime(newReport.getDataEndTime());

                // 来源报告最新时间
                OffsetDateTime curEndDateDateTime = report.getDataEndTime();

                // 校验时间
                if (curEndDateDateTime.isBefore(offsetDateEndDateTime)) {
                    log.warn("[Amazon SP-APi] 查询最新报告【{}】对比历史报告数据结束时间晚, 跳过:ReportId={}", reportType, report.getReportId());
                    return Collections.emptyList();
                }
            }
        }

        JSONObject jsonObject = (JSONObject) JSON.toJSON(report);
        // 补充其他信息
        jsonObject.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        jsonObject.put("createdMethod", "query");
        jsonObject.put("shopId", shopInfoDTO.getId());

        jsonObject.put("marketplaceIds", String.join(",", report.getMarketplaceIds()));
        String resultJson = JSONUtil.toJsonStr(jsonObject);
        // 设置到缓存(已完成或结束删除)
//      redisUtil.set(key, resultJson, 600);
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(resultJson));
    }

    /**
     * 查询最新报告
     */
    protected List<DmpInputTaskInitDTO> queryNewReport(DmpInputInitResponse dmpResponse, String reportType, AmazonMarketplaceEnum marketplaceEnum, ReportsApi reportsApi, String rateLimitStr, String limitKey, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime, String marketplaceIdsType) {
        ApiResponse<GetReportsResponse> reportsWithHttpInfo;
        Report report;
        try {
            List<String> reportTypes = Collections.singletonList(reportType);
            List<String> processingStatuses = Collections.singletonList(Report.ProcessingStatusEnum.DONE.getValue());
            Integer pageSize = 1;
            String createdSince = null;
            String createdUntil = null;
            String nextToken = null;
            // 按配置解析当前请求站点IDS
            List<String> marketplaceIds = checkAndGetMarketplaceIdsByCfgType(marketplaceIdsType, marketplaceEnum, shopInfoDTO);
            reportsWithHttpInfo = reportsApi.getReportsWithHttpInfo(reportTypes, processingStatuses, marketplaceIds, pageSize, createdSince, createdUntil, nextToken);
            ReportList reportList = reportsWithHttpInfo.getData().getReports();
            report = reportList.stream().findFirst().orElse(null);
            if (null == report) {
                log.warn("[Amazon SP-APi] 查询最新【{}】报告为空:{}", reportType, JSONUtil.toJsonStr(reportList));
                return Collections.emptyList();
            }
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊报告查询】 platformShopCode={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                dmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 查询最新报告失败:body=" + JSONUtil.toJsonStr(e));
        }

        // 校验中台是否已存在
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(report.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null != reportInfo) {
            log.warn("[Amazon SP-APi] 查询最新报告{},已存在跳过:{}", report.getReportType(), report.getReportId());
            return Collections.emptyList();
        }
        // 是否校验数据结束时间最新
        return checkAndConvert(reportType, shopInfoDTO, checkNewDateEndTime, report);
    }

    /**
     * 查询指定报告IDS
     */
    protected List<DmpInputTaskInitDTO> querySpecReportIds(DmpInputInitResponse dmpResponse, String reportType, String reportId, ReportsApi reportsApi, String rateLimitStr, String limitKey, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime) {
        Report report = null;
        try {
            report = reportsApi.getReport(reportId);
            if (null == report) {
                log.warn("[Amazon SP-APi] 查询指定报告类型【{}】，报告ID【{}】报告为空:{}", reportType, reportId, JSONUtil.toJsonStr(report));
                return Collections.emptyList();
            }
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊报告查询】 查询指定报告类型【{}】，报告ID【{}】,platformShopCode={},当前触发429限流:放弃当前请求任务",
                        reportType,
                        reportId,
                        shopInfoDTO.getPlatformShopCode()
                );
                // 触发限流不执行当前
                dmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 查询指定报告 "+ reportType + "失败:body=" + JSONUtil.toJsonStr(e));
        }
        if (Report.ProcessingStatusEnum.IN_PROGRESS.equals(report.getProcessingStatus()) || Report.ProcessingStatusEnum.IN_QUEUE.equals(report.getProcessingStatus())){
            // 处理中报告中断等待结果
            dmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }

        // 是否校验数据结束时间最新
        return checkAndConvert(reportType, shopInfoDTO, checkNewDateEndTime, report);
    }

    /**
     * 检查喝获取限流信息
     */
    protected AmazonLimitInfoDTO checkAndLimitInfo(AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum, String platformShopCode) {
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
        // 校验速率
        Object limitObj = redisUtil.get(limitKey);
        return AmazonLimitInfoDTO.builder()
                .requestTypeRateLimiterEnum(requestTypeRateLimiterEnum)
                .limitKey(limitKey)
                .rateLimitStr(requestTypeRateLimiterEnum.getRateLimit())
                .limitFlag(null != limitObj)
                .build();

    }


    /**
     * 组合请求参数
     */
    protected CreateReportSpecification createReportSpecificationParam(String reportType, List<String> marketplaceIdsArray) {
        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(reportType);
        body.setMarketplaceIds(marketplaceIdsArray);
        String startTime = dmpInputTaskEntity.getStartTime().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();
        String endTime = dmpInputTaskEntity.getEndTime().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();
        body.setDataStartTime(startTime);
        body.setDataEndTime(endTime);
        return body;
    }


    /**
     * 转换响应
     */
    protected static List<DmpInputTaskInitDTO> convertDmpInputTaskInitDTOS(String reportId, String reportType, AmazonShopInfoDTO shopInfoDTO) {
        Report report = new Report();
        report.setReportId(reportId);
        report.setReportType(reportType);
        report.setProcessingStatus(Report.ProcessingStatusEnum.IN_PROGRESS);

        JSONObject jsonObject = (JSONObject) JSON.toJSON(report);
        // 补充其他信息
        jsonObject.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        jsonObject.put("createdMethod", "system");
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonObject)));
    }


    /**
     * 请求创建中报告预计时间
     */
    protected Long requestAndCheckWaitTime(String reportType, ReportsApi reportsApi, AmazonShopInfoDTO shopInfoDTO) {
        List<CfgAmzReportTypeEntity> list = cfgAmzReportTypeService.findActive(null);
        if (CollectionUtils.isEmpty(list)) {
            ServiceException.runError("未找到报告配置");
        }
        CfgAmzReportTypeEntity curReportType = list.stream().filter(e -> e.getReportType().equalsIgnoreCase(reportType)).findFirst().orElse(null);
        if (null == curReportType) {
            ServiceException.runError("未找到当前报告配置:" + reportType);
        }
        Map<String, List<CfgAmzReportTypeEntity>> reportGroupMap = list.stream().collect(Collectors.groupingBy(CfgAmzReportTypeEntity::getReportGroup));
        List<CfgAmzReportTypeEntity> curReportGroupList = reportGroupMap.get(curReportType.getReportGroup());
        List<String> reportTypes = curReportGroupList.stream().map(CfgAmzReportTypeEntity::getReportType).distinct().collect(Collectors.toList());

        // 查询是否有处理中的报告(响应预估处理结束时间:0=无处理中报告)
        return amzReportHandleService.queryProcessReportWaitTime(reportsApi, reportTypes, dmpCfgInputEntity.getId(), shopInfoDTO.getPlatformShopCode());
    }

    /**
     * 检查报告是否是合并站点
     */
    protected static List<String> checkReportIsMergeMarketplace(String reportType, AmazonShopInfoDTO shopInfoDTO) {
        List<String> marketplaceIds;
        AmazonReportRecordTypeEnum recordTypeEnum = AmazonReportRecordTypeEnum.checkAndGetByRecordType(reportType);
        if (recordTypeEnum.isHasMergeMarketplaces()){
            // 合并站点
            // 校验MarketplaceId
            marketplaceIds = new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet());
        } else {
            AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
            // 非合并站点
            marketplaceIds = Collections.singletonList(marketplaceEnum.getMarketplaceId());
        }
        return marketplaceIds;
    }

}
