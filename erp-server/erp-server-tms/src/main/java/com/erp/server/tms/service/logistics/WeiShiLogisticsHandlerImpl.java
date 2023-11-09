package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.weishi.dto.request.*;
import com.sdk.tms.weishi.dto.response.*;
import com.sdk.tms.weishi.server.WeiShiService;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.response.YanWenGetLabel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.WEI_SHI)
public class WeiShiLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private WeiShiService weiShiService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        WeiShiResponse<List<WeiShiChannel>> weiShiResponse =  weiShiService.getAllChannel();
        if(isFailure(weiShiResponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByWeiShi(weiShiResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        WeiShiCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByWeiShi(logisticsOrderVO);
        WeiShiCreateOrder weiShiResponse = weiShiService.createOrder(request);
        if(isFailure(weiShiResponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
        }
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(weiShiResponse.getOrderCode())
                .deliveryNo(weiShiResponse.getReferenceNo())
                .trackNo(weiShiResponse.getOrderCode())
                .build());
    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO labelVO) throws IOException {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        for(String deliveryNo : labelVO.getDeliveryNo()){
            WeiShiGetLabelUrlRequest request = WeiShiGetLabelUrlRequest.builder()
                    .referenceNo(deliveryNo)
                    .build();
            WeiShiGetLabelUrl weiShiGetLabelUrlResponse = weiShiService.getLabelUrl(request);
            if(isFailure(weiShiGetLabelUrlResponse.getAsk())){
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,deliveryNo+weiShiGetLabelUrlResponse.getError().getErrMessage());
            }
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setBase64(FileUtil.convertPdfUrlToBase64(weiShiGetLabelUrlResponse.getUrl()));
            response.setDeliveryNoList(Collections.singletonList(deliveryNo));
            result.add(response);
        }
        return success(result);
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList) {
        WeiShiGetTrackNumberRequest weiShiCancelOrderRequest = WeiShiGetTrackNumberRequest.builder()
                .referenceNoList(logisticsQueryVOList.getDeliveryNo())
                .build()
                ;
        WeiShiResponse<List<WeiShiGetTrackNumber>> weiShiresponse = weiShiService.getTrackNumber(weiShiCancelOrderRequest);
        if(isFailure(weiShiresponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
        }
        List<LogisticsOrderResponseVO> response = LogisticsOrderConverter.INSTANCE.trackInfoConvertByWeiShi(weiShiresponse.getData());
        return success(response);
    }

//    @Override
//    public ApiResult<String> interceptOrder(LogisticsInterceptOrderVO logisticsQueryVO) {
//        WeiShiInterceptOrderRequest weiShiInterceptOrderRequest = WeiShiInterceptOrderRequest.builder()
//                .referenceNo(logisticsQueryVO.getDeliveryNo().get(0))
//                .build();
//        WeiShiResponse<String> weiShiresponse = weiShiService.interceptOrder(weiShiInterceptOrderRequest);
//        if(isFailure(weiShiresponse.getAsk())){
//            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
//        }
//        return success(weiShiresponse.getData());
//    }

    @Override
    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO cancelOrderVO) {
        WeiShiCancelOrderRequest weiShiInterceptOrderRequest = WeiShiCancelOrderRequest.builder()
                .referenceNo(cancelOrderVO.getDeliveryNo().get(0))
                .build();
        WeiShiResponse<String> weiShiresponse = weiShiService.cancelOrder(weiShiInterceptOrderRequest);
        if(isFailure(weiShiresponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
        }
        return success(weiShiresponse.getData());
    }
    private Boolean isFailure(String ask){
        return !TmsConstant.SUCCESS.equals(ask);
    }
}
