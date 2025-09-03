package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.enums.CreateStatusEnum;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.CreateFulfillmentOrderRequest;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.CreateFulfillmentOrderResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/20 11:12
 */
@Service
@Slf4j
public class AmazonSoMultiChannelConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {

    @Resource
    private OmsTaskFeign omsTaskFeign;
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private CfgAppClientService cfgAppClientService;

    public static void main(String[] args) {
    }

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        SoMultiChannelDTO.CreateResultDTO createResultDTO = new SoMultiChannelDTO.CreateResultDTO();
        JSONObject jsonObject = JSONUtil.parseObj(ext);
        String id = jsonObject.getStr("id", "");
        createResultDTO.setId(id);
        String shopId = jsonObject.getStr("shopId", "");
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        try {
            CreateFulfillmentOrderRequest body = JSONUtil.toBean(jsonObject, CreateFulfillmentOrderRequest.class);
            body.setMarketplaceId(marketplaceEnum.getMarketplaceId());
            ApiResponse<CreateFulfillmentOrderResponse> fulfillmentOrderWithHttpInfo = api.createFulfillmentOrderWithHttpInfo(body);
            log.warn("创建订单响应：{}", JSONUtil.toJsonStr(fulfillmentOrderWithHttpInfo));
            //成功后，更新任务状态
            createResultDTO.setCreateStatus(CreateStatusEnum.SUCCESS.getCode());
            omsTaskFeign.updateSoMultiChannel(createResultDTO);
        } catch (ApiException e) {
            createResultDTO.setCreateStatus(CreateStatusEnum.FAILED.getCode());
            createResultDTO.setMsg("亚马逊创建订单异常：" + JSONUtil.toJsonStr(e.getResponseBody()));
            omsTaskFeign.updateSoMultiChannel(createResultDTO);
            throw new ServiceException("亚马逊创建订单异常：" + JSONUtil.toJsonStr(e.getResponseBody()));
        } catch (LWAException e) {
            createResultDTO.setCreateStatus(CreateStatusEnum.FAILED.getCode());
            createResultDTO.setMsg("亚马逊创建订单异常：" + JSONUtil.toJsonStr(e.getErrorMessage()));
            omsTaskFeign.updateSoMultiChannel(createResultDTO);
            throw new ServiceException("亚马逊创建订单异常：" + JSONUtil.toJsonStr(e.getErrorMessage()));
        }
        return ApiResult.success();
    }


}
