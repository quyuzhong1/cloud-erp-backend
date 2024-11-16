package com.erp.server.tms.service.logistics;


import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.tms.tiktok.channel.provider.ShippingProvidersBean;
import com.sdk.tms.tiktok.service.TikTokShipperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TikTok物流
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TIK_TOK)
public class TikTokLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokShipperService tikTokShipperService;


    /**
     * 查询店铺
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        //获取商铺配置信息
        Map<String, String> map = new HashMap<>();
        map.put("shopId", shopId);
        return map;
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        List<LogisticsSaleChannelEntity> resuletList = new ArrayList<>();
        List<ShippingProvidersBean> providersBeanList = tikTokShipperService.sendTikTokLogisticsChannel(authMap.get("shopId"));
        for (ShippingProvidersBean providerDTO : providersBeanList) {
            LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity()
                    .setCode(providerDTO.getId())
                    .setPlatformChannelId(providerDTO.getId())
                    .setCnName(providerDTO.getName())
                    .setLogisticsPlatform(LogisticsPlatformEnum.TIK_TOK.getCode());
            resuletList.add(logisticsSaleChannelEntity);
        }
        return success(resuletList);
    }

    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TIK_TOK;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
