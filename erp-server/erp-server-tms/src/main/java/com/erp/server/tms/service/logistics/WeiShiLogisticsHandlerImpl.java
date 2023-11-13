package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.weishi.dto.request.*;
import com.sdk.tms.weishi.dto.response.*;
import com.sdk.tms.weishi.server.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WEI_SHI)
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
        WeiShiResponse<List<WeiShiChannel>> weiShiResponse =  weiShiService.getAllChannel(chanelQueryVO.getLogisticsAuthEntity());
        if(isFailure(weiShiResponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByWeiShi(weiShiResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        WeiShiCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByWeiShi(logisticsOrderVO);
        WeiShiCreateOrder weiShiResponse = weiShiService.createOrder(request,logisticsOrderVO.getLogisticsAuthEntity());
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
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        for(LogisticsGetLabelVO logisticsGetLabelVO : labelVO){
            WeiShiGetLabelUrlRequest request = WeiShiGetLabelUrlRequest.builder()
                    .referenceNo(logisticsGetLabelVO.getDeliveryNo())
                    .build();
            WeiShiGetLabelUrl weiShiGetLabelUrlResponse = weiShiService.getLabelUrl(request,logisticsGetLabelVO.getLogisticsAuthEntity());
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
            response.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
            response.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
            if(isFailure(weiShiGetLabelUrlResponse.getAsk())){
                response.failure(getName(),logisticsGetLabelVO.getDeliveryNo(),weiShiGetLabelUrlResponse.getMessage());
            }else{
                response.setBase64(FileUtil.convertPdfUrlToBase64(weiShiGetLabelUrlResponse.getUrl()));
                response.success();
            }
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
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<String> deliveryList = logisticsQueryVOList.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList());
        WeiShiGetTrackNumberRequest weiShiCancelOrderRequest = WeiShiGetTrackNumberRequest.builder()
                .referenceNoList(deliveryList)
                .build()
                ;
        WeiShiResponse<List<WeiShiGetTrackNumber>> weiShiresponse = weiShiService.getTrackNumber(weiShiCancelOrderRequest,logisticsQueryVOList.get(0).getLogisticsAuthEntity());
        if(isFailure(weiShiresponse.getAsk())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
        }
        List<LogisticsOrderResponseVO> response = LogisticsOrderConverter.INSTANCE.trackInfoConvertByWeiShi(weiShiresponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        List<InterceptResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsInterceptOrderVO interceptOrderVO : logisticsQueryVO){
            WeiShiInterceptOrderRequest weiShiInterceptOrderRequest = WeiShiInterceptOrderRequest.builder()
                    //客户单号
                    .referenceNo(interceptOrderVO.getDeliveryNo())
                    .build();
            WeiShiResponse<String> weiShiresponse = weiShiService.interceptOrder(weiShiInterceptOrderRequest,interceptOrderVO.getLogisticsAuthEntity());
            InterceptResponseVO interceptResponseVO = LogisticsOperationOrderConverter.INSTANCE.interceptOrderCovert(interceptOrderVO);
            if(isFailure(weiShiresponse.getAsk())){
                isSuccess = false;
                interceptResponseVO.failure(getName(),interceptOrderVO.getDeliveryNo(),weiShiresponse.getError().getErrMessage());
            }else{
                interceptResponseVO.success();
            }
            result.add(interceptResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList)  {
        List<CancelResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsCancelOrderVO cancelOrderVO : cancelOrderVOList){
            WeiShiCancelOrderRequest request = WeiShiCancelOrderRequest.builder()
                    .referenceNo(cancelOrderVO.getDeliveryNo())
                    .build();
            WeiShiResponse<String> weiShiResponse = weiShiService.cancelOrder(request,cancelOrderVO.getLogisticsAuthEntity());
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            if(isFailure(weiShiResponse.getAsk())){
                isSuccess = false;
                cancelResponseVO.failure(getName(),cancelOrderVO.getDeliveryNo(),weiShiResponse.getMessage());
            }else{
                cancelResponseVO.success();
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }
    private Boolean isFailure(String ask){
        return !TmsConstant.SUCCESS.equals(ask);
    }

    private String getName(){return getPlatForm().getName();};

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEI_SHI;
    }
}
