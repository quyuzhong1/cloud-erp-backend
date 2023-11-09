package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsLabelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.yuntu.dto.request.YunTuCreateOrderRequest;
import com.sdk.tms.yuntu.dto.request.YunTuPrintLabelRequest;
import com.sdk.tms.yuntu.dto.response.YunTuChannel;
import com.sdk.tms.yuntu.dto.response.YunTuCreateOrder;
import com.sdk.tms.yuntu.dto.response.YunTuPrintLabel;
import com.sdk.tms.yuntu.dto.response.YunTuResponse;
import com.sdk.tms.yuntu.server.YunTuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.YUN_TU)
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


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO labelVO) throws IOException {
        YunTuPrintLabelRequest request = YunTuPrintLabelRequest.builder()
                .orderNumbers(labelVO.getDeliveryNo())
                .build();
        YunTuResponse<List<YunTuPrintLabel>> yunTuResponse = yunTuService.getPrintLabel(request);
        if(isFailure(yunTuResponse.getCode())){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yunTuResponse.getMessage());
        }
        List<YunTuPrintLabel> yunTuPrintLabels = yunTuResponse.getData();
        for(YunTuPrintLabel yunTuPrintLabel : yunTuPrintLabels){
            yunTuPrintLabel.setBase64(FileUtil.convertPdfUrlToBase64(yunTuPrintLabel.getUrl()));
            yunTuPrintLabel.setOrderNumber(yunTuPrintLabel.getOrderInfos().get(0).getCustomerOrderNumber());
        }
        List<LogisticsPrintLabelResponse> responses = LogisticsLabelConverter.INSTANCE.labelConvertByYuTu(yunTuPrintLabels);
        return success(responses);
    }
//
//    /**
//     * 查询订单(批量)
//     *
//     * @param logisticsQueryVOList
//     * @return
//     */
//    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList) {
//        WeiShiGetTrackNumberRequest weiShiCancelOrderRequest = WeiShiGetTrackNumberRequest.builder()
//                .referenceNoList(logisticsQueryVOList.getDeliveryNo())
//                .build()
//                ;
//        WeiShiResponse<List<WeiShiGetTrackNumber>> weiShiresponse = weiShiService.getTrackNumber(weiShiCancelOrderRequest);
//        if(isFailure(weiShiresponse.getAsk())){
//            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
//        }
//        List<LogisticsOrderResponseVO> response = LogisticsOrderConverterImpl.INSTANCE.trackInfoConvertByWeiShi(weiShiresponse.getData());
//        return success(response);
//    }
//
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
//
//    @Override
//    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO cancelOrderVO) {
//        WeiShiCancelOrderRequest weiShiInterceptOrderRequest = WeiShiCancelOrderRequest.builder()
//                .referenceNo(cancelOrderVO.getDeliveryNo().get(0))
//                .build();
//        WeiShiResponse<String> weiShiresponse = weiShiService.cancelOrder(weiShiInterceptOrderRequest);
//        if(isFailure(weiShiresponse.getAsk())){
//            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,weiShiresponse.getError().getErrMessage());
//        }
//        return success(weiShiresponse.getData());
//    }
    private Boolean isFailure(String code){
        return !"0000".equals(code);
    }
}
