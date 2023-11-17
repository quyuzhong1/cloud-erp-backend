package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.tms.disifang.model.base.ResponseMsg;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.disifang.model.label.request.LabelRequest;
import com.sdk.tms.disifang.model.order.response.QueryOrderResponse;
import com.sdk.tms.disifang.model.product.request.ChanelRequest;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.*;
import com.sdk.tms.express.model.order.response.OrderResponse;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName AliExpressLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.ALI_EXPRESS)
public class AliExpressLogisticsHandlerImpl extends AbstractLogisticsHandler {
//
//    /**
//     * 创建订单
//     *
//     * @param logisticsOrderVO
//     * @return
//     */
//    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
//        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
//        OrderRequest orderRequest = processCreateOrderData(logisticsOrderVO);
//        boolean success = false;
//        BaseResult baseResult = null;
//        try {
//            baseResult = expressShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
//            //转换实体
//            if (baseResult.isSuccess()) {
//                OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
//                responseVO.setDeliveryNo(orderResponse.getOrderId());
//                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
//                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
//                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
//                    if (Objects.nonNull(waybillNoInfo)) {
//                        responseVO.setTransportNo(waybillNoInfo.getWaybillNo());
//                        responseVO.setTrackNo(waybillNoInfo.getWaybillNo());
//                    }
//                    List<WaybillNoInfo> collect = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 2).collect(Collectors.toList());
//                    if (CollectionUtils.isNotEmpty(collect)) {
//                        responseVO.setMore(true);
//                        List<LogisticsOrderResponseVO> vos = new ArrayList<>(collect.size());
//                        collect.forEach(waybillNoInfoList2 -> {
//                            vos.add(LogisticsOrderResponseVO.builder().deliveryNo(orderResponse.getOrderId())
//                                    .trackNo(waybillNoInfoList2.getWaybillNo())
//                                    .transportNo(waybillNoInfoList2.getWaybillNo()).build());
//                        });
//                        responseVO.setLogisticsOrderResponseVOS(vos);
//                    }
//                }
//                success = true;
//                responseVO.success();
//            } else {
//                responseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
//            }
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//        if (success) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
//                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(baseResult));
//        } else {
//            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
//                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(baseResult));
//        }
//        return success ? success(responseVO) : failure(responseVO);
//    }
//
//    /**
//     * 订单数据整理
//     *
//     * @param logisticsOrderVO
//     * @return
//     */
//    private OrderRequest processCreateOrderData(LogisticsOrderVO logisticsOrderVO) {
//        //收寄双方信息
//        List<ContactInfo> contactInfoList = new ArrayList<>(2);
//        ContactInfo sender = LogisticsOrderConverter.INSTANCE.orderRequestSendUserByExpress(logisticsOrderVO);
//        ContactInfo receiver = LogisticsOrderConverter.INSTANCE.orderRequestReceiverUserByExpress(logisticsOrderVO);
//        contactInfoList.add(sender);
//        contactInfoList.add(receiver);
//        //报关信息
//        CustomsInfo customsInfo = LogisticsOrderConverter.INSTANCE.orderRequestCustomsInfoByExpress(logisticsOrderVO.getParceInfoVO());
//        //托寄物信息
//        List<CargoDetail> cargoDetails = LogisticsOrderConverter.INSTANCE.orderRequestCargoDetailByExpress(logisticsOrderVO.getLogisticsProductVOList());
//        OrderRequest orderRequest = OrderRequest.builder()
//                .language("zh-CN")
//                .orderId(logisticsOrderVO.getDeliveryNo())
//                //报关信息
//                .customsInfo(customsInfo)
//                //托寄物信息
//                .cargoDetails(cargoDetails)
//                .cargoDesc(null)
//                //增值服务
//                .serviceList(null)
//                //收寄双方信息
//                .contactInfoList(contactInfoList)
//                //顺丰月结卡号 月结支付时传值，现结不需传值；沙箱联调可使用测试月结卡号7551234567（非正式，无须绑定，仅支持联调使用）
//                .monthlyCard("7551234567")
//                .payMethod(1)
//                //快件产品类别
//                .expressTypeId(1)
//                .parcelQty(1)
//                //是否返回路由标签： 默认1， 1：返回路由标签， 0：不返回；除部分特殊用户外，其余用户都默认返回
//                .isReturnRoutelabel(1)
//                .build();
//        return orderRequest;
//    }
//    /**
//     * 查询订单(批量)
//     *
//     * @param logisticsQueryVOList
//     * @return
//     */
//    @Override
//    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
//        List<LogisticsOrderResponseVO> list = new ArrayList<>();
//        logisticsQueryVOList.forEach(logisticsQueryBaseVO -> {
//            com.sdk.tms.disifang.model.order.request.OrderQueryRequest orderQueryRequest = com.sdk.tms.disifang.model.order.request.OrderQueryRequest.builder()
//                    .request_no(logisticsQueryBaseVO.getDeliveryNo())
//                    .build();
//            ResponseMsg responseMsg = dsfShipperService.queryOrder(logisticsQueryBaseVO.getAuthMap(), orderQueryRequest);
//            //失败
//            if (!StringUtils.isBlank(responseMsg.getResult()) && Objects.equals("1", responseMsg.getResult())) {
////                List<QueryOrderResponse> responses = JSONUtil.toList(JSONUtil.parseArray(responseMsg.getData()), QueryOrderResponse.class);
//                List<QueryOrderResponse> responses = JSONObject.parseArray(responseMsg.getData().toString(), QueryOrderResponse.class);
//                if (CollectionUtils.isNotEmpty(responses)){
//                    responses.forEach(queryOrderResponse ->{
//                        LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
//                                .transportNo(queryOrderResponse.getConsignmentInfo().getRef_no())
//                                .trackNo(queryOrderResponse.getConsignmentInfo().getTrackingNo())
//                                .transportNo(queryOrderResponse.getConsignmentInfo().getDs_consignment_no())
//                                .logisticsChannelNo(queryOrderResponse.getConsignmentInfo().getLogistics_channel_no())
//                                .odaResultSign(queryOrderResponse.getConsignmentInfo().getOda_result_sign())
//                                .build();
//                        logisticsOrderOperateLogService.addOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
//                                logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
//                                RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responseMsg));
//                        list.add(orderResponseVO);
//                    });
//                }
//            }
//        });
//        return success(list);
//    }
//
//    /**
//     * 获取标签
//     * request_no 请求单号（支持4PX单号、客户单号和面单号
//     *
//     * @param logisticsQueryVO
//     * @return
//     */
//    @Override
//    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
//        List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
//        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
//        assert logisticsGetLabelVO != null;
//        request labelRequest = request.builder()
//                .requestNo(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
//                .logisticsProductCode(logisticsGetLabelVO.getLogisticsSaleChannelEntity().getCode())
//                .build();
//        ResponseMsg responseMsg = dsfShipperService.getLabelList(logisticsGetLabelVO.getAuthMap(), labelRequest);
//        LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
//        //失败
//        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
//                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
//            response.failure(LogisticsPlatformEnum.DSF.getName(), responseMsg.getResult(), responseMsg.getMsg());
//            responses.add(response);
//            return failure(responses);
//        } else {
//            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
//                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
//            //TODO 结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片
//
//            response = LogisticsPrintLabelResponse.builder()
//                    .deliveryNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
//                    .base64(FileUtil.convertPdfUrlToBase64((String) responseMsg.getData())).build();
//            response.success();
//            responses.add(response);
//            return success(responses);
//        }
//    }
//
//    /**
//     * 渠道查询
//     *
//     * @param chanelQueryVO
//     * @return
//     */
//    @Override
//    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
//        ChanelRequest chanelRequest = ChanelRequest.builder()
//                .transport_mode("1")
//                .build();
//        ResponseMsg responseMsg = dsfShipperService.getChanelList(chanelQueryVO.getAuthMap(), chanelRequest);
//        //失败
//        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
//            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
//                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
//            return failure(responseMsg.getMsg());
//        } else {
//            List<ChanelInfo> chanelInfos = JSONObject.parseArray(responseMsg.getData().toString(), ChanelInfo.class);
//            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
//                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
//            return success(LogisticsChannelConverter.INSTANCE.channelConvertByDSF(chanelInfos));
//        }
//    }
}
