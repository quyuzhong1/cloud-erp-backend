package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnums;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.disifang.model.base.ResponseMsg;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.disifang.model.label.request.LabelRequest;
import com.sdk.tms.disifang.model.order.request.*;
import com.sdk.tms.disifang.model.order.response.OrderResponse;
import com.sdk.tms.disifang.model.order.response.QueryOrderResponse;
import com.sdk.tms.disifang.model.product.request.ChanelRequest;
import com.sdk.tms.disifang.service.DsfShipperService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zdy
 * @ClassName DsfLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月02日
 * @version: 1.0
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.SDF)
public class DsfLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private DsfShipperService dsfShipperService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    private List<Parcel> getParcel(LogisticsOrderVO logisticsOrderVO) {
        if (CollectionUtils.isEmpty(logisticsOrderVO.getLogisticsProductVOList())) {
            return Collections.emptyList();
        }
        List<Parcel> parcelList = new ArrayList<>(logisticsOrderVO.getLogisticsProductVOList().size());
        logisticsOrderVO.getLogisticsProductVOList().forEach(logisticsProductVO -> {
            Parcel parcel = new Parcel();
            parcel.setWeight(logisticsProductVO.getWeight());
            parcel.setParcel_value(logisticsProductVO.getDeclarePrice());
            parcel.setCurrency(logisticsProductVO.getDeclareCurrency());
            if (logisticsProductVO.isElectric()) {
                parcel.setInclude_battery("Y");
            } else {
                parcel.setInclude_battery("N");
            }
            //海关申报信息
            DeclareProductInfo productInfo = LogisticsOrderConverter.INSTANCE.dsfProductMapping(logisticsProductVO);
            parcel.setDeclare_product_info(Collections.singletonList(productInfo));
            parcelList.add(parcel);
        });
        return parcelList;
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        ApiResult apiResult = new ApiResult();
        OrderRequest orderRequest = LogisticsOrderConverter.INSTANCE.orderRequestToDsf(logisticsOrderVO);
        //包裹信息封装
        orderRequest.setParcelList(getParcel(logisticsOrderVO));
        //保险信息封装 暂时不做 默认为N
        orderRequest.setIs_insure("N");
        ResponseMsg responseMsg = dsfShipperService.createOrder(logisticsOrderVO.getLogisticsAuthEntity().getAccount(),
                logisticsOrderVO.getLogisticsAuthEntity().getPassword(), orderRequest);
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            apiResult.setMsg(responseMsg.getMsg());
            apiResult.setCode(-1);
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnums.CREATE_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg));
        } else {
            apiResult.setCode(200);
            OrderResponse orderResponse = JSONUtil.toBean(JSONUtil.parseObj(responseMsg.getData()), OrderResponse.class);
            apiResult.setData(LogisticsOrderResponseVO.builder()
                    .transportNo(orderResponse.getRef_no())
                    .trackNo(orderResponse.getTracking_no())
                    .transportNo(orderResponse.getDs_consignment_no())
                    .logisticsChannelNo(orderResponse.getLogistics_channel_no())
                    .odaResultSign(orderResponse.getOda_result_sign())
                    .build());
            apiResult.setMsg(responseMsg.getMsg());
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnums.CREATE_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg));
        }

        return apiResult;
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO logisticsQueryVO) {
        OrderCancelRequest orderCancelRequest = OrderCancelRequest.builder()
                .request_no(logisticsQueryVO.getTransportNo().get(0))
                .cancel_reason(logisticsQueryVO.getReason())
                .build();
        ResponseMsg responseMsg = dsfShipperService.cancelOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderCancelRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return success(responseMsg.getMsg());
        }
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrder(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> responseVOS = new ArrayList<>();
        logisticsQueryVOList.forEach(logisticsQueryBaseVO -> {
            ApiResult<LogisticsOrderResponseVO> responseMsg = this.queryOrder(logisticsQueryBaseVO);
            if (responseMsg.isSuccess()) {
                responseVOS.add(responseMsg.getData());
            }
        });
        return success(responseVOS);
    }

    /**
     * 查询订单
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                .request_no(logisticsQueryVO.getTransportNo().get(0))
                .build();
        ResponseMsg responseMsg = dsfShipperService.queryOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderQueryRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.QUERY_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            QueryOrderResponse queryOrderResponse = JSONUtil.toBean(JSONUtil.toJsonStr(responseMsg.getData()), QueryOrderResponse.class);
            LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                    .transportNo(queryOrderResponse.getConsignmentInfo().getRef_no())
                    .trackNo(queryOrderResponse.getConsignmentInfo().getTracking_no())
                    .transportNo(queryOrderResponse.getConsignmentInfo().getDs_consignment_no())
                    .logisticsChannelNo(queryOrderResponse.getConsignmentInfo().getLogistics_channel_no())
                    .odaResultSign(queryOrderResponse.getConsignmentInfo().getOda_result_sign())
                    .build();
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.QUERY_ORDER.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return success(orderResponseVO);
        }
    }

    /**
     * 获取标签
     * request_no 请求单号（支持4PX单号、客户单号和面单号
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult getLabelList(LogisticsGetLabelVO logisticsQueryVO) {
        LabelRequest labelRequest = LabelRequest.builder()
                .requestNo(logisticsQueryVO.getTransportNo())
                .logisticsProductCode(logisticsQueryVO.getLogisticsChannelEntity().getCode())
                .build();
        ResponseMsg responseMsg = dsfShipperService.getLabelList(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                logisticsQueryVO.getLogisticsAuthEntity().getPassword(), labelRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.GET_LABEL_LIST.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.GET_LABEL_LIST.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            //TODO 结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片
            return success(responseMsg.getData());
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
        ChanelRequest chanelRequest = ChanelRequest.builder()
                .transport_mode("1")
                .build();
        ResponseMsg responseMsg = dsfShipperService.getChanelList(chanelQueryVO.getLogisticsAuthEntity().getAccount(),
                chanelQueryVO.getLogisticsAuthEntity().getPassword(), chanelRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnums.GET_CHANEL_LIST.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            List<ChanelInfo> chanelInfos = JSONUtil.toList(JSONUtil.toJsonStr(responseMsg.getData()), ChanelInfo.class);
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnums.GET_CHANEL_LIST.getCode(), PlatformDictEnum.SDF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
            return success(LogisticsChannelConverter.INSTANCE.channelConvertByDSFList(chanelInfos));
        }
    }
}
