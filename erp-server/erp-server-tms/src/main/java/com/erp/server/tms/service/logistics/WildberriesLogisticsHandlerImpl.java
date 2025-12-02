package com.erp.server.tms.service.logistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.wildberries.dto.OrderLabelRequest;
import com.sdk.oms.wildberries.dto.OrderLabelResponse;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 艾姆勒物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WILDBERRIES)
public class WildberriesLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private WildberriesSDKService wildberriesSDKService;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    /**
     * 查询店铺授权
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        Map<String, String> map = new HashMap<>();
        if (CharSequenceUtil.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                map.put("token", shopAuth.getAccessToken());
            }
        }
        return map;
    }
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.error(-1, "功能未开放");
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
        List<Long> orderIdList = logisticsQueryVO.stream().map(LogisticsGetLabelVO::getPlatformCode).map(Long::parseLong).distinct().collect(Collectors.toList());
        OrderLabelRequest request = OrderLabelRequest.builder()
                .height(40)
                .width(58)
                .type("png")
                .orders(orderIdList)
                .build();
        LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
        try {
            OrderLabelResponse orderLabelResponse = wildberriesSDKService.getOrderLabel(logisticsGetLabelVO.getAuthMap().get("token"), request);
            //失败
            if (CollUtil.isEmpty(orderLabelResponse.getStickers())) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderLabelResponse));
                response.failure(LogisticsPlatformEnum.WILDBERRIES.getName(), orderIdList.stream().map(String::valueOf).collect(Collectors.joining(",")), orderLabelResponse.getMessage());
                responses.add(response);
                return failure(response.getMessage(),responses);
            } else {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.WILDBERRIES.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderLabelResponse));
                FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                        .base64(PdfUtil.ImageToPdfBase64(orderLabelResponse.getStickers().get(0).getFile()))
                        .fileName(logisticsGetLabelVO.getDeliveryNo() + ".pdf")
                        .build();
                String url = fileFeign.uploadFileByBase64(uploadBase64);
                response = LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
                        .labelUrl(url).build();
                response.success();
                responses.add(response);
                return success(responses);
            }
        } catch (Exception e) {
            log.error(" wildberries获取getLabelList接口异常：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.WILDBERRIES.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e));
            response.failure(LogisticsPlatformEnum.WILDBERRIES.getName(), orderIdList.stream().map(String::valueOf).collect(Collectors.joining(",")), e.getMessage());
            responses.add(response);
            return failure(responses);
        }

    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList)  {
        return ApiResult.success();
    }
    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        String token = authMap.get("token");
        if (CharSequenceUtil.isBlank(token)){
            throw new ServiceException("授权信息不能为空");
        }
        return success("授权成功");
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.WILDBERRIES;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
