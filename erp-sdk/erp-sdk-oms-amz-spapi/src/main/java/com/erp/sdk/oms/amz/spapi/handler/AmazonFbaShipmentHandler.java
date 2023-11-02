package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractFbaShipmentHandler;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
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
    private ShopInfoFeign shopInfoFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlatformAmazonFbaShipmentDTO> download(JobTaskDTO data) {
        // 获取店铺信息
        String shopId = data.getShopId();
        ShopInfoEntity shop = shopInfoFeign.getShopInfoById(shopId);
        if (null == shop) {
            throw new ServiceException("未找到店铺信息:shopId=" + shopId);
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shop.getDictCountryCode());

        try {
            FbaInboundApi api = FbaInboundApi.initApi(marketplaceEnum);
            String queryType = AmazonFbaQueryTypeEnum.DATE_RANGE.getCode();
            String marketplaceId = marketplaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            List<String> shipmentIdList = null;
            String lastUpdatedAfter = DateUtil.plus8SameUtcOffset(data.getNextTime()).toString();
            String lastUpdatedBefore = DateUtil.plus8SameUtcOffset(LocalDateTime.now()).toString();
            String nextToken = null;
            InboundShipmentList responseList = api.getAllShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, lastUpdatedAfter, lastUpdatedBefore, nextToken);
            // 返回下载源数据
            return responseList.stream()
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shop))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
        }
    }


    @Override
    public List<PlatformFbaShipmentDTO> convert(List<PlatformAmazonFbaShipmentDTO> sourceDataList) {
        // 亚马逊FBA货件转换为发送mq数据
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(SdkFbaShipmentConverter.INSTANCE::downloadDtoToSaveDto)
                .collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.AMAZON.getCode();
    }

    @Override
    public PlatformFbaShipmentDTO downloadDetail(PlatformFbaShipmentDTO dto, JSONObject extendObj) {
        String shopId = extendObj.getString("shopId");
        // 获取店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
        if (null == shopInfoEntity) {
            throw new ServiceException("未找到店铺详情:" + shopId);
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoEntity.getDictCountryCode());
        try {
            // 查询FBA货件item
            FbaInboundApi api = FbaInboundApi.initApi(marketPlaceEnum);
            GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(dto.getFbaShipmentId(), marketPlaceEnum.getMarketplaceId());
            InboundShipmentItemList itemData = response.getPayload().getItemData();

            List<PlatformFbaShipmentReceiveDTO> receiveDTOList = itemData.stream()
                    .map(SdkFbaShipmentConverter.INSTANCE::receiveDtoToSaveDto)
                    .collect(Collectors.toList());
            dto.setReceiveDTOList(receiveDTOList);
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 查询FBA货件item失败" + e);
        }
        return dto;
    }
}
