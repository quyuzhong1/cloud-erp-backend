package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.walmart.WalmartCarriersDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Jim
 * @ClassName WalmartLogisticsHandlerImpl
 * @description: 沃尔玛物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WALMART)
public class WalmartLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private WalmartSdkClientService walmartSdkClientService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private LogisticsOperateService logisticsOperateService;




    /**
     * 根据平台获取授权列表
     *
     * @param platform
     * @return
     */
    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.WALMART_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return Collections.emptyList();
        }
        if (Objects.isNull(cfgAppClient)) return Collections.emptyList();

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("id", cfgAppClient.getId());
        return Collections.singletonList(map);
    }




    /**
     * 渠道查询
     *
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            Map<String, String> authMap = chanelQueryVO.getAuthMap();
            String clientId = authMap.get("clientId");
            String clientSecret = authMap.get("clientSecret");
            String baseUrl = WalmartStaticKey.baseUrl + "token";
            // 获取权限
            WalmartTokenDTO walmartTokenDTO = walmartSdkClientService.sendWalmartPostToken(baseUrl, clientId, clientSecret);
            String accessToken = walmartTokenDTO.getAccessToken();

            String carriersBaseUrl = WalmartStaticKey.baseUrl + "shipping/labels/carriers";
            //请求参数
            HashMap<String, Object> paramMap = new HashMap<>();
            String jsonResult = walmartSdkClientService.sendWalmartGet(carriersBaseUrl, clientId, clientSecret, accessToken, paramMap);

            WalmartCarriersDTO resultDTO = JSONUtil.toBean(jsonResult, WalmartCarriersDTO.class);

            List<WalmartCarriersDTO.Carrier> carrierList = resultDTO.getCarriers();
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WALMART.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(jsonResult));
            return success(LogisticsChannelConverter.INSTANCE.channelConvertByWalmart(carrierList));

        } catch (Exception e) {
            log.error("沃尔玛getChannel接口调用失败：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WALMART.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }
    }


    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WALMART;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
