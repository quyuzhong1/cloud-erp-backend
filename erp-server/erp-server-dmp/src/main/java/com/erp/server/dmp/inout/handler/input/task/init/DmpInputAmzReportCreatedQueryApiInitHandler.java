package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
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
import com.erp.model.dmp.entity.DmpAmzReportInfoEntity;
import com.erp.model.dmp.enums.AmzReportTaskStatusEnum;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.reports.GetReportsResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.Report;
import com.erp.sdk.oms.amz.spapi.model.reports.ReportList;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpAmzReportInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 *
 * @author Jim
 */
@Service
@Scope("prototype")
public class DmpInputAmzReportCreatedQueryApiInitHandler extends DmpInputInitHandler {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private DmpAmzReportInfoService dmpAmzReportInfoService;

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
        // 店铺信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(dmpInputTaskEntity.getNextLevelId());
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 指定报告ID解析
        String reportId = "";
        String detailExtendJson = dmpInputTaskEntity.getExtendJson();
        if (StringUtils.isNotBlank(detailExtendJson)) {
            JSONObject detailExtendObj = JSONObject.parseObject(detailExtendJson);
            reportId = detailExtendObj.getString("reportId");
        }


        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_INFO_PREFIX, dmpInputTaskEntity.getId(), AmzReportTaskStatusEnum.CREATED.getCode());
        if (StringUtils.isBlank(reportId)){
            // 查询处理中的报告
            DmpAmzReportInfoEntity reportEntity = dmpAmzReportInfoService.lambdaQuery()
                    .eq(DmpAmzReportInfoEntity::getPlatformShopCode, shopInfoDTO.getPlatformShopCode())
                    .eq(DmpAmzReportInfoEntity::getReportType, reportType)
                    .in(DmpAmzReportInfoEntity::getProcessingStatus, Arrays.asList(Report.ProcessingStatusEnum.IN_PROGRESS.getValue(), Report.ProcessingStatusEnum.IN_QUEUE.getValue()))
                    .last(" LIMIT 1")
                    .one();
            if (null == reportEntity){
                return Collections.emptyList();
            }

            // 从缓存获取(已完成或结束删除)
            Object reportObj = redisUtil.get(key);
            if (null != reportObj) {
                return Collections.singletonList(DmpInputTaskInitDTO.initMsg(reportObj.toString()));
            }
            reportId = reportEntity.getReportId();
        }


        // 请求亚马逊接口
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        Report report;
        try {
            report = reportsApi.getReport(reportId);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = (JSONObject) JSON.toJSON(report);
        // 补充其他信息
        jsonObject.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        jsonObject.put("createdMethod", "system");
        jsonObject.put("shopId", shopInfoDTO.getId());
        if (null != report) {
            jsonObject.put("marketplaceIds", String.join(",", report.getMarketplaceIds()));
            jsonObject.put("processingStatus", report.getProcessingStatus().getValue());
        }
        String resultJson = JSONUtil.toJsonStr(jsonObject);
        if (null != report) {
            // 设置到缓存(已完成或结束删除)
            redisUtil.set(key, resultJson, 600);
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(resultJson));
    }


}
