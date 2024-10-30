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
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceIdsTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.GetReportsResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下
 * 亚马逊sp-api 直接查询最新报告
 * 获取数据方式
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportDirectQueryApiInitHandler extends DmpInputInitHandler {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;

    /**
     * 直接查询亚马逊最新Listing报告
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String extendJson = dmpCfgInputEntity.getExtendJson();
        // 主单配置
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (null == extendObj) {
            ServiceException.runError("extendJson参数为空");
        }
        String reportType = extendObj.getString("reportType");

        // 明细配置
        // 是否检查数据最后时间
        boolean checkNewDateEndTime = false;
        // 指定请求站点类型
        String marketplaceIdsType = "";
        // 指定报告ID
        String reportId = "";

        String detailExtendJson = dmpCfgInputDetailEntity.getExtendJson();
        if (StringUtils.isNotBlank(detailExtendJson)) {
            JSONObject detailExtendObj = JSONObject.parseObject(detailExtendJson);
            reportId = detailExtendObj.getString("reportId");
            marketplaceIdsType = detailExtendObj.getString("marketplaceIdsType");
            checkNewDateEndTime = detailExtendObj.getBooleanValue("checkNewDateEndTime");
        }

        // 店铺信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(dmpCfgInputDetailEntity.getNextLevelId());
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_QUERY;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 校验速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【亚马逊报告查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextChain(false);
            return Collections.emptyList();
        }
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        // 请求亚马逊接口
        ReportsApi reportsApi = AmazonSpApiInitUtils.create(ReportsApi.class, shopInfoDTO, false);

        if (StringUtils.isNotBlank(reportId)) {
            // 查询指定报告
            return querySpecReportIds((DmpInputInitResponse) dmpResponse, reportType, reportId, marketplaceEnum, reportsApi, rateLimitStr, limitKey, shopInfoDTO, checkNewDateEndTime);
        } else {
            // 查询最新报告
            return queryNewReport((DmpInputInitResponse) dmpResponse, reportType, marketplaceEnum, reportsApi, rateLimitStr, limitKey, shopInfoDTO, checkNewDateEndTime, marketplaceIdsType);
        }

    }

    /**
     * 查询最新报告IDS
     */
    private List<DmpInputTaskInitDTO> querySpecReportIds(DmpInputInitResponse dmpResponse, String reportType, String reportId, AmazonMarketplaceEnum marketplaceEnum, ReportsApi reportsApi, String rateLimitStr, String limitKey, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime) {
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
                dmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 查询指定报告 "+ reportType + "失败:body=" + JSONUtil.toJsonStr(e));
        }

        // 是否校验数据结束时间最新
        return checkAndConvert(reportType, shopInfoDTO, checkNewDateEndTime, report);

    }


    /**
     * 查询最新报告
     */
    private List<DmpInputTaskInitDTO> queryNewReport(DmpInputInitResponse dmpResponse, String reportType, AmazonMarketplaceEnum marketplaceEnum, ReportsApi reportsApi, String rateLimitStr, String limitKey, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime, String marketplaceIdsType) {
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
                dmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 查询最新listing失败:body=" + JSONUtil.toJsonStr(e));
        }

        // 校验中台是否已存在
        DmpAmzReportInfoEntity reportInfo = dmpAmzReportInfoService.getByReportId(report.getReportId(), Report.ProcessingStatusEnum.DONE.getValue());
        if (null != reportInfo) {
            log.warn("[Amazon SP-APi] 查询最新listing最新报告已存在跳过:{}", report.getReportId());
            return Collections.emptyList();
        }
        // 是否校验数据结束时间最新
        return checkAndConvert(reportType, shopInfoDTO, checkNewDateEndTime, report);
    }

    /**
     * 通过配置marketplaceIdsType获取当前请求站点IDS
     */
    private List<String> checkAndGetMarketplaceIdsByCfgType(String marketplaceIdsType, AmazonMarketplaceEnum marketplaceEnum, AmazonShopInfoDTO shopInfoDTO) {
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
    private List<DmpInputTaskInitDTO> checkAndConvert(String reportType, AmazonShopInfoDTO shopInfoDTO, Boolean checkNewDateEndTime, Report report) {
        if (checkNewDateEndTime) {
            DmpAmzReportInfoEntity newReport = dmpAmzReportInfoService.getOneByNewEndDate(reportType);
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

}
