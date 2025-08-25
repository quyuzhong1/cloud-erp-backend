package com.erp.server.dmp.push.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
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
    private KingdeeSoConsumerService kingdeeSoConsumerService;

    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private CfgAppClientService cfgAppClientService;

    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(StrUtil.format("FBillNo in ({})", "'XSD24053100007'"));
        String filterStr = String.join(" and ", queryFilters);

        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FCustId.FNumber,FSaleDeptId.FName,FSalerId.FName,FSalerId.FNumber,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1.FNumber,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode," +
                "FDeliveryDate";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1, 0);
        System.out.println(queryList);


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
        Map<String, Object> map = JSONUtil.parseObj(ext);
        String shopId = map.getOrDefault("shopId", "").toString();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
        try {
            CreateFulfillmentOrderResponse response = api.createFulfillmentOrder(null);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } catch (LWAException e) {
            throw new RuntimeException(e);
        }

//        // 获取店铺信息
//            String shopId = map.get("shopId").toString();
//            // 获取店铺授权信息
//            AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
//            if (null == shopInfoDTO) {
//                throw new ServiceException("未找到店铺授权:" + shopId);
//            }
//            AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
//
//            try {
//                FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
//                String queryType = AmazonFbaQueryTypeEnum.DATE_RANGE.getCode();
//                String marketplaceId = marketPlaceEnum.getMarketplaceId();
//                List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
//                List<String> shipmentIdList = null;
//                String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getLastTime()).toString();
////            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(LocalDateTime.of(2023, 11, 1, 0, 0, 0)).toString();
//                String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
//                String nextToken = null;
//                InboundShipmentList responseList = api.getAllShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, lastUpdatedAfter, lastUpdatedBefore, nextToken);
//                // 返回下载源数据
//                responseList.stream()
//                        .map(e -> new PlatformAmazonFbaShipmentDTO(e, shopInfoDTO.getId(), shopInfoDTO.getName()))
//                        .collect(Collectors.toList());
//            } catch (Exception e) {
//                throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
//            }
//




        kingdeeSoConsumerService.executeConsumer(map);
        return ApiResult.success();
    }


}
