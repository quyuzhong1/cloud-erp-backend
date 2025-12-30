package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.ReportsApi;
import com.erp.sdk.oms.amz.spapi.dto.AmazonLimitInfoDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
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
public class DmpInputAmzReportDirectQueryApiInitHandler extends DmpInputAmzReportCommonApiInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;

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
        if (StringUtils.isBlank(reportType)){
            ServiceException.runError("报告类型reportType不能为空");
        }

        // 明细配置
        // 是否检查数据最后时间
        boolean checkNewDateEndTime = false;
        // 指定请求站点类型
        String marketplaceIdsType = "";
        // 指定报告ID
        String reportId = "";

        String detailExtendJson = dmpInputTaskEntity.getExtendJson();
        if (StringUtils.isNotBlank(detailExtendJson)) {
            JSONObject detailExtendObj = JSONObject.parseObject(detailExtendJson);
            reportId = detailExtendObj.getString("reportId");
            marketplaceIdsType = detailExtendObj.getString("marketplaceIdsType");
            checkNewDateEndTime = detailExtendObj.getBooleanValue("checkNewDateEndTime");
        }

        // 店铺信息
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(dmpInputTaskEntity.getNextLevelId());
        // 市场信息
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 默认请求速率配置
        AmazonLimitInfoDTO amazonLimitInfoDTO = checkAndLimitInfo(AmazonRequestTypeRateLimiterEnum.REPORTS_QUERY, shopInfoDTO.getPlatformShopCode());
        if (amazonLimitInfoDTO.isLimitFlag()) {
            log.warn("【亚马逊报告查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        String rateLimitStr = amazonLimitInfoDTO.getRateLimitStr();
        String limitKey = amazonLimitInfoDTO.getLimitKey();

        // 请求亚马逊接口
        ReportsApi reportsApi = AmazonSpApiInitUtils.create(ReportsApi.class, shopInfoDTO, false);

        if (StringUtils.isNotBlank(reportId)) {
            // 查询指定报告
            return querySpecReportIds((DmpInputInitResponse) dmpResponse, reportType, reportId, reportsApi, rateLimitStr, limitKey, shopInfoDTO, checkNewDateEndTime);
        } else {
            // 查询最新报告
            return queryNewReport((DmpInputInitResponse) dmpResponse, reportType, marketplaceEnum, reportsApi, rateLimitStr, limitKey, shopInfoDTO, checkNewDateEndTime, marketplaceIdsType);
        }

    }




}
