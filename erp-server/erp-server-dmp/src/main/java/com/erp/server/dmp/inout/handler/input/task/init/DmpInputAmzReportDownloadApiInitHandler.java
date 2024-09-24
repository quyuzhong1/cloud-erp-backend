package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportDocument;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiReportUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 * 下载报告
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportDownloadApiInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(findMongoData)) {
            // 主数据不存在明细无需处理
            log.warn("亚马逊报告下载主任务taskId={},结果为空无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        Map<String, Object> mongoObjectMap = findMongoData.get(0);
        Object statusObj = mongoObjectMap.get("processingStatus");
        if (null == statusObj) {
            log.error("报告状态不存在:{}", mongoObjectMap);
            return Collections.emptyList();
        }
        if (!Report.ProcessingStatusEnum.DONE.getValue().equalsIgnoreCase(statusObj.toString())) {
            log.warn("报告状态非完成跳过下载:{}", mongoObjectMap);
            return Collections.emptyList();
        }
        // 报告ID
        String reportId = checkAndGetMongoValue(mongoObjectMap, "reportId");
        // 报告类型
        String reportType = checkAndGetMongoValue(mongoObjectMap, "reportType");
        // 店铺ID
        String shopId = checkAndGetMongoValue(mongoObjectMap, "shopId");
        // 店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.REPORTS_DOCUMENT_QUERY;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 校验速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【亚马逊报告文档查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextChain(false);
            return Collections.emptyList();
        }
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        // 请求获取亚马逊报告文档信息
        ReportDocument reportDocument = null;
        try {
            reportDocument = queryReportDocument(mongoObjectMap, shopInfoDTO);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊报告文档查询】 platformShopCode={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            throw new ServiceException("[Amazon SP-APi] 亚马逊报告文档查询失败:body=" + JSONUtil.toJsonStr(e));
        }

        // 根据url下载到FastDFS
        String compressionAlgorithm = null == reportDocument.getCompressionAlgorithm() ? "" : reportDocument.getCompressionAlgorithm().getValue();
        String fileName = StrUtil.subBetween(reportDocument.getUrl(), ".com/", "?");
        String fastDFSUrl = AmazonSpApiReportUtils.downloadAndUploadFastDFS(reportDocument.getUrl(), compressionAlgorithm, fileName, reportDocument.getReportDocumentId(), reportType);

        // 记录报告路径信息
        JSONObject jsonObject = (JSONObject) JSON.toJSON(reportDocument);
        // 补充其他信息
        jsonObject.put("filePath", fastDFSUrl);
        jsonObject.put("reportId", reportId);

        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonObject)));
    }

    /**
     * 请求亚马逊报告文档信息
     *
     * @param mongoObjectMap mongo报告信息
     * @return 亚马逊报告文档对象
     */
    private ReportDocument queryReportDocument(Map<String, Object> mongoObjectMap, AmazonShopInfoDTO shopInfoDTO) throws ApiException {
        // 报告文档ID
        String reportDocumentId = checkAndGetMongoValue(mongoObjectMap, "reportDocumentId");

        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, dmpInputTaskEntity.getParentTaskId(), AmzReportTaskStatusEnum.DOWNLOAD.getCode());
        Object reportDocumentObj = redisUtil.get(key);
        if (null != reportDocumentObj) {
            return JSONUtil.toBean(reportDocumentObj.toString(), ReportDocument.class);
        }

        // 请求亚马逊接口
        ReportsApi reportsApi = AmazonSpApiInitUtils.create(ReportsApi.class, shopInfoDTO, false);

        ApiResponse<ReportDocument> respWithHttpInfo = reportsApi.getReportDocumentWithHttpInfo(reportDocumentId);
        ReportDocument reportDocument = respWithHttpInfo.getData();

        List<String> xAmzExpiresValues = UriComponentsBuilder.fromHttpUrl(reportDocument.getUrl()).build().getQueryParams().get("X-Amz-Expires");
        // 提取过期时间的值
        int xAmzExpires = Integer.parseInt(xAmzExpiresValues.stream().findFirst().orElse("300")) - 1;
        // 设置到缓存(已完成或结束删除)
        redisUtil.set(key, JSONUtil.toJsonStr(reportDocument), xAmzExpires);

        return reportDocument;

    }
}
