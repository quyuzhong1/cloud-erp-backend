package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
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
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.*;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 燕文物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.YAN_WEN)
public class YanWenLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private YanWenService yanWenService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        YanWenResponse<List<YanWenChannel>> yanWenResponse =  yanWenService.getAllChannel(chanelQueryVO.getLogisticsAuthEntity());
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYanWenList(yanWenResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        YanWenCreateWayBillRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYanWen(logisticsOrderVO);
        YanWenResponse<YanWenCreateWayBill> yanWenResponse = yanWenService.createWayBill(request,logisticsOrderVO.getLogisticsAuthEntity());
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
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
            YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request,logisticsGetLabelVO.getLogisticsAuthEntity());
            if(!labelResponse.getSuccess()){
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,logisticsGetLabelVO.getDeliveryNo()+labelResponse.getMessage());
            }
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setBase64(labelResponse.getData().getBase64String());
            response.setTransportNoList(Collections.singletonList(labelResponse.getData().getWaybillNumber()));
            response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
            result.add(response);
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
            YanWenResponse<String> yanWenResponse = yanWenService.cancelOrder(request,cancelOrderVO.getLogisticsAuthEntity());
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            if(!yanWenResponse.getSuccess()){
                isSuccess = false;
                cancelResponseVO.failure(getName(),cancelOrderVO.getDeliveryNo(),yanWenResponse.getMessage());
            }else{
                cancelResponseVO.success();
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
        YanWenResponse<List<YanWenQueryOrder>> yanWenResponse = yanWenService.queryOrder(request,logisticsQueryVOList.get(0).getLogisticsAuthEntity());
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
        }
        List<LogisticsOrderResponseVO> list = LogisticsOrderConverter.INSTANCE.orderQueryByYanWen(yanWenResponse.getData());
        return success(list);
    }

    private String getName(){return getPlatForm().getName();}
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.YAN_WEN;
    }
}
