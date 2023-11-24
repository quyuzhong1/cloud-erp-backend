package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.aliexpress.model.channel.response.ChannelResponse;
import com.erp.tms.aliexpress.model.channel.response.ChannelResult;
import com.erp.tms.aliexpress.model.label.request.LabelRequest;
import com.erp.tms.aliexpress.model.label.request.WarehouseOrderQuery;
import com.erp.tms.aliexpress.model.label.response.LabelResponse;
import com.erp.tms.aliexpress.model.label.response.LabelResult;
import com.erp.tms.aliexpress.model.order.request.*;
import com.erp.tms.aliexpress.model.order.response.*;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
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
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    /**
     * 根据平台获取授权列表
     *
     * @param platform
     * @return
     */
    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        ApiResult<List<ShopAuthEntity>> authShops = shopInfoFeign.getAuthShopByPlatformType(getPlatForm().getCode());
        if (!authShops.isSuccess() || CollectionUtils.isEmpty(authShops.getData())) return Collections.emptyList();
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return Collections.emptyList();
        }
        List<Map<String, String>> mapList = new ArrayList<>(authShops.getData().size());
        CfgAppClientEntity finalCfgAppClient = cfgAppClient;
        authShops.getData().forEach(shopAuthEntity -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", finalCfgAppClient.getId());
            map.put("logisticsPlatform", getPlatForm().getCode());
            map.put("clientSecret", finalCfgAppClient.getClientSecret());
            map.put("clientId", finalCfgAppClient.getClientId());
            map.put("url", finalCfgAppClient.getUrl());
            map.put("token", shopAuthEntity.getToken());
            mapList.add(map);
        });
        return mapList;
    }

    /**
     * 速卖通  authId 需要是店铺 shopId
     *
     * @param authId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return Collections.EMPTY_MAP;
        }
        if (Objects.isNull(cfgAppClient)) return Collections.EMPTY_MAP;
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        return map;
    }

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
        OrderResult response = null;
        ValidatorUtil.validateEntity(orderRequest);
        try {
            response = aliExpressShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
            //转换实体
            if (!Objects.isNull(response) && !Objects.isNull(response.getResultSuccess()) && response.getResultSuccess()) {
                OrderResponse orderResponse = response.getResult();
                responseVO.setDeliveryNo(orderResponse.getOutOrderId());
                responseVO.setTransportNo(orderResponse.getIntlTrackingNo());
                responseVO.setTrackNo(orderResponse.getIntlTrackingNo());
                success = true;
                responseVO.success();
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response));
            } else {
                String msg = Objects.nonNull(response.getResult()) ? response.getResult().getErrorDesc() : response.getErrorResponse().getMsg();
                responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), msg);
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response));
            }
        } catch (Exception e) {
            responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), e.getMessage());
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e));
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
        //收寄信息
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setSender(LogisticsOrderConverter.INSTANCE.orderRequestSendUserByAliExpress(logisticsOrderVO));
        addressDTO.setPickup(LogisticsOrderConverter.INSTANCE.orderRequestPickUpUserByAliExpress(logisticsOrderVO));
//        addressDTO.setRefund(addressDTO.getSender());
        addressDTO.setReceiver(LogisticsOrderConverter.INSTANCE.orderRequestReceiverUserByAliExpress(logisticsOrderVO));
        OrderRequest orderRequest = OrderRequest.builder()
                .pickup_type(logisticsOrderVO.getPickupType())
                .declareProducts(declareProducts)
                .domestic_logistics_company(logisticsOrderVO.getLogisticsSaleChannel().getSupplierName())
                .domestic_logistics_company_id(-1L)
//                .domestic_tracking_no(logisticsOrderVO.getDeliveryNo())
                .package_num(logisticsOrderVO.getParceInfoVO().getTotalQuantity())
                .trade_order_from(logisticsOrderVO.getOrderSource())
                .trade_order_id(logisticsOrderVO.getDeliveryNo())
                .undeliverable_decision("0")
                .warehouse_carrier_service(logisticsOrderVO.getLogisticsSaleChannel().getCode())
                //托寄物信息
                .address_d_t_os(addressDTO)
                .is_agree_upgrade_reverse_parcel_insure(false)
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
                    .page_size(20)
                    .trade_order_id(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            BaseResult iopResponse = null;
            ValidatorUtil.validateEntity(queryOrderRequest);
            try {
                iopResponse = aliExpressShipperService.queryLogisticsOrder(logisticsQueryBaseVO.getAuthMap(), queryOrderRequest);
                QueryResponse queryResponse = JSONObject.parseObject(iopResponse.getResult(), QueryResponse.class);
                //失败
                if (Objects.isNull(queryResponse) || Objects.isNull(queryResponse.getSuccess()) || !queryResponse.getSuccess()) {
                    logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
                            logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(iopResponse));
                } else {
                    List<QueryResult> responses = queryResponse.getResultList();
                    if (CollectionUtils.isNotEmpty(responses)) {
                        responses.forEach(queryOrderResponse -> {
                            LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                                    .transportNo(queryOrderResponse.getInternational_logistics_num())
                                    .trackNo(queryOrderResponse.getLogistics_order_id())
                                    .deliveryNo(queryOrderResponse.getTrade_order_id())
                                    .logisticsChannelNo(queryOrderResponse.getLogistics_service_list().get(0).getCode())
                                    .build();
                            logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
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
        LabelResult labelList = null;
        ValidatorUtil.validateEntity(labelRequest);
        try {
            labelList = aliExpressShipperService.getLabelList(logisticsGetLabelVO.getAuthMap(), labelRequest);
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            String result = labelList.getResult();
            LabelResponse labelResponse = JSONObject.parseObject(result, LabelResponse.class);
            //失败
            if (Objects.isNull(labelList.getResult()) || Objects.isNull(labelResponse) || !StringUtils.isBlank(labelResponse.getErrorDesc())) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                response.failure(LogisticsPlatformEnum.ALI_EXPRESS.getName(), "all", labelResponse.getErrorDesc());
                responses.add(response);
                return failure(responses);
            } else {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                //TODO 结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片
                String prefix = "data:application/pdf;base64,";
                response = LogisticsPrintLabelResponse.builder()
                        .transportNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getTransportNo).collect(Collectors.toList()))
                        .base64(prefix + labelResponse.getBody()).build();
                response.success();
                responses.add(response);
                return success(responses);
            }
        } catch (ApiException e) {
            log.error("速卖通getLabelList接口调用失败：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
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
            ChannelResult responseMsg = aliExpressShipperService.getChanelList(chanelQueryVO.getAuthMap());
            //失败
            if (Objects.isNull(responseMsg) || !responseMsg.getResultSuccess()) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return failure(responseMsg.getErrorDesc());
            } else {
                List<ChannelResponse> chanelInfos = responseMsg.getResultList();
                logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return success(LogisticsChannelConverter.INSTANCE.channelConvertByAliExpress(chanelInfos));
            }
        } catch (Exception e) {
            log.error("速卖通getChannel接口调用失败：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }

    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult authorization(Map<String, String> authMap){
        try {
            ChannelResult responseMsg = aliExpressShipperService.getChanelList(authMap);
            if (Objects.isNull(responseMsg) || !responseMsg.getResultSuccess()) {
                //授权失败
                return failure("授权失败");
            }else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.ALI_EXPRESS;
    }
}
