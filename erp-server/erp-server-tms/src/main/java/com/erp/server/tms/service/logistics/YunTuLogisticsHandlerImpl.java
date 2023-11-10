package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.annotation.PlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
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
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsLabelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.weishi.dto.request.WeiShiInterceptOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiResponse;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import com.sdk.tms.yuntu.server.YunTuService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage());
        }
        YunTuCreateOrder yunTuCreateOrder = yunTuResponse.getData().get(0);
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(yunTuCreateOrder.getWayBillNumber())
                .deliveryNo(yunTuCreateOrder.getCustomerOrderNumber())
                .trackNo(yunTuCreateOrder.getTrackingNumber())
                .build());
    }


//    @Override
//    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO labelVO) throws IOException {
//        YunTuPrintLabelRequest request = YunTuPrintLabelRequest.builder()
//                .orderNumbers(labelVO.getDeliveryNo())
//                .build();
//        YunTuResponse<List<YunTuPrintLabel>> yunTuResponse = yunTuService.getPrintLabel(request);
//        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
//        //云途调取打印标签，可能全部失败，也有可能部分成功，部分失败
//        if(isFailure(yunTuResponse.getCode())){
//            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
//            response.failure(getName(),"all",yunTuResponse.getMessage());
//            responseList.add(response);
//            return failure(responseList);
//        }
//        boolean isSuccess = true;
//        List<YunTuPrintLabel> yunTuPrintLabels = yunTuResponse.getData();
//        for(YunTuPrintLabel yunTuPrintLabel : yunTuPrintLabels){
//            //成功的订单
//            List<String> successList = yunTuPrintLabel.getOrderInfos().stream().filter(v->v.getCode().equals(100)).map(YunTuPrintLabel.OrderInfo::getCustomerOrderNumber).collect(Collectors.toList());
//            if(CollectionUtil.isNotEmpty(successList)){
//                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
//                response.setBase64(FileUtil.convertPdfUrlToBase64(yunTuPrintLabel.getUrl()));
//                response.setDeliveryNoList(successList);
//                response.success();
//                responseList.add(response);
//            }
//            //失败的订单
//            List<YunTuPrintLabel.OrderInfo> failureList = yunTuPrintLabel.getOrderInfos().stream().filter(v->!v.getCode().equals(100)).collect(Collectors.toList());
//            if(CollectionUtil.isNotEmpty(failureList)){
//                isSuccess = false;
//                for(YunTuPrintLabel.OrderInfo orderInfo : failureList){
//                    LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
//                    response.setDeliveryNoList(Collections.singletonList(orderInfo.getCustomerOrderNumber()));
//                    response.failure(getName(),orderInfo.getCustomerOrderNumber(),orderInfo.getError());
//                    responseList.add(response);
//                }
//            }
//        }

//        return isSuccess?success(responseList):failure(responseList);
//    }

//    /**
//     * 查询订单(批量)
//     *
//     * @param logisticsQueryVOList
//     * @return
//     */
//    @Override
//    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList) {
//        YunTuGetTrackingNumRequest request = YunTuGetTrackingNumRequest.builder()
//                .customerOrderNumber(String.join(",", logisticsQueryVOList.getDeliveryNo()))
//                .build();
//        YunTuResponse<List<YunTuTrackingNumber>> yunTuResponse = yunTuService.getTrackingNumber(request);
//        List<LogisticsOrderResponseVO> responseList = new ArrayList<>();
//        if(isFailure(yunTuResponse.getCode())){
//            LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
//            response.failure(getName(),String.join(",", logisticsQueryVOList.getDeliveryNo()),yunTuResponse.getMessage());
//            responseList.add(response);
//            return failure(responseList);
//        }
//        responseList = LogisticsOrderConverter.INSTANCE.orderQueryByYunTu(yunTuResponse.getData());
//        return success(responseList);
//    }

//    @Override
//    public ApiResult<List<InterceptResponseVO>> interceptOrder(LogisticsInterceptOrderVO logisticsQueryVO) {
//        List<InterceptResponseVO> result = new ArrayList<>();
//        //客户单号
//        List<String> deliveryNoList = logisticsQueryVO.getDeliveryNo();
//        boolean isSuccess = true;
//        for(String deliveryNo : deliveryNoList){
//            YunTuInterceptOrderRequest request = YunTuInterceptOrderRequest.builder()
//                    .remark(StringUtils.isBlank(logisticsQueryVO.getInterceptReason())?"客户要求拦截":logisticsQueryVO.getInterceptReason())
//                    //传客户单号
//                    .orderType(2)
//                    .orderNumber(deliveryNo)
//                    .build();
//            YunTuResponse<YunTuInterceptOrder> yunTuResponse = yunTuService.interceptOrder(request);
//            InterceptResponseVO interceptResponseVO = new InterceptResponseVO();
//            interceptResponseVO.setDeliveryNo(deliveryNo);
//            if(isFailure(yunTuResponse.getCode())){
//                isSuccess = false;
//                interceptResponseVO.failure(getName(),deliveryNo,yunTuResponse.getMessage());
//            }else{
//                interceptResponseVO.success();
//            }
//            result.add(interceptResponseVO);
//        }
//        return isSuccess?success(result):failure(result);
//    }

//    @Override
//    public ApiResult<List<CancelResponseVO>> cancelOrder(LogisticsCancelOrderVO cancelOrderVO) {
//        List<CancelResponseVO> result = new ArrayList<>();
//        //客户单号
//        List<String> deliveryNoList = cancelOrderVO.getDeliveryNo();
//        boolean isSuccess = true;
//        for(String deliveryNo : deliveryNoList){
//            YunTuCancelOrderRequest request = YunTuCancelOrderRequest.builder()
//                    //传客户单号
//                    .orderType(2)
//                    .orderNumber(deliveryNo)
//                    .build();
//            YunTuResponse<YunTuCancelOrder> yunTuResponse = yunTuService.cancelOrder(request);
//            CancelResponseVO cancelResponseVO = new CancelResponseVO();
//            cancelResponseVO.setDeliveryNo(deliveryNo);
//            if(isFailure(yunTuResponse.getCode())){
//                isSuccess = false;
//                cancelResponseVO.failure(getName(),deliveryNo,yunTuResponse.getMessage());
//            }else{
//                cancelResponseVO.success();
//            }
//            result.add(cancelResponseVO);
//        }
//        return isSuccess?success(result):failure(result);
//
//    }
    private Boolean isFailure(String code){
        return !"0000".equals(code);
    }

    private String getName(){return LogisticsPlatformEnum.YUN_TU.getName();};
}
