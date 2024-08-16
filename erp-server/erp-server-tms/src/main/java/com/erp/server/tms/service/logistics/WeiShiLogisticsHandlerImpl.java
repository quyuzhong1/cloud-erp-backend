package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.weishi.dto.request.*;
import com.sdk.tms.weishi.dto.response.*;
import com.sdk.tms.weishi.server.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WEI_SHI)
public class WeiShiLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private WeiShiService weiShiService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            WeiShiResponse<List<WeiShiChannel>> weiShiResponse =  weiShiService.getAllChannel(chanelQueryVO.getAuthMap());
            if(isFailure(weiShiResponse.getAsk())){
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(weiShiResponse));

                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
            }
            List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByWeiShi(weiShiResponse.getData());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(weiShiResponse));
            return success(response);
        }catch (Exception e){
            log.error("纬狮渠道接口异常：{}",e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        WeiShiCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByWeiShi(logisticsOrderVO);
        ValidatorUtil.validateEntity(request);
        try {
            WeiShiCreateOrder weiShiResponse = weiShiService.createOrder(request,logisticsOrderVO.getAuthMap());
            if(isFailure(weiShiResponse.getAsk())){
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(weiShiResponse), false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
            }
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(weiShiResponse), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(weiShiResponse.getOrderCode())
                    .deliveryNo(weiShiResponse.getReferenceNo())
                    .trackNo(weiShiResponse.getShippingMethodNo())
                    .build());
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        for(LogisticsGetLabelVO logisticsGetLabelVO : labelVO){
            WeiShiGetLabelUrlRequest request = WeiShiGetLabelUrlRequest.builder()
                    .referenceNo(logisticsGetLabelVO.getPlatformCode())
                    .build();
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            try {
                ValidatorUtil.validateEntity(request);
                WeiShiGetLabelUrl weiShiGetLabelUrlResponse = weiShiService.getLabelUrl(request,logisticsGetLabelVO.getAuthMap());
                response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
                response.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
                response.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
                if(isFailure(weiShiGetLabelUrlResponse.getAsk())){
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(weiShiGetLabelUrlResponse));
                    response.failure(getPlatForm().getName(),logisticsGetLabelVO.getDeliveryNo(),weiShiGetLabelUrlResponse.getMessage());
                }else{
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(weiShiGetLabelUrlResponse));
                    response.setBase64(FileUtil.convertPdfUrlToBase64(weiShiGetLabelUrlResponse.getUrl()));
                    response.success();
                }
                result.add(response);
            }catch (Exception e){
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(e));
                response.failure(getPlatForm().getName(),logisticsGetLabelVO.getDeliveryNo(),e.getMessage());
                result.add(response);
            }

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
        ValidatorUtil.validateEntity(weiShiCancelOrderRequest);
        try {
            WeiShiResponse<List<WeiShiGetTrackNumber>> weiShiresponse = weiShiService.getTrackNumber(weiShiCancelOrderRequest,logisticsQueryVOList.get(0).getAuthMap());
            if(isFailure(weiShiresponse.getAsk())){
                logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                        null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(weiShiresponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
            }
            List<LogisticsOrderResponseVO> response = LogisticsOrderConverter.INSTANCE.trackInfoConvertByWeiShi(weiShiresponse.getData());
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                    null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(weiShiresponse));
            return success(response);
        }catch (Exception e){
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                    null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        List<InterceptResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsInterceptOrderVO interceptOrderVO : logisticsQueryVO){
            WeiShiInterceptOrderRequest weiShiInterceptOrderRequest = WeiShiInterceptOrderRequest.builder()
                    //客户单号
                    .referenceNo(interceptOrderVO.getPlatformCode())
                    .build();
            InterceptResponseVO interceptResponseVO = LogisticsOperationOrderConverter.INSTANCE.interceptOrderCovert(interceptOrderVO);
            try {
                ValidatorUtil.validateEntity(weiShiInterceptOrderRequest);
                WeiShiResponse<String> weiShiresponse = weiShiService.interceptOrder(weiShiInterceptOrderRequest,interceptOrderVO.getAuthMap());
                if(isFailure(weiShiresponse.getAsk())){
                    logisticsOperateService.pushOperateLog(interceptOrderVO.getOrderId(),
                            interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(weiShiresponse), false);
                    isSuccess = false;
                    interceptResponseVO.failure(getPlatForm().getName(),interceptOrderVO.getDeliveryNo(),weiShiresponse.getError().getErrMessage());
                }else{
                    logisticsOperateService.pushOperateLog(interceptOrderVO.getOrderId(),
                            interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(weiShiresponse), false);
                    interceptResponseVO.success();
                }
            }catch (Exception e){
                logisticsOperateService.pushOperateLog(interceptOrderVO.getOrderId(),
                        interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(e), true);
                isSuccess = false;
                interceptResponseVO.failure(getPlatForm().getName(),interceptOrderVO.getDeliveryNo(),e.getMessage());
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
                    .referenceNo(cancelOrderVO.getPlatformCode())
                    .build();
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            try {
                ValidatorUtil.validateEntity(request);
                WeiShiResponse<String> weiShiResponse = weiShiService.cancelOrder(request,cancelOrderVO.getAuthMap());
                if(isFailure(weiShiResponse.getAsk())){
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(weiShiResponse), false);
                    isSuccess = false;
                    cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),weiShiResponse.getMessage());
                }else{
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(weiShiResponse), false);
                    cancelResponseVO.success();
                }
            }catch (Exception e){
                logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(e), true);
                isSuccess = false;
                cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),e.getMessage());
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult authorization(Map<String, String> authMap){
        try {
            WeiShiResponse<List<WeiShiChannel>> weiShiResponse = weiShiService.getAllChannel(authMap);
            if(isFailure(weiShiResponse.getAsk())) {
                //授权失败
                return failure("授权失败");
            }else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    private Boolean isFailure(String ask){
        return !TmsConstant.SUCCESS.equals(ask);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEI_SHI;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    /**
     * 更新重量
     *
     * @return
     */
    @Override
    public ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO) {
        try {
            WeiShiUpdateWeightRequest weiShiCancelOrderRequest = WeiShiUpdateWeightRequest.builder()
                    .orderCode(logisticsUpdateWeightVO.getPlatformCode())
                    .weight(logisticsUpdateWeightVO.getWeight().divide(new BigDecimal(1000),4, RoundingMode.HALF_UP))
                    .build();
            ValidatorUtil.validateEntity(weiShiCancelOrderRequest);
            List<WeiShiUpdateWeightRequest> weightRequests = Arrays.asList(weiShiCancelOrderRequest);
            WeiShiResponse<String> response = weiShiService.updateWeight(weightRequests, logisticsUpdateWeightVO.getAuthMap());

            if(isFailure(response.getAsk())){
                logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                        logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,response.getMessage());
            }
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
            return success();
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(e),true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
}
