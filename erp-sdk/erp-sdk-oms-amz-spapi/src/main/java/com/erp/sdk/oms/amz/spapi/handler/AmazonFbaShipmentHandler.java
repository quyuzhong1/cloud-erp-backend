package com.erp.sdk.oms.amz.spapi.handler;

import cn.hutool.json.JSONUtil;
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
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedList;
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
            FbaInboundApi api = FbaInboundApi.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);
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
        List<PlatformFbaShipmentDTO> resultList = new LinkedList<>();
        for (PlatformAmazonFbaShipmentDTO sourceDto : sourceDataList) {
            PlatformFbaShipmentDTO newDto = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(sourceDto);
            resultList.add(newDto);
        }
        return resultList;
//        return sourceDataList.stream()
//                // 组装
//                .map(SdkFbaShipmentConverter.INSTANCE::downloadDtoToSaveDto)
//                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String json = "{\"shipmentInfo\":{\"shipmentId\":\"FBA17FKD1MPZ\",\"shipmentName\":\"FBA STA (10/07/2023 07:09)-CMH2\",\"shipFromAddress\":{\"name\":\"LC108092（Ling）\",\"addressLine1\":\"14939 Summit Drive\",\"city\":\"Eastvale\",\"stateOrProvinceCode\":\"CA\",\"countryCode\":\"US\",\"postalCode\":\"92880\"},\"destinationFulfillmentCenterId\":\"CMH2\",\"shipmentStatus\":\"RECEIVING\",\"labelPrepType\":\"SELLER_LABEL\",\"boxContentsSource\":\"INTERACTIVE\"},\"shopId\":\"1720261566995107842\",\"shopName\":\"亚马逊测试店铺美国\",\"platformUpdateTime\":1698995263691,\"downloadStatus\":0,\"downloadTime\":\"\",\"detailList\":[]}";
        PlatformFbaShipmentDTO shipmentDTO = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(JSONUtil.toBean(json, PlatformAmazonFbaShipmentDTO.class));
        System.out.println(JSONUtil.toJsonStr(shipmentDTO));
    }

    @Override
    public String getTargetPlatform() {
        return PlatformDictEnum.AMAZON.getCode();
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
        String shopId = dto.getShopId();
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(dto.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("未找到店铺授权:" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        try {
            // 查询FBA货件item
            FbaInboundApi api = FbaInboundApi.initApi(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false);
            GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(dto.getShipmentInfo().getShipmentId(), marketPlaceEnum.getMarketplaceId());
            InboundShipmentItemList itemData = response.getPayload().getItemData();
            dto.setDetailList(itemData);
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 查询FBA货件item失败" + e);
        }
        return dto;
    }
}
