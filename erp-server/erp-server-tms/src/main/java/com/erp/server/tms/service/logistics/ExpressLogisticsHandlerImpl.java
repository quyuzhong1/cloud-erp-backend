package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FileUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.express.model.base.BaseResponse;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.*;
import com.sdk.tms.express.model.order.response.*;
import com.sdk.tms.express.service.ExpressShipperService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName ExpressLogisticsHandlerImpl
 * @description: 顺丰物流接口开发
 * @date 2023年11月10日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SF_EXPRESS)
public class ExpressLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    ExpressShipperService expressShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = processCreateOrderData(logisticsOrderVO);
        boolean success = false;
        BaseResult baseResult = null;
        ValidatorUtil.validateEntity(orderRequest);
        try {
            baseResult = expressShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
            //转换实体
            if (baseResult.isSuccess()) {
                OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
                responseVO.setDeliveryNo(orderResponse.getOrderId());
                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                    if (Objects.nonNull(waybillNoInfo)) {
                        responseVO.setTransportNo(waybillNoInfo.getWaybillNo());
                        responseVO.setTrackNo(waybillNoInfo.getWaybillNo());
                    }
                    List<WaybillNoInfo> collect = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 2).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        responseVO.setMore(true);
                        List<LogisticsOrderResponseVO> vos = new ArrayList<>(collect.size());
                        collect.forEach(waybillNoInfoList2 -> {
                            vos.add(LogisticsOrderResponseVO.builder()
                                    .deliveryNo(orderResponse.getOrderId())
                                    .trackNo(waybillNoInfoList2.getWaybillNo())
                                    .transportNo(waybillNoInfoList2.getWaybillNo()).build());
                        });
                        responseVO.setLogisticsOrderResponseVOS(vos);
                    }
                }
                success = true;
                responseVO.success();
            } else {
                responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), baseResult.getErrorMsg());
            }
        } catch (UnsupportedEncodingException e) {
            responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), e.getMessage());
        }
        if (success) {
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(baseResult), false);
        } else {
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(baseResult), false);
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
        //收寄双方信息
        List<ContactInfo> contactInfoList = new ArrayList<>(2);
        ContactInfo sender = LogisticsOrderConverter.INSTANCE.orderRequestSendUserByExpress(logisticsOrderVO);
        ContactInfo receiver = LogisticsOrderConverter.INSTANCE.orderRequestReceiverUserByExpress(logisticsOrderVO);
        contactInfoList.add(sender);
        contactInfoList.add(receiver);
        //报关信息
        CustomsInfo customsInfo = LogisticsOrderConverter.INSTANCE.orderRequestCustomsInfoByExpress(logisticsOrderVO.getParceInfoVO());
        //托寄物信息
        List<CargoDetail> cargoDetails = LogisticsOrderConverter.INSTANCE.orderRequestCargoDetailByExpress(logisticsOrderVO.getLogisticsProductVOList());
        OrderRequest orderRequest = OrderRequest.builder()
                .language("zh-CN")
                .orderId(logisticsOrderVO.getDeliveryNo())
                //报关信息
                .customsInfo(customsInfo)
                //托寄物信息
                .cargoDetails(cargoDetails)
                .cargoDesc(null)
                //增值服务
                .serviceList(null)
                //收寄双方信息
                .contactInfoList(contactInfoList)
                //顺丰月结卡号 月结支付时传值，现结不需传值；沙箱联调可使用测试月结卡号7551234567（非正式，无须绑定，仅支持联调使用）
                .monthlyCard(logisticsOrderVO.getMonthlyCard())
                .payMethod(1)
                //快件产品类别
                .expressTypeId(Integer.valueOf(logisticsOrderVO.getLogisticsSaleChannel().getCode()))
                .parcelQty(1)
                //是否返回路由标签： 默认1， 1：返回路由标签， 0：不返回；除部分特殊用户外，其余用户都默认返回
                .isReturnRoutelabel(1)
                .build();
        return orderRequest;
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryBaseVOS
     * @return
     */
    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {
        List<ConfirmResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryBaseVOS) {
            ConfirmResponseVO responseVO = new ConfirmResponseVO();
            //支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(logisticsQueryVO.getDeliveryNo())
                    .dealType(1)
                    .build();
            //只支持单个订单取消
            boolean success = false;
            String msg = null;
            BaseResult baseResult = null;
            try {
                ValidatorUtil.validateEntity(orderUpdateRequest);
                baseResult = expressShipperService.updateOrder(logisticsQueryVO.getAuthMap(), orderUpdateRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (2 == orderUpdateResponse.getResStatus()) {
                        success = true;
                        responseVO.success();
                    } else {
                        isSuccess = false;
                        responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                    }
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), e.getMessage());
            }
            if (success) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CONFIRM_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            } else {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CONFIRM_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVOS
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVOS) {
        List<CancelResponseVO> responseVOS = new ArrayList<>(logisticsQueryVOS.size());
        boolean isSuccess = true;
        for (LogisticsCancelOrderVO logisticsQueryVO : logisticsQueryVOS) {
            CancelResponseVO responseVO = new CancelResponseVO();
            boolean success = false;
            BaseResult baseResult = null;
            //支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(logisticsQueryVO.getDeliveryNo())
                    .dealType(2)
                    .build();
            try {
                ValidatorUtil.validateEntity(orderUpdateRequest);
                baseResult = expressShipperService.updateOrder(logisticsQueryVO.getAuthMap(), orderUpdateRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (2 == orderUpdateResponse.getResStatus()) {
                        success = true;
                        responseVO.success();
                    } else {
                        isSuccess = false;
                        responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                    }
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), e.getMessage());
            }
            if (success) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            } else {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }


    /**
     * 取消订单
     *
     * @param logisticsQueryVOS
     * @return
     */
    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVOS) {
        List<InterceptResponseVO> responseVOS = new ArrayList<>(logisticsQueryVOS.size());
        boolean isSuccess = true;
        for (LogisticsInterceptOrderVO logisticsQueryVO : logisticsQueryVOS) {
            InterceptResponseVO responseVO = new InterceptResponseVO();
            boolean success = false;
            BaseResult baseResult = null;
            //支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(logisticsQueryVO.getDeliveryNo())
                    .dealType(2)
                    .build();
            try {
                ValidatorUtil.validateEntity(orderUpdateRequest);
                baseResult = expressShipperService.updateOrder(logisticsQueryVO.getAuthMap(), orderUpdateRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (2 == orderUpdateResponse.getResStatus()) {
                        success = true;
                        responseVO.success();
                    } else {
                        isSuccess = false;
                        responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                    }
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), e.getMessage());
            }
            if (success) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            } else {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult), false);
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }


    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> responseVOS = new ArrayList<>(logisticsQueryVOList.size());
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryVOList) {
            ApiResult<LogisticsOrderResponseVO> result = this.queryOrder(logisticsQueryVO);
            if (!result.isSuccess()) {
                isSuccess = false;
            }
            responseVOS.add(result.getData());
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    private ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        boolean success = false;
        BaseResult baseResult = null;
        LogisticsOrderResponseVO logisticsOrderResponseVO = new LogisticsOrderResponseVO();
        OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                .orderId(logisticsQueryVO.getDeliveryNo())
                .searchType(1)
                .language("zh-CN")
                .build();
        try {
            ValidatorUtil.validateEntity(orderQueryRequest);
            baseResult = expressShipperService.queryOrder(logisticsQueryVO.getAuthMap(), orderQueryRequest);
            if (baseResult.isSuccess()) {
                OrderSearchRespDto orderSearchRespDto = JSONUtil.toBean(baseResult.getMsgData(), OrderSearchRespDto.class);
                logisticsOrderResponseVO.setDeliveryNo(orderSearchRespDto.getOrderId());
                //运单号列表
                List<WaybillNoInfoList> waybillNoInfoList = orderSearchRespDto.getWaybillNoInfoList();
                WaybillNoInfoList waybillNoInfoList1 = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                if (Objects.nonNull(waybillNoInfoList1)) {
                    logisticsOrderResponseVO.setTransportNo(waybillNoInfoList1.getWaybillNo());
                    logisticsOrderResponseVO.setTrackNo(waybillNoInfoList1.getWaybillNo());
                }
                List<WaybillNoInfoList> collect = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 2).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    logisticsOrderResponseVO.setMore(true);
                    List<LogisticsOrderResponseVO> vos = new ArrayList<>(collect.size());
                    collect.forEach(waybillNoInfoList2 -> {
                        vos.add(LogisticsOrderResponseVO.builder().deliveryNo(waybillNoInfoList1.getWaybillNo())
                                .trackNo(waybillNoInfoList2.getWaybillNo())
                                .transportNo(waybillNoInfoList2.getWaybillNo()).build());
                    });
                    logisticsOrderResponseVO.setLogisticsOrderResponseVOS(vos);
                }
                logisticsOrderResponseVO.success();
                success = true;
            } else {
                logisticsOrderResponseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), baseResult.getErrorMsg());
            }
        } catch (UnsupportedEncodingException e) {
            logisticsOrderResponseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), e.getMessage());
            log.error("查询订单异常：{}", e.getMessage());
        }
        if (success) {
            logisticsOperateService.pullOperateLog(logisticsQueryVO.getOrderId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
        } else {
            logisticsOperateService.pullOperateLog(logisticsQueryVO.getOrderId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
        }
        return success ? success(logisticsOrderResponseVO) : failure(logisticsOrderResponseVO);
    }

    /**
     * 获取标签
     *
     * @param logisticsGetLabelVOS
     * @return
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOS) throws IOException {
        List<LogisticsPrintLabelResponse> responseVOS = new ArrayList<>(logisticsGetLabelVOS.size());
        boolean isSuccess = true;
        for (LogisticsGetLabelVO logisticsGetLabelVO : logisticsGetLabelVOS) {
            boolean success = false;
            BaseResult baseResult = null;
            LogisticsPrintLabelResponse responseVO = new LogisticsPrintLabelResponse();
            //支持单个取消
            OrderLabelRequest orderLabelRequest = OrderLabelRequest.builder()
//                    .templateCode("fm_76130_standard_{clientcode}")
                    .templateCode("fm_100_vips_"+ logisticsGetLabelVO.getAuthMap().get("clientId"))
//                    .templateCode("fm_210_standard_"+ logisticsGetLabelVO.getAuthMap().get("clientId"))
                    .documents(Collections.singletonList(Document.builder().masterWaybillNo(logisticsGetLabelVO.getTransportNo()).build()))
                    .version("2.0")
                    .fileType("pdf")
                    .sync(true)
                    .build();
            try {
                ValidatorUtil.validateEntity(orderLabelRequest);
                baseResult = expressShipperService.getLabel(logisticsGetLabelVO.getAuthMap(), orderLabelRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    LabelResponse labelResponse = JSONUtil.toBean(JSONUtil.toJsonStr(baseResult.getObj()), LabelResponse.class);
                    //根据文件列表 下载文件然后转换base64
                    List<PrintFile> files = labelResponse.getFiles();
                    responseVO.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
                    responseVO.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
                    responseVO.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
                    List<LogisticsPrintLabelResponse> logisticsPrintLabelResponses = new ArrayList<>();
                    if (1 == files.size()){
                        responseVO.setMore(false);
                        responseVO.setBase64(FileUtil.convertPdfUrlToBase64(files.get(0).getUrl(), files.get(0).getToken()));
                    }else {
                        files.forEach(printFile -> {
                            responseVO.setMore(true);
                            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                            response.setTrackNoList(Collections.singletonList(printFile.getWaybillNo()));
                            response.setTransportNoList(Collections.singletonList(printFile.getWaybillNo()));
                            response.setDeliveryNoList(Collections.singletonList(printFile.getSeqNo()));
                            try {
                                response.setBase64(FileUtil.convertPdfUrlToBase64(printFile.getUrl(), printFile.getToken()));
                            } catch (IOException e) {
                                log.error("获取标签文件异常：{}", e.getMessage());
                            }
                            logisticsPrintLabelResponses.add(response);
                        });
                    }
                    responseVO.setLogisticsPrintLabelResponses(logisticsPrintLabelResponses);
                    responseVO.success();
                    success = true;
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), baseResult.getErrorMessage());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), e.getMessage());
            }
            if (success) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(baseResult));
            } else {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(baseResult));
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        String waybillNo = "SF1040275268927";
        try {
            BaseResponse baseResponse = expressShipperService.validateWaybillNo(authMap, waybillNo);
            BaseResult baseResult = JSONUtil.toBean(baseResponse.getApiResultData(), BaseResult.class);
            if (StringUtils.isNotEmpty(baseResponse.getApiErrorMsg()) || !baseResult.isSuccess()) {
                //授权失败
                return failure("授权失败" + baseResponse.getApiErrorMsg());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
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
        List<LogisticsSaleChannelEntity> entityList = new ArrayList<>();
        entityList.add(new LogisticsSaleChannelEntity().setCode("1").setPlatformChannelId("1").setAging("T4").setCnName("顺丰特快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("2").setPlatformChannelId("2").setAging("T6").setCnName("顺丰标快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("6").setPlatformChannelId("6").setAging("T104").setCnName("顺丰即日").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("10").setPlatformChannelId("10").setAging("T14").setCnName("国际小包").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("23").setPlatformChannelId("23").setAging("T9").setCnName("顺丰国际特惠(文件)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("24").setPlatformChannelId("24").setAging("T9").setCnName("顺丰国际特惠(包裹)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("26").setPlatformChannelId("26").setAging("T7").setCnName("国际大件").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("60").setPlatformChannelId("60").setAging("T4").setCnName("顺丰特快（文件）").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("99").setPlatformChannelId("99").setAging("T4").setCnName("顺丰国际标快(文件)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("100").setPlatformChannelId("100").setAging("T4").setCnName("顺丰国际标快(包裹)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("242").setPlatformChannelId("242").setAging("T77").setCnName("丰网速运").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("247").setPlatformChannelId("247").setAging("T68").setCnName("电商标快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        return success(entityList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SF_EXPRESS;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
