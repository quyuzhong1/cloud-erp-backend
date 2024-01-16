package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.PaperSizeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.batong.model.label.base.BaseData;
import com.erp.tms.batong.model.label.request.AdditionalInfo;
import com.erp.tms.batong.model.label.request.ConfigInfo;
import com.erp.tms.batong.model.label.request.LabelRequest;
import com.erp.tms.batong.model.label.request.ListOrder;
import com.erp.tms.batong.model.label.response.LabelResponse;
import com.erp.tms.batong.model.order.request.OrderRequest;
import com.erp.tms.batong.model.order.response.TrackBase;
import com.erp.tms.batong.service.BaTongService;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Lambda
 * @Classname BtLogisticsHandlerImpl
 * @Description 巴通物流商对接
 * @Date 2024-01-09 15:07
 * @Created by yl
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BaTong)
public class BaTongLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private BaTongService baTongService;

    @Resource
    private LogisticsOperateService logisticsOperateService;


    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrder) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = new OrderRequest();


        // OrderRequest.builder().
        // referenceNo(logisticsOrder.getDeliveryNo()).
        // shippingMethod(logisticsOrder.getLogisticsChannelEntity().getCode()).
        //


        return null;

    }

    /**
     * 获取物流原始渠道信息
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            List<BaseData> baseList = baTongService.listShippingMethod(chanelQueryVO.getAuthMap());
            List<LogisticsSaleChannelEntity> list = new ArrayList<>(baseList.size());
            if (CollectionUtils.isEmpty(baseList)) {
                return success(list);
            }
            list = LogisticsChannelConverter.INSTANCE.channelConvertByBaTong(baseList);
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseList));
            return success(list);
        } catch (Exception e) {
            log.error("获取物流原始渠道信息异常 {}", e.getMessage());
        }

        return success(Collections.emptyList());
    }


    /**
     * 取消订单
     *
     * @param logisticsCancelOrderList
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderList) {
        LogisticsCancelOrderVO logisticsCancelOrderVO = logisticsCancelOrderList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        Map<String, String> authMap = logisticsCancelOrderVO.getAuthMap();
        if (Objects.isNull(authMap)) {
            return failure("缺少授权信息");
        }
        Boolean isSuccess = true;
        List<CancelResponseVO> responseList = new ArrayList<>(logisticsCancelOrderList.size());
        for (LogisticsCancelOrderVO item : logisticsCancelOrderList) {
            try {
                CancelResponseVO responseVO = new CancelResponseVO();
                Boolean result = baTongService.deleteOrder(authMap, item.getDeliveryNo());
                if (result) {
                    responseVO.setDeliveryNo(item.getDeliveryNo());
                }
                responseList.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pushOperateLog(item.getAuthMap().get("id"),
                        item.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(e));
                isSuccess = false;
            }

        }
        return isSuccess ? success(responseList) : failure(responseList);

    }


    /**
     * 获取标签
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-16 14:13
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryList) {
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;

        List<LogisticsPrintLabelResponse> resultList = new ArrayList<>(logisticsQueryList.size());
        for (LogisticsGetLabelVO item : logisticsQueryList) {
            try {
                Map<String, String> authMap = item.getAuthMap();
                String deliveryNo = item.getDeliveryNo();
                //是否打印配货单
                Boolean isPrintPacking = "Y".equalsIgnoreCase(item.getIsPdn());
                //是否打印报关单
                Boolean isPrintDeclare = "Y".equalsIgnoreCase(logisticsGetLabelVO.getIsPcd());

                ConfigInfo configInfo = new ConfigInfo();
                //pdf
                configInfo.setLabelFileType("2");
                String labelType = item.getLabelType();
                String a4Code = PaperSizeEnum.A4.getCode();
                String labelPaperType = "1";
                if (a4Code.equals(labelType)) {
                    labelPaperType = "2";
                }
                configInfo.setLabelPaperType(labelPaperType);
                //标签内容类型
                String labelContentType = getLabelContentType(isPrintPacking, isPrintDeclare);
                configInfo.setLabelContentType(labelContentType);
                AdditionalInfo additionalInfo = new AdditionalInfo();
                //是否打印配货信息
                additionalInfo.setLabelPrintInvoiceInfo(item.getIsPdn());
                additionalInfo.setLabelPrintBuyerid("N");
                additionalInfo.setLabelPrintDatetime("Y");
                additionalInfo.setCustomsDeclarationPrintActualWeight("N");
                ListOrder listOrder = ListOrder.builder().referenceNo(deliveryNo).build();
                LabelRequest labelRequest = LabelRequest.builder().
                        configInfo(configInfo).
                        orderList(Arrays.asList(listOrder)).build();
                LabelResponse labelResponse = baTongService.getLabel(authMap, labelRequest);
                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.setDeliveryNoList(Collections.singletonList(deliveryNo));
                response.setTrackNoList(Collections.singletonList(item.getTrackNo()));
                response.setTransportNoList(Collections.singletonList(item.getTransportNo()));
                response.setBase64(labelResponse.getBase64());
                resultList.add(response);
            } catch (Exception e) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(e));
                return failure(getPlatForm().getName() + ":" + e.getMessage());
            }

        }

        return success(resultList);
    }


    /**
     * 获取订单列表
     *
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-16 18:09
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> resultList = new ArrayList<>(logisticsQueryVOList.size());
        for (LogisticsQueryBaseVO item : logisticsQueryVOList) {
            Map<String, String> authMap = item.getAuthMap();
            ListOrder listOrder = ListOrder.builder().
                    referenceNo(item.getDeliveryNo()).
                    build();
            try {
                TrackBase trackBase = baTongService.getTrack(authMap, listOrder);
                LogisticsOrderResponseVO responseVO = LogisticsOrderResponseVO.builder().
                        transportNo(trackBase.getShippingMethodNo()).
                        trackNo(trackBase.getShippingMethodNo()).build();
                resultList.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pullOperateLog(item.getAuthMap().get("id"),
                        item.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
                return failure(getPlatForm().getName() + ":" + e.getMessage());
            }

        }
        return success(resultList);

    }

    /**
     * 标签内容类型代码
     * 1：标签
     * 2：报关单
     * 3：配货单
     * 4：标签+报关单
     * 5：标签+配货单
     * 6：标签+报关单+配货单
     *
     * @param isPrintPacking 是否打印配货单
     * @param isPrintDeclare 是否打印报关单
     * @return
     */
    private String getLabelContentType(Boolean isPrintPacking, Boolean isPrintDeclare) {
        if (isPrintPacking && isPrintDeclare) {
            return "6";
        }
        if (isPrintPacking) {
            return "3";
        }
        if (isPrintDeclare) {
            return "2";
        }
        return "1";

    }
}
