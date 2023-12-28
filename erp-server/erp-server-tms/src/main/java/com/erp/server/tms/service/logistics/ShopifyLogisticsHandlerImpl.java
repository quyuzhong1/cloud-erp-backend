package com.erp.server.tms.service.logistics;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.ConfirmResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentServicesItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentServicesRoot;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.sdk.tms.express.model.order.request.OrderRequest;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jim
 * @ClassName ShopifyLogisticsHandlerImpl
 * @description: 亚马逊物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SHOPIFY)
public class ShopifyLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private ShopifyRestClientService shopifyRestClientService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    /**
     * 创建订单
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return null;
    }

    /**
     * 订单数据整理
     */
    private OrderRequest processCreateOrderData(LogisticsOrderVO logisticsOrderVO) {
        return null;
    }

    /**
     * 确认订单
     */
    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {
        return null;
    }

    /**
     * 取消订单
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVOS) {
        return null;
    }

    /**
     * 查询订单(批量)
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return null;
    }


    /**
     * 获取标签
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOS) throws IOException {
        return null;
    }

    /**
     * 授权判断
     */
    @Override
    public ApiResult<?> authorization(Map<String, String> authMap) {
        return null;
    }

    /**
     * 渠道查询
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        String shopId = authMap.get("shopId");
        ShopifyShopInfoDTO shopInfoDTO = ShopSdkServer.getTokenAndDomainByShopId(shopId);
        if (null == shopInfoDTO) {
            log.error("[Shopify渠道下载]从缓存中获取shopify token 失败: shopId={}", shopId);
            throw new ServiceException();
        }
        String shopifyShopDomain = shopInfoDTO.getShopDomain();
        String accessToken = shopInfoDTO.getAccessToken();

        try {
            ShopifyFulfillmentServicesRoot fulfillmentServicesResp = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                    .getFulfillmentServices();
            List<ShopifyFulfillmentServicesItem> fulfillmentServices = fulfillmentServicesResp.getFulfillmentServices();

            //接口数据映射
            List<LogisticsSaleChannelEntity> logisticsSaleChannelEntities = LogisticsChannelConverter.INSTANCE.channelConvertByShopify(fulfillmentServices);
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("shopId"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPIFY.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(fulfillmentServices));
            return success(logisticsSaleChannelEntities);
        } catch (Exception e) {
            log.error("Shopify渠道服务接口调用异常：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("shopId"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.SHOPIFY.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SHOPIFY;
    }
}
