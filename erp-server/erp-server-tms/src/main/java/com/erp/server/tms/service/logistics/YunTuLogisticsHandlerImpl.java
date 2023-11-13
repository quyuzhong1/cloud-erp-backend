package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollectionUtil;
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
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import com.sdk.tms.yuntu.server.YunTuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@LogisticsPlatformType(LogisticsPlatformEnum.YUN_TU)
public class YunTuLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private YunTuService yunTuService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        YunTuResponse<List<YunTuChannel>> yunTuResponse =  yunTuService.getAllChannel();
        if(isFailure(yunTuResponse.getCode())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYunTu(yunTuResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        YunTuCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYunTu(logisticsOrderVO);
        YunTuResponse<List<YunTuCreateOrder>> yunTuResponse = yunTuService.createOrder(Collections.singletonList(request));
        if(isFailure(yunTuResponse.getCode())){
            List<YunTuCreateOrder> yunTuCreateOrders = yunTuResponse.getData();
            String remark = "";
            if(CollectionUtil.isNotEmpty(yunTuCreateOrders)){
                remark = yunTuCreateOrders.get(0).getRemark();
            }
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage()+remark);
        }
        YunTuCreateOrder yunTuCreateOrder = yunTuResponse.getData().get(0);
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(yunTuCreateOrder.getWayBillNumber())
                .deliveryNo(yunTuCreateOrder.getCustomerOrderNumber())
                .trackNo(yunTuCreateOrder.getTrackingNumber())
                .build());
    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<String> deliveryList = labelVO.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList());
        YunTuPrintLabelRequest request = YunTuPrintLabelRequest.builder()
                .orderNumbers(deliveryList)
                .build();
        YunTuResponse<List<YunTuPrintLabel>> yunTuResponse = yunTuService.getPrintLabel(request);
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        //云途调取打印标签，可能全部失败，也有可能部分成功，部分失败
        if(isFailure(yunTuResponse.getCode())){
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.failure(getName(),"all",yunTuResponse.getMessage());
            responseList.add(response);
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
                    response.failure(getName(),orderInfo.getCustomerOrderNumber(),orderInfo.getError());
                    responseList.add(response);
                }
            }
        }

        return isSuccess?success(responseList):failure(responseList);
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
        YunTuResponse<List<YunTuTrackingNumber>> yunTuResponse = yunTuService.getTrackingNumber(request);
        List<LogisticsOrderResponseVO> responseList = new ArrayList<>();
        if(isFailure(yunTuResponse.getCode())){
            LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
            response.failure(getName(),String.join(",", deliveryList),yunTuResponse.getMessage());
            responseList.add(response);
            return failure(responseList);
        }
        responseList = LogisticsOrderConverter.INSTANCE.orderQueryByYunTu(yunTuResponse.getData());
        return success(responseList);
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
            YunTuResponse<YunTuInterceptOrder> yunTuResponse = yunTuService.interceptOrder(request);
            InterceptResponseVO interceptResponseVO = LogisticsOperationOrderConverter.INSTANCE.interceptOrderCovert(interceptOrderVO);
            if(isFailure(yunTuResponse.getCode())){
                isSuccess = false;
                interceptResponseVO.failure(getName(),interceptOrderVO.getDeliveryNo(),yunTuResponse.getMessage());
            }else{
                interceptResponseVO.success();
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
            YunTuResponse<YunTuCancelOrder> yunTuResponse = yunTuService.cancelOrder(request);
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            if(isFailure(yunTuResponse.getCode())){
                isSuccess = false;
                cancelResponseVO.failure(getName(),cancelOrderVO.getDeliveryNo(),yunTuResponse.getMessage());
            }else{
                cancelResponseVO.success();
            }
            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);

    }
    private Boolean isFailure(String code){
        return !"0000".equals(code);
    }

    private String getName(){return LogisticsPlatformEnum.YUN_TU.getName();};
}
