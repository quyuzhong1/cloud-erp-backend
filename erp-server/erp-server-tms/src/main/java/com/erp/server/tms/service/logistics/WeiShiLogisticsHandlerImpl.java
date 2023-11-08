package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.constant.TmsConstant;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.weishi.dto.request.WeiShiCreateOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.weishi.dto.response.WeiShiCreateOrder;
import com.sdk.tms.weishi.dto.response.WeiShiResponse;
import com.sdk.tms.weishi.server.WeiShiService;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.*;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


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
    //    @Override
//    public ApiResult<String> getLabelUrl(LogisticsGetLabelVO labelVO) {
//        YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
//                .waybillNumber(labelVO.getTransportNo())
//                .printRemark(labelVO.getPrintRemark())
//                .build();
//        YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request);
//        if(!labelResponse.getSuccess()){
//            return ApiResult.error(ApiError.ERROR_500.code,labelResponse.getMessage());
//        }
//        return success(labelResponse.getData().getBase64String());
//    }
    @Override
    public ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        return null;
    }
//
//    @Override
//    public ApiResult<String> getLabelUrl(LogisticsGetLabelVO labelVO) {
//        YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
//                .waybillNumber(labelVO.getTransportNo())
//                .printRemark(labelVO.getPrintRemark())
//                .build();
//        YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request);
//        if(!labelResponse.getSuccess()){
//            return ApiResult.error(ApiError.ERROR_500.code,labelResponse.getMessage());
//        }
//        return success(labelResponse.getData().getBase64String());
//    }
//
//    @Override
//    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO cancelOrderVO) {
//        YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
//                .waybillNumber(cancelOrderVO.getTransportNo())
//                .note(cancelOrderVO.getReason())
//                .build();
//        YanWenResponse<String> yanWenResponse =  yanWenService.cancelOrder(request);
//        if(!yanWenResponse.getSuccess()){
//            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
//        }
//        return success();
//    }
//
//    @Override
//    public ApiResult<List<LogisticsOrderResponseVO>> queryOrder(List<LogisticsQueryBaseVO> logisticsQueryVOList){
//        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
//                .listNumber(logisticsQueryVOList.stream().map(LogisticsQueryBaseVO :: getDeliveryNo).collect(Collectors.toList()))
//                .build();
//        YanWenResponse<List<YanWenQueryOrder>> yanWenResponse = yanWenService.queryOrder(request);
//        if(!yanWenResponse.getSuccess()){
//            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
//        }
//        List<LogisticsOrderResponseVO> list = LogisticsOrderConverter.INSTANCE.orderQueryByYanWen(yanWenResponse.getData());
//        return success(list);
//    }

    private Boolean isSuccess(String ask){
        return TmsConstant.SUCCESS.equals(ask);
    }
    private Boolean isFailure(String ask){
        return !TmsConstant.SUCCESS.equals(ask);
    }
}
