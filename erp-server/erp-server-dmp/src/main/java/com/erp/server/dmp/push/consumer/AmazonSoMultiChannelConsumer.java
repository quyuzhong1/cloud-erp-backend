package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.enums.CreateStatusEnum;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.CreateFulfillmentOrderRequest;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.CreateFulfillmentOrderResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.push.service.business.KingdeeSoConsumerService;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String jsonStr ="{\"marketplaceId\":\"1730162240754708482\",\"sellerFulfillmentOrderId\":\"WFHD250822000010\",\"destinationAddress\":{\"stateOrRegion\":\"APPS\",\"city\":\"Dubai\",\"phone\":\"\",\"countryCode\":\"AE\",\"postalCode\":\"123123\",\"name\":\"Jorge Paniagua\",\"addressLine1\":\"Jumeirah beach street\",\"addressLine2\":\"Building Marina Wharf 1 Floor 27, Flat 2701 / PH2\",\"addressLine3\":\"\",\"districtOrCounty\":\"ss\"},\"displayableOrderDate\":\"2025-08-22T10:25:41.462Z[UTC]\",\"shippingSpeedCategory\":\"Standard\",\"displayableOrderId\":\"408-4194613-9811562\",\"id\":\"1958837975472435201\",\"shopId\":\"1735509194049589249\",\"items\":[{\"sellerFulfillmentOrderItemId\":\"1753302923623796737\",\"quantity\":1,\"fulfillmentNetworkSku\":\"X0012PS2AH\",\"sellerSku\":\"2961-AU2-FBA\"}],\"displayableOrderComment\":\"WFHD250822000010\",\"fulfillmentPolicy\":\"FillOrKill\"}";
        SoMultiChannelDTO.CreateResultDTO createResultDTO = new SoMultiChannelDTO.CreateResultDTO();
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);

        CreateFulfillmentOrderRequest body = JSONUtil.toBean(jsonObject, CreateFulfillmentOrderRequest.class);

        String id = jsonObject.getStr("id", "");
        createResultDTO.setId(id);
        String shopId = jsonObject.getStr("shopId", "");
        CfgAppClientService cfgAppClientService = SpringUtil.getBean(CfgAppClientService.class);
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        try {
//            CreateFulfillmentOrderRequest body = JSONUtil.toBean(jsonObject, CreateFulfillmentOrderRequest.class);
            CreateFulfillmentOrderResponse response = api.createFulfillmentOrder(body);
            System.out.println(response);
            //成功后，更新任务状态
            createResultDTO.setCreateStatus(CreateStatusEnum.SUCCESS.getCode());
//            omsTaskFeign.updateSoMultiChannel(createResultDTO);
        } catch (ApiException | LWAException e) {
            createResultDTO.setCreateStatus(CreateStatusEnum.FAILED.getCode());
//            omsTaskFeign.updateSoMultiChannel(createResultDTO);
            throw new RuntimeException(e);
        }


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
        try {
            CreateFulfillmentOrderRequest body = JSONUtil.toBean(jsonObject, CreateFulfillmentOrderRequest.class);
            CreateFulfillmentOrderResponse response = api.createFulfillmentOrder(body);
            System.out.println(response);
            //成功后，更新任务状态
            createResultDTO.setCreateStatus(CreateStatusEnum.SUCCESS.getCode());
            omsTaskFeign.updateSoMultiChannel(createResultDTO);
        } catch (ApiException | LWAException e) {
            createResultDTO.setCreateStatus(CreateStatusEnum.FAILED.getCode());
            createResultDTO.setMsg("亚马逊创建订单异常：" + e.getMessage());
            omsTaskFeign.updateSoMultiChannel(createResultDTO);
            throw new ServiceException("亚马逊创建订单异常：" + e.getMessage());
        }
        return ApiResult.success();
    }


}
