package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollectionUtil;
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
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import com.sdk.tms.yuntu.server.YunTuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@LogisticsPlatformType(LogisticsPlatformEnum.YUN_TU)
public class YunTuLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private YunTuService yunTuService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            YunTuResponse<List<YunTuChannel>> yunTuResponse =  yunTuService.getAllChannel(chanelQueryVO.getAuthMap());
            if(isFailure(yunTuResponse.getCode())){
                logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yunTuResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage());
            }
            List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYunTu(yunTuResponse.getData());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yunTuResponse));
            return success(response);
        }catch (Exception e){
            log.error("云途渠道接口异常：{}",e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }

    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        YunTuCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYunTu(logisticsOrderVO);
        ValidatorUtil.validateEntity(request);
        try {
            YunTuResponse<List<YunTuCreateOrder>> yunTuResponse = yunTuService.createOrder(Collections.singletonList(request),logisticsOrderVO.getAuthMap());
            if(isFailure(yunTuResponse.getCode())){
                List<YunTuCreateOrder> yunTuCreateOrders = yunTuResponse.getData();
                String remark = "";
                if(CollectionUtil.isNotEmpty(yunTuCreateOrders)){
                    remark = yunTuCreateOrders.get(0).getRemark();
                }
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yunTuResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage()+remark);
            }
            YunTuCreateOrder yunTuCreateOrder = yunTuResponse.getData().get(0);
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yunTuResponse));
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(yunTuCreateOrder.getWayBillNumber())
                    .deliveryNo(yunTuCreateOrder.getCustomerOrderNumber())
                    .trackNo(yunTuCreateOrder.getTrackingNumber())
                    .build());
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,e.getMessage());
        }

    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<String> deliveryList = labelVO.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList());
        YunTuPrintLabelRequest request = YunTuPrintLabelRequest.builder()
                .orderNumbers(deliveryList)
                .build();
        ValidatorUtil.validateEntity(request);
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        try {
            YunTuResponse<List<YunTuPrintLabel>> yunTuResponse = yunTuService.getPrintLabel(request,labelVO.get(0).getAuthMap());
            //云途调取打印标签，可能全部失败，也有可能部分成功，部分失败
            if(isFailure(yunTuResponse.getCode())){
                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.failure(getPlatForm().getName(),"all",yunTuResponse.getMessage());
                responseList.add(response);
                logisticsOperateService.pullOperateLog(labelVO.get(0).getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(labelVO), JSONUtil.toJsonStr(yunTuResponse));
                return failure(responseList);
            }
            boolean isSuccess = true;
            List<YunTuPrintLabel> yunTuPrintLabels = yunTuResponse.getData();
            for(YunTuPrintLabel yunTuPrintLabel : yunTuPrintLabels){
                //成功的订单
                List<String> successList = yunTuPrintLabel.getOrderInfos().stream().filter(v->v.getCode().equals(100)).map(YunTuPrintLabel.OrderInfo::getCustomerOrderNumber).collect(Collectors.toList());
                if(CollectionUtil.isNotEmpty(successList)){
                    LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                    response.setBase64(FileUtil.convertPdfUrlToBase64(yunTuPrintLabel.getUrl()));
                    response.setDeliveryNoList(successList);
                    response.success();
                    responseList.add(response);
                }
                //失败的订单
                List<YunTuPrintLabel.OrderInfo> failureList = yunTuPrintLabel.getOrderInfos().stream().filter(v->!v.getCode().equals(100)).collect(Collectors.toList());
                if(CollectionUtil.isNotEmpty(failureList)){
                    isSuccess = false;
                    for(YunTuPrintLabel.OrderInfo orderInfo : failureList){
                        LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                        response.setDeliveryNoList(Collections.singletonList(orderInfo.getCustomerOrderNumber()));
                        response.failure(getPlatForm().getName(),orderInfo.getCustomerOrderNumber(),orderInfo.getError());
                        responseList.add(response);
                    }
                }
            }
            logisticsOperateService.pullOperateLog(labelVO.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(labelVO), JSONUtil.toJsonStr(yunTuResponse));
            return isSuccess?success(responseList):failure(responseList);
        }catch (Exception e){
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.failure(getPlatForm().getName(),"all",e.getMessage());
            responseList.add(response);
            logisticsOperateService.pullOperateLog(labelVO.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(labelVO), JSONUtil.toJsonStr(e));
            return failure(responseList);
        }

    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<String> deliveryList = logisticsQueryVOList.stream().map(LogisticsQueryBaseVO :: getDeliveryNo).collect(Collectors.toList());
        YunTuGetTrackingNumRequest request = YunTuGetTrackingNumRequest.builder()
                .customerOrderNumber(String.join(",", deliveryList))
                .build();
        ValidatorUtil.validateEntity(request);
        List<LogisticsOrderResponseVO> responseList = new ArrayList<>();
        try {
            YunTuResponse<List<YunTuTrackingNumber>> yunTuResponse = yunTuService.getTrackingNumber(request,logisticsQueryVOList.get(0).getAuthMap());
            if(isFailure(yunTuResponse.getCode())){
                LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
                response.failure(getPlatForm().getName(),String.join(",", deliveryList),yunTuResponse.getMessage());
                responseList.add(response);
                logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                        UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yunTuResponse));
                return failure(responseList);
            }
            responseList = LogisticsOrderConverter.INSTANCE.orderQueryByYunTu(yunTuResponse.getData());
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yunTuResponse));
            return success(responseList);
        }catch (Exception e){
            LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
            response.failure(getPlatForm().getName(),String.join(",", deliveryList),e.getMessage());
            responseList.add(response);
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
            return failure(responseList);
        }

    }

    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVOList) {
        List<InterceptResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsInterceptOrderVO interceptOrderVO : logisticsQueryVOList){
            YunTuInterceptOrderRequest request = YunTuInterceptOrderRequest.builder()
                    .remark(StringUtils.isBlank(interceptOrderVO.getInterceptReason())?"客户要求拦截":interceptOrderVO.getInterceptReason())
                    //传客户单号
                    .orderType(2)
                    .orderNumber(interceptOrderVO.getDeliveryNo())
                    .build();
            InterceptResponseVO interceptResponseVO = LogisticsOperationOrderConverter.INSTANCE.interceptOrderCovert(interceptOrderVO);
            try {
                ValidatorUtil.validateEntity(request);
                YunTuResponse<YunTuInterceptOrder> yunTuResponse = yunTuService.interceptOrder(request,interceptOrderVO.getAuthMap());
                if(isFailure(yunTuResponse.getCode())){
                    isSuccess = false;
                    interceptResponseVO.failure(getPlatForm().getName(),interceptOrderVO.getDeliveryNo(),yunTuResponse.getMessage());
                    logisticsOperateService.pushOperateLog(interceptOrderVO.getAuthMap().get("id"),
                            interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(yunTuResponse));
                }else{
                    interceptResponseVO.success();
                    logisticsOperateService.pushOperateLog(interceptOrderVO.getAuthMap().get("id"),
                            interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(yunTuResponse));
                }
            }catch (Exception e){
                isSuccess = false;
                interceptResponseVO.failure(getPlatForm().getName(),interceptOrderVO.getDeliveryNo(),e.getMessage());
                logisticsOperateService.pushOperateLog(interceptOrderVO.getAuthMap().get("id"),
                        interceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(interceptOrderVO), JSONUtil.toJsonStr(e));
            }

            result.add(interceptResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList) {
        List<CancelResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsCancelOrderVO cancelOrderVO : cancelOrderVOList){
            YunTuCancelOrderRequest request = YunTuCancelOrderRequest.builder()
                    //传客户单号
                    .orderType(2)
                    .orderNumber(cancelOrderVO.getDeliveryNo())
                    .build();
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            try {
                ValidatorUtil.validateEntity(request);
                YunTuResponse<YunTuCancelOrder> yunTuResponse = yunTuService.cancelOrder(request,cancelOrderVO.getAuthMap());
                if(isFailure(yunTuResponse.getCode())){
                    isSuccess = false;
                    cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),yunTuResponse.getMessage());
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getAuthMap().get("id"),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yunTuResponse));
                }else{
                    cancelResponseVO.success();
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getAuthMap().get("id"),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yunTuResponse));
                }
            }catch (Exception e){
                isSuccess = false;
                cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),e.getMessage());
                logisticsOperateService.pushOperateLog(cancelOrderVO.getAuthMap().get("id"),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YUN_TU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(e));
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);

    }
    private Boolean isFailure(String code){
        return !"0000".equals(code);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.YUN_TU;
    }
}
