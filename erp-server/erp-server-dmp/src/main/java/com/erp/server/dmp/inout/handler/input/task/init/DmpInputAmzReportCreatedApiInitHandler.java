package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportResponse;
import com.erp.sdk.oms.amz.spapi.model.reports.CreateReportSpecification;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.AmzReportHandleService;
import com.erp.server.dmp.service.CfgAmzReportTypeService;
import com.erp.server.dmp.service.CfgAppClientService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的亚马逊api获取数据方式
 *
 * @author Jim
 */
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
        JSONArray marketplaceIdsArray = extendObj.getJSONArray("marketplaceIds");

        // 从缓存获取(已完成或结束删除)
        String key = StrUtil.format(RedisCacheConstants.AMZ_REPORT_RESULT_PREFIX, dmpCfgInputEntity.getId(), AmzReportTaskStatusEnum.CREATED.getCode());
        Object reportIdObj = redisUtil.get(key);
        if (null != reportIdObj) {
            String reportId = (String) reportIdObj;
            String jsonStr = JSONUtil.toJsonStr(AmazonCreateReportResultDTO.success(reportId));
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(jsonStr));
        }
        // 校验MarketplaceId
        if (marketplaceIdsArray.isEmpty()) {
            ServiceException.runError("未找到MarketplaceId,cfgInputId=" + dmpCfgInputEntity.getId());
        }
        Object marketplaceObj = marketplaceIdsArray.stream().findFirst().orElse(null);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByMarketplaceId(marketplaceObj.toString());
        if (null == marketplaceEnum) {
            String msg = StrUtil.format("未找到Marketplace枚举类型,cfgInputId={}, marketplaceId={}", dmpCfgInputEntity.getId(), marketplaceIdsArray);
            ServiceException.runError(msg);
        }
        // 获取店铺信息
        String shopId = dmpCfgInputDetailEntity.getNextLevelId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        if (null == shopInfoDTO) {
            ServiceException.runError("未找到店铺授权:" + shopId);
        }
        ReportsApi reportsApi = ReportsApi.initApi(marketplaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);

        // 获取同组报告类型配置
        // 请求创建中报告预计时间
        Long estimatedWaitSecond = requestAndCheckWaitTime(reportType, reportsApi, shopInfoDTO);
        if (estimatedWaitSecond > 0) {
            // TODO 修改下次时间？
            String jsonStr = JSONUtil.toJsonStr(AmazonCreateReportResultDTO.wait(estimatedWaitSecond));
            return Collections.singletonList(DmpInputTaskInitDTO.initMsg(jsonStr));
        }

        // 组合请求参数
        CreateReportSpecification body = createReportSpecificationParam(reportType, marketplaceIdsArray);
        ApiResponse<CreateReportResponse> reportWithHttpInfo = null;
        try {
            // 请求亚马逊创建报告接口
            reportWithHttpInfo = reportsApi.createReportWithHttpInfo(body);
        } catch (ApiException e) {
            ServiceException.runError(ExceptionUtil.stacktraceToString(e));
        }
        CreateReportResponse reportResponse = reportWithHttpInfo.getData();
        String reportId = reportResponse.getReportId();
        if (null == reportId) {
            ServiceException.runError("请求亚马逊创建报告失败：body=" + JSONUtil.toJsonStr(reportResponse));
        }
        // 设置到缓存(已完成或结束删除)
        redisUtil.set(key, reportId);
        String jsonStr = JSONUtil.toJsonStr(AmazonCreateReportResultDTO.success(reportId));
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(jsonStr));
    }

    /**
     * 请求创建中报告预计时间
     */
    private Long requestAndCheckWaitTime(String reportType, ReportsApi reportsApi, AmazonShopInfoDTO shopInfoDTO) {
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
    private CreateReportSpecification createReportSpecificationParam(String reportType, JSONArray marketplaceIdsArray) {
        CreateReportSpecification body = new CreateReportSpecification();
        body.setReportType(reportType);
        body.setMarketplaceIds(marketplaceIdsArray.stream().map(Object::toString).collect(Collectors.toList()));
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
}
