package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.*;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 燕文物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.YAN_WEN)
public class YanWenLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;
    @Resource
    private YanWenService yanWenService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        YanWenResponse<List<YanWenChannel>> yanWenResponse =  yanWenService.getAllChannel(chanelQueryVO.getAuthMap());
        if(!yanWenResponse.getSuccess()){
            logisticsOrderOperateLogService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yanWenResponse));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYanWenList(yanWenResponse.getData());
        logisticsOrderOperateLogService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yanWenResponse));
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        YanWenCreateWayBillRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYanWen(logisticsOrderVO);
        YanWenResponse<YanWenCreateWayBill> yanWenResponse = yanWenService.createWayBill(request,logisticsOrderVO.getAuthMap());
        if(!yanWenResponse.getSuccess()){
            logisticsOrderOperateLogService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yanWenResponse));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
        logisticsOrderOperateLogService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yanWenResponse));
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(yanWenResponse.getData().getWaybillNumber())
                .deliveryNo(yanWenResponse.getData().getOrderNumber())
                .trackNo(yanWenResponse.getData().getWaybillNumber())
                .build());
    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        for(LogisticsGetLabelVO logisticsGetLabelVO : labelVO){
            YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
                    .waybillNumber(logisticsGetLabelVO.getTransportNo())
                    .printRemark(logisticsGetLabelVO.getPrintRemark())
                    .build();
            YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request,logisticsGetLabelVO.getAuthMap());
            if(!labelResponse.getSuccess()){
                logisticsOrderOperateLogService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(labelResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,logisticsGetLabelVO.getDeliveryNo()+labelResponse.getMessage());
            }
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setBase64(labelResponse.getData().getBase64String());
            response.setTransportNoList(Collections.singletonList(labelResponse.getData().getWaybillNumber()));
            response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
            result.add(response);
            logisticsOrderOperateLogService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(labelResponse));
        }
        return success(result);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList) {
        List<CancelResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsCancelOrderVO cancelOrderVO : cancelOrderVOList){
            YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
                    .note(cancelOrderVO.getReason())
                    .waybillNumber(cancelOrderVO.getTransportNo())
                    .build();
            YanWenResponse<String> yanWenResponse = yanWenService.cancelOrder(request,cancelOrderVO.getAuthMap());
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            if(!yanWenResponse.getSuccess()){
                isSuccess = false;
                cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),yanWenResponse.getMessage());
                logisticsOrderOperateLogService.pushOperateLog(cancelOrderVO.getAuthMap().get("id"),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yanWenResponse));
            }else{
                cancelResponseVO.success();
                logisticsOrderOperateLogService.pushOperateLog(cancelOrderVO.getAuthMap().get("id"),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yanWenResponse));
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList){
        List<String> deliveryList = logisticsQueryVOList.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList());
        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
                .listNumber(deliveryList)
                .build();
        YanWenResponse<List<YanWenQueryOrder>> yanWenResponse = yanWenService.queryOrder(request,logisticsQueryVOList.get(0).getAuthMap());
        if(!yanWenResponse.getSuccess()){
            logisticsOrderOperateLogService.pushOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                    UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yanWenResponse));
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
        List<LogisticsOrderResponseVO> list = LogisticsOrderConverter.INSTANCE.orderQueryByYanWen(yanWenResponse.getData());
        logisticsOrderOperateLogService.pushOperateLog(logisticsQueryVOList.get(0).getAuthMap().get("id"),
                UUID.randomUUID().toString(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yanWenResponse));
        return success(list);
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.YAN_WEN;
    }
}
