package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
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
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.weishi.dto.request.*;
import com.sdk.tms.weishi.dto.response.*;
import com.sdk.tms.weishi.server.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
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
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;
    @Resource
    private WeiShiService weiShiService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        WeiShiResponse<List<WeiShiChannel>> weiShiResponse =  weiShiService.getAllChannel(chanelQueryVO.getAuthMap());
        if(isFailure(weiShiResponse.getAsk())){
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(weiShiResponse));

            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByWeiShi(weiShiResponse.getData());
        logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(weiShiResponse));
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        WeiShiCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByWeiShi(logisticsOrderVO);
        WeiShiCreateOrder weiShiResponse = weiShiService.createOrder(request,logisticsOrderVO.getAuthMap());
        if(isFailure(weiShiResponse.getAsk())){
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(weiShiResponse));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiResponse.getError().getErrMessage());
        }
        logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(weiShiResponse));
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
            WeiShiGetLabelUrl weiShiGetLabelUrlResponse = weiShiService.getLabelUrl(request,logisticsGetLabelVO.getAuthMap());
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
            response.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
            response.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
            if(isFailure(weiShiGetLabelUrlResponse.getAsk())){
                logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(weiShiGetLabelUrlResponse));
                response.failure(getPlatForm().getName(),logisticsGetLabelVO.getDeliveryNo(),weiShiGetLabelUrlResponse.getMessage());
            }else{
                logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(weiShiGetLabelUrlResponse));
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
        WeiShiResponse<List<WeiShiGetTrackNumber>> weiShiresponse = weiShiService.getTrackNumber(weiShiCancelOrderRequest,logisticsQueryVOList.get(0).getAuthMap());
        if(isFailure(weiShiresponse.getAsk())){
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(weiShiresponse));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
        }
        List<LogisticsOrderResponseVO> response = LogisticsOrderConverter.INSTANCE.trackInfoConvertByWeiShi(weiShiresponse.getData());
        logisticsOrderOperateLogService.addOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(weiShiresponse));
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
            WeiShiResponse<String> weiShiresponse = weiShiService.interceptOrder(weiShiInterceptOrderRequest,interceptOrderVO.getAuthMap());
            InterceptResponseVO interceptResponseVO = LogisticsOperationOrderConverter.INSTANCE.interceptOrderCovert(interceptOrderVO);
            if(isFailure(weiShiresponse.getAsk())){
                logisticsOrderOperateLogService.addOperateLog(interceptOrderVO.getAuthMap().get("id"),
                        interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(weiShiresponse));
                isSuccess = false;
                interceptResponseVO.failure(getPlatForm().getName(),interceptOrderVO.getDeliveryNo(),weiShiresponse.getError().getErrMessage());
            }else{
                logisticsOrderOperateLogService.addOperateLog(interceptOrderVO.getAuthMap().get("id"),
                        interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(weiShiresponse));
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
            WeiShiResponse<String> weiShiResponse = weiShiService.cancelOrder(request,cancelOrderVO.getAuthMap());
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            if(isFailure(weiShiResponse.getAsk())){
                logisticsOrderOperateLogService.addOperateLog(cancelOrderVO.getAuthMap().get("id"),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(weiShiResponse));
                isSuccess = false;
                cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),weiShiResponse.getMessage());
            }else{
                logisticsOrderOperateLogService.addOperateLog(cancelOrderVO.getAuthMap().get("id"),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.WEI_SHI.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(weiShiResponse));
                cancelResponseVO.success();
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }
    private Boolean isFailure(String ask){
        return !TmsConstant.SUCCESS.equals(ask);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WEI_SHI;
    }
}
