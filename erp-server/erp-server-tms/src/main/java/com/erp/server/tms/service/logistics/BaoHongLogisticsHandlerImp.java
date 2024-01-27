package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import com.sdk.tms.baohong.service.BaoHongService;
import com.sdk.wms.goodcang.dto.response.GoodCangLogisticsProductsResp;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BAO_HONG)
public class BaoHongLogisticsHandlerImp extends AbstractLogisticsHandler {
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private BaoHongService baoHongService;

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.BAO_HONG_AUTHORIZE;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return new HashMap<>() ;
        }
        if (Objects.isNull(cfgAppClient)) return new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        map.put("orderId", "8182808069884648");
        map.put("childOrderId","8182808069884648");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(authId)) {
//            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(authId);
//            if (Objects.nonNull(shopAuth)) {
//                map.put("shopId", shopAuth.getShopId());
//                map.put("token", shopAuth.getAccessToken());
//            }
        }
        return map;
    }

    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        return super.getLogisticsAuthConfigByPlatform(platform);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return super.createOrder(logisticsOrderVO);
    }

    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return super.confirmOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return super.cancelOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return super.interceptOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return super.updateOrder(logisticsOrderVOS);
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return super.queryOrderList(logisticsQueryVOList);
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        return super.getLabelList(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        return super.getTrack(logisticsTrackVO);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return super.getChannel(chanelQueryVO);
    }

    @Override
    public ApiResult authorization(Map<String, String> authMap) {
        try {
            TransferLogisticsContext.setAuthMap(authMap);

            BaoHongResponse<List<SmRow>> response = baoHongService.getShippingMethodList();
            if (isFailure(response)) {
                return failure("授权失败:" + response.getMessage());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        } finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        return super.registerLogisticsNumber(registerTrackVO);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.BAO_HONG;
    }

    private boolean isFailure(BaoHongResponse<?> response){
        return response.getAsk().equals("0");
    }
}
