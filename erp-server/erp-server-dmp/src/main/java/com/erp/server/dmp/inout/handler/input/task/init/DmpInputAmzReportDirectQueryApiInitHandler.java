package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.GetReportsResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * 直接查询亚马逊最新Listing报告
     */
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String extendJson = dmpCfgInputEntity.getExtendJson();
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (null == extendObj) {
            ServiceException.runError("extendJson参数为空");
        }
        String reportType = extendObj.getString("reportType");

        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, dmpInputTaskEntity.getId(), AmzReportTaskStatusEnum.DIRECT_QUERY.getCode());
        Object reportObj = redisUtil.get(key);
        if (null != reportObj) {
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(reportObj.toString()));
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
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        ApiResponse<GetReportsResponse> reportsWithHttpInfo;
        Report report;
        try {
            List<String> reportTypes = Collections.singletonList(reportType);
            List<String> processingStatuses = Collections.singletonList(Report.ProcessingStatusEnum.DONE.getValue());
            Integer pageSize = 1;
            String createdSince = null;
            String createdUntil = null;
            String nextToken = null;
            List<String> marketplaceIds = Collections.singletonList(marketplaceEnum.getMarketplaceId());
            reportsWithHttpInfo = reportsApi.getReportsWithHttpInfo(reportTypes, processingStatuses, marketplaceIds, pageSize, createdSince, createdUntil, nextToken);
            ReportList reportList = reportsWithHttpInfo.getData().getReports();
            report = reportList.stream().findFirst().orElse(null);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊报告查询】 platformShopCode={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 查询最新listing失败:body=" + e.getMessage());
        }

        JSONObject jsonObject = (JSONObject) JSON.toJSON(report);
        // 补充其他信息
        jsonObject.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        jsonObject.put("createdMethod", "query");
        jsonObject.put("shopId", shopInfoDTO.getId());
        if (null != report) {
            jsonObject.put("marketplaceIds", String.join(",", report.getMarketplaceIds()));
        }
        String resultJson = JSONUtil.toJsonStr(jsonObject);
        if (null != report) {
            // 设置到缓存(已完成或结束删除)
            redisUtil.set(key, resultJson, 600);
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(resultJson));
    }

    public static void main(String[] args) {
        BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal("2"), 8, RoundingMode.DOWN);
        System.out.println(timeOut.longValue());
    }
}
