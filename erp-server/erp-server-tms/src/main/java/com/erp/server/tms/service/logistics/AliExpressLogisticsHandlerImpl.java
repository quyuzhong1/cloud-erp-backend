package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
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
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.channel.response.ChannelResponse;
import com.erp.tms.aliexpress.model.label.request.LabelRequest;
import com.erp.tms.aliexpress.model.label.request.WarehouseOrderQuery;
import com.erp.tms.aliexpress.model.order.request.DeclareProduct;
import com.erp.tms.aliexpress.model.order.request.OrderRequest;
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.OrderResponse;
import com.erp.tms.aliexpress.model.order.response.QueryResult;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = processCreateOrderData(logisticsOrderVO);
        boolean success = false;
        IopResponse response = null;
        try {
            response = aliExpressShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
            //转换实体
            if (!StringUtils.isBlank(response.getCode()) && Objects.equals("0", response.getCode())) {
                OrderResponse orderResponse = JSONUtil.toBean(response.getBody(), OrderResponse.class);
                responseVO.setDeliveryNo(orderResponse.getTrade_order_id());
                responseVO.setTransportNo(orderResponse.getIntl_tracking_no());
                responseVO.setTrackNo(orderResponse.getIntl_tracking_no());
                success = true;
                responseVO.success();
                logisticsOrderOperateLogService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response));
            } else {
                responseVO.failure(getPlatForm().getName(), response.getCode(), response.getMessage());
                logisticsOrderOperateLogService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response));
            }
        } catch (InterruptedException | ApiException e) {
            throw new RuntimeException(e);
        }
        return success ? success(responseVO) : failure(responseVO);
    }

    /**
     * 订单数据整理
     *
     * @param logisticsOrderVO
     * @return
     */
    private OrderRequest processCreateOrderData(LogisticsOrderVO logisticsOrderVO) {
        //申报产品信息
        List<DeclareProduct> declareProducts = LogisticsOrderConverter.INSTANCE.orderRequestProductByAliExpress(logisticsOrderVO.getLogisticsProductVOList());

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
        OrderRequest orderRequest = OrderRequest.builder()
                .declareProducts(declareProducts)
                .trade_order_id(logisticsOrderVO.getDeliveryNo())
                .warehouse_carrier_service(logisticsOrderVO.getLogisticsSaleChannel().getCode())
                //托寄物信息
//                .address_d_t_os(cargoDetails)
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
                .build();
        return orderRequest;
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> list = new ArrayList<>();
        logisticsQueryVOList.forEach(logisticsQueryBaseVO -> {
            QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                    .current_page(1)
                    .trade_order_id(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            IopResponse iopResponse = null;
            try {
                iopResponse = aliExpressShipperService.queryLogisticsOrder(logisticsQueryBaseVO.getAuthMap(), queryOrderRequest);
                //失败
                if (!StringUtils.isBlank(iopResponse.getCode()) && !Objects.equals("0", iopResponse.getCode())) {
                    logisticsOrderOperateLogService.pushOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
                            logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(iopResponse));
                } else {
                    List<QueryResult> responses = JSONObject.parseArray(iopResponse.getBody(), QueryResult.class);
                    if (CollectionUtils.isNotEmpty(responses)) {
                        responses.forEach(queryOrderResponse -> {
                            LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                                    .transportNo(queryOrderResponse.getInternational_logistics_num())
                                    .trackNo(queryOrderResponse.getLogistics_order_id())
                                    .deliveryNo(queryOrderResponse.getTrade_order_id())
                                    .logisticsChannelNo(queryOrderResponse.getLogistics_service_list().get(0).getCode())
                                    .build();
                            logisticsOrderOperateLogService.pushOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
                                    logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responses));
                            list.add(orderResponseVO);
                        });
                    }
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

        });
        return success(list);
    }

    /**
     * 获取标签
     * request_no 请求单号（支持4PX单号、客户单号和面单号
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        List<WarehouseOrderQuery> warehouseOrderQueries = new ArrayList<>(logisticsQueryVO.size());
        logisticsQueryVO.stream().forEach(logisticsGetLabelVO1 -> {
            WarehouseOrderQuery warehouseOrderQuery = new WarehouseOrderQuery();
            warehouseOrderQuery.setInternational_logistics_id(logisticsGetLabelVO1.getTransportNo());
            warehouseOrderQueries.add(warehouseOrderQuery);
        });
        LabelRequest labelRequest = LabelRequest.builder()
                .print_detail(false)
                .warehouseOrderQueries(warehouseOrderQueries)
                .build();
        IopResponse labelList = null;
        try {
            labelList = aliExpressShipperService.getLabelList(logisticsGetLabelVO.getAuthMap(), labelRequest);
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            //失败
            if (StringUtils.isBlank(labelList.getCode()) || !Objects.equals("0", labelList.getCode())) {
                logisticsOrderOperateLogService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                response.failure(LogisticsPlatformEnum.ALI_EXPRESS.getName(), labelList.getCode(), labelList.getMessage());
                responses.add(response);
                return failure(responses);
            } else {
                logisticsOrderOperateLogService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                //TODO 结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片

                response = LogisticsPrintLabelResponse.builder()
                        .transportNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getTransportNo).collect(Collectors.toList()))
                        .base64(labelList.getBody()).build();
                response.success();
                responses.add(response);
                return success(responses);
            }
        } catch (ApiException e) {
            log.error("速卖通getLabelList接口调用失败：{}", e.getMessage());
            logisticsOrderOperateLogService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                    logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
            return failure(e.getMessage());
        }
    }


    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            IopResponse responseMsg = aliExpressShipperService.getChanelList(chanelQueryVO.getAuthMap());
            //失败
            if (StringUtils.isBlank(responseMsg.getCode()) || !Objects.equals("0", responseMsg.getCode())) {
                logisticsOrderOperateLogService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return failure(responseMsg.getMessage());
            } else {
                List<ChannelResponse> chanelInfos = JSONObject.parseArray(responseMsg.getBody(), ChannelResponse.class);
                logisticsOrderOperateLogService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return success(LogisticsChannelConverter.INSTANCE.channelConvertByAliExpress(chanelInfos));
            }
        } catch (Exception e) {
            log.error("速卖通getChannel接口调用失败：{}", e.getMessage());
            logisticsOrderOperateLogService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }

    }
}
