package com.erp.sdk.oms.amz.spapi.handler;

import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractFbaShipmentHandler;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.*;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊FBA货件处理器
 *
 * @author Jim
 * @since 2023-11-01
 **/
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.AMAZON)
@BusinessType(BusinessTypeEnum.FBA_SHIPMENT)
public class AmazonFbaShipmentHandler extends AbstractFbaShipmentHandler<PlatformAmazonFbaShipmentDTO, PlatformFbaShipmentDTO> {

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformAmazonFbaShipmentDTO> download(JobTaskDTO data) {
        // 获取店铺信息
        String shopId = data.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + shopId);
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        try {
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            String queryType = AmazonFbaQueryTypeEnum.DATE_RANGE.getCode();
            String marketplaceId = marketPlaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            List<String> shipmentIdList = null;
            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getLastTime()).toString();
//            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(LocalDateTime.of(2023, 11, 1, 0, 0, 0)).toString();
            String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
            String nextToken = null;
            InboundShipmentList responseList = api.getAllShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, lastUpdatedAfter, lastUpdatedBefore, nextToken);
            // 返回下载源数据
            return responseList.stream()
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shopInfoDTO.getId(), shopInfoDTO.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
        }
    }


    @Override
    public List<PlatformFbaShipmentDTO> convert(List<PlatformAmazonFbaShipmentDTO> sourceDataList) {
        // 亚马逊FBA货件转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDtoList(sourceDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }

    /**
     * 是否发送MQ
     * true=发送
     * false=不发送（有其他详情需要额外拉取）
     */
    public Boolean getIsSendMq() {
        return Boolean.FALSE;
    }


    @Override
    public PlatformAmazonFbaShipmentDTO downloadDetail(PlatformAmazonFbaShipmentDTO dto, JSONObject extendObj) {
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(dto.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        try {
            // 查询FBA货件item
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            InboundShipmentInfo shipmentInfo = dto.getShipmentInfo();
            if (null == shipmentInfo){
               throw new ServiceException("[Amazon SP-APi] 数据异常查询FBA货件主信息结果为空：" + dto.getUniqueId());
            }
            GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(dto.getShipmentInfo().getShipmentId(), marketPlaceEnum.getMarketplaceId());
            InboundShipmentItemList itemData = response.getPayload().getItemData();
            dto.setDetailList(itemData);
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 查询FBA货件item失败" + e);
        }
        return dto;
    }

    public List<PlatformAmazonFbaShipmentDTO> checkAndDownloadMainInfo(List<PlatformAmazonFbaShipmentDTO> currentDTOList, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketPlaceEnum) {
        List<String> shipmentIdList = currentDTOList.stream()
                .filter(e-> null == e.getShipmentInfo())
                .map(PlatformAmazonFbaShipmentDTO::getUniqueId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shipmentIdList)){
            return currentDTOList;
        }

        String queryType = AmazonFbaQueryTypeEnum.SHIPMENT.getCode();
        String marketplaceId = marketPlaceEnum.getMarketplaceId();
        try {
            // 查询FBA货件item
            FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);
            // 主表信息为空, 请求获取
            GetShipmentsResponse shipments = api.getShipments(queryType, marketplaceId, null, shipmentIdList, null, null, null);
            if (CollectionUtils.isEmpty(shipments.getPayload().getShipmentData())){
                throw new ServiceException("[Amazon SP-APi] 查询FBA货件主信息结果为空异常：" + shipmentIdList);
            }
            Map<String, InboundShipmentInfo> mainMap = shipments.getPayload().getShipmentData()
                    .stream()
                    .collect(Collectors.toMap(InboundShipmentInfo::getShipmentId, Function.identity()));
            for (PlatformAmazonFbaShipmentDTO fbaShipmentDTO : currentDTOList) {
                InboundShipmentInfo shipmentInfo = fbaShipmentDTO.getShipmentInfo();
                if (null != shipmentInfo){
                    continue;
                }
                InboundShipmentInfo inboundShipmentInfo = mainMap.get(fbaShipmentDTO.getUniqueId());
                if (null == inboundShipmentInfo){
                    throw new ServiceException("[Amazon SP-APi] 未找到FBA货件主信息" + fbaShipmentDTO.getUniqueId());
                }
                fbaShipmentDTO.setShipmentInfo(inboundShipmentInfo);
            }
            return currentDTOList;
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 查询FBA货件主信息失败" + e);
        }
    }
}
