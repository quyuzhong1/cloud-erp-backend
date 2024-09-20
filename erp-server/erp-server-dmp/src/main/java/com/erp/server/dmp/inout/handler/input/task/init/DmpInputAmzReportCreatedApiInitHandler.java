package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonCreateReportResultDTO;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.CfgAmzReportTypeEntity;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonReportRecordTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportSpecification;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiConfigUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportCreatedApiInitHandler extends DmpInputInitHandler {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAmzReportTypeService cfgAmzReportTypeService;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private AmzReportHandleService AmzReportHandleService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String extendJson = dmpCfgInputEntity.getExtendJson();
        JSONObject extendObj = JSONObject.parseObject(extendJson);
        if (null == extendObj) {
            ServiceException.runError("extendJson参数为空");
        }
        String reportType = extendObj.getString("reportType");

        // 获取店铺信息
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            ServiceException.runError("未找到店铺授权:" + shopId);
        }

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
            throw new ServiceException(msg);
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
            dmpInputInitResponse.setDoNextChain(false);
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
                BigDecimal timeOut = BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN);
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
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
    public Long requestAndCheckWaitTime(String reportType, ReportsApi reportsApi, AmazonShopInfoDTO shopInfoDTO) {
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
        return AmzReportHandleService.queryProcessReportWaitTime(reportsApi, reportTypes, dmpCfgInputEntity.getId(), shopInfoDTO.getPlatformShopCode());
    }

    /**
     * 组合请求参数
     */
    private CreateReportSpecification createReportSpecificationParam(String reportType, List<String> marketplaceIdsArray) {
        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(reportType);
        body.setMarketplaceIds(marketplaceIdsArray);
        String startTime = dmpCfgInputDetailEntity.getLastTime().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();
        String endTime = dmpCfgInputDetailEntity.getNextTime().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toOffsetDateTime()
                .toString();
        body.setDataStartTime(startTime);
        body.setDataEndTime(endTime);
        return body;
    }
}
