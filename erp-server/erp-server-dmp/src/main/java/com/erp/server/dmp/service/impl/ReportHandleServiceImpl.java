package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.convert.SdkFbaShipmentConverter;
import com.erp.sdk.oms.amz.spapi.dto.*;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaQueryTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonFbaShipmentStatusEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetShipmentItemsResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentItemList;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.InboundShipmentList;
import com.erp.server.dmp.convert.DmpFbaInventoryConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ReportHandleService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 亚马逊报告计划表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ReportHandleServiceImpl implements ReportHandleService {

    @Resource
    private MongoService mongoService;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    @Resource
    private BusinessServiceImpl businessService;


    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void combineInventory(ReportInventoryCombineMongoDTO combineInventoryDTO,
                                 List<ReportFbaMyiAllInventoryMongoDTO> fbaMyiAllInventoryMongoDTOList,
                                 List<ReportReservedMongoDTO> reportReservedMongoDTOList,
                                 List<ReportFbaInventoryPlanningMongoDTO> planningMongoDTOList
    ) {
        // 报告组合状态: 0=未组合，1=可组合, 2=已组合, 必须是可组合的记录
        if (1 != combineInventoryDTO.getCombineStatus()) {
            return;
        }
        // 修改记录为已组合
        // 修改数据
        combineInventoryDTO.setCombineStatus(2);
        MapUtil mapUtil = JSONUtil.toBean(JSONUtil.toJsonStr(combineInventoryDTO), MapUtil.class);
        ReportInventoryCombineMongoDTO queryCombineInventoryDTO = new ReportInventoryCombineMongoDTO(combineInventoryDTO.getDataStartTime(), combineInventoryDTO.getDataEndTime(), combineInventoryDTO.getMarketplaceIds());
        mongoService.updateMongoData(queryCombineInventoryDTO, mapUtil, MongoTableNameContant.REPORT_AMAZON_COMBINE_INVENTORY, ReportInventoryCombineMongoDTO.class);

        // 查询当前店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(combineInventoryDTO.getShopId());
        // 查询SKU绑定的信息
        List<String> sellerSkuList = fbaMyiAllInventoryMongoDTOList
                .stream()
                .map(ReportFbaMyiAllInventoryMongoDTO::getSku)
                .distinct()
                .collect(Collectors.toList());
        Map<String, ListingInfoEntity> listingInfoMap;
        if (!CollectionUtils.isEmpty(sellerSkuList)) {
            ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
            paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
            paramDTO.setPlatformSkuNoList(sellerSkuList);
            paramDTO.setMatchResult(true);
            listingInfoMap = omsListingInfoFeign.list(paramDTO)
                    .stream()
                    .collect(Collectors.toMap(ListingInfoEntity::getSkuNo, Function.identity()));
        } else {
            listingInfoMap = new HashMap<>();
        }

        // 转换
        Map<String, ReportFbaMyiAllInventoryMongoDTO> myiAllInventoryMap = fbaMyiAllInventoryMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        Map<String, ReportReservedMongoDTO> reservedMap = reportReservedMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        Map<String, ReportFbaInventoryPlanningMongoDTO> planningMap = planningMongoDTOList
                .stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}_{}", e.getAsin(), e.getFnsku(), e.getSku()), Function.identity()));

        // 转换实体
        List<FbaInventoryEntity> fbaInventoryEntityList = myiAllInventoryMap
                .entrySet()
                .stream()
                .map(e -> DmpFbaInventoryConverter.INSTANCE.mergeToFbaInventoryEntity(combineInventoryDTO,
                        e.getValue(),
                        reservedMap.get(e.getKey()),
                        planningMap.get(e.getKey()),
                        shopInfoEntity,
                        listingInfoMap.get(e.getValue().getSku())))
                .collect(Collectors.toList());
        // TODO 防止低时间数据修改校验？

        // 保存到WMS
        wmsFbaInventoryFeign.allBatchSave(fbaInventoryEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean pullShipment(DmpPullShipmentDTO dto) {
        // 获取店铺信息
        ShopInfoEntity shop = shopInfoFeign.getShopInfoById(dto.getShopId());
        if (null == shop) {
            throw new ServiceException("未找到店铺信息:shopId=" + dto.getShopId());
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shop.getDictCountryCode());

        try {
            FbaInboundApi api = FbaInboundApi.initApi(marketplaceEnum);
            String queryType = AmazonFbaQueryTypeEnum.SHIPMENT.getCode();
            String marketplaceId = marketplaceEnum.getMarketplaceId();
            List<String> shipmentStatusList = AmazonFbaShipmentStatusEnum.getAllStatus();
            List<String> shipmentIdList = dto.getShipmentCodeList();
            // 请求亚马逊接口
            InboundShipmentList responseList = api.getAllShipments(queryType, marketplaceId, shipmentStatusList, shipmentIdList, null, null, null);
            if (CollectionUtils.isEmpty(responseList)) {
                return true;
            }
            // 返回下载源数据
            List<PlatformAmazonFbaShipmentDTO> amazonFbaShipmentDTOList = responseList.stream()
                    .map(e -> new PlatformAmazonFbaShipmentDTO(e, shop))
                    .collect(Collectors.toList());

            // 查询FBA货件item
            for (PlatformAmazonFbaShipmentDTO shipmentDTO : amazonFbaShipmentDTOList) {
                GetShipmentItemsResponse response = api.getShipmentItemsByShipmentId(shipmentDTO.getShipmentInfo().getShipmentId(), marketplaceEnum.getMarketplaceId());
                InboundShipmentItemList itemData = response.getPayload().getItemData();
                shipmentDTO.setDetailList(itemData);
            }

            String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
            String platform = PlatformDictEnum.AMAZON.getCode();
            String business = BusinessTypeEnum.FBA_SHIPMENT.getCode();
            for (PlatformAmazonFbaShipmentDTO amazonShipmentDTO : amazonFbaShipmentDTOList) {
                amazonShipmentDTO.setDownloadStatus(1);
                amazonShipmentDTO.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                // 转换
                PlatformFbaShipmentDTO platformFbaShipmentDTO = SdkFbaShipmentConverter.INSTANCE.downloadDtoToSaveDto(amazonShipmentDTO);
                InboundShipmentItemList sourceDetailList = amazonShipmentDTO.getDetailList();
                List<PlatformFbaShipmentReceiveDTO> receiveDTOList = sourceDetailList.stream()
                        .map(SdkFbaShipmentConverter.INSTANCE::receiveDtoToSaveDto)
                        .collect(Collectors.toList());
                platformFbaShipmentDTO.setReceiveDTOList(receiveDTOList);
                // 合并成详情
                List<PlatformFbaShipmentReceiveDTO> detailListDTO = new ArrayList<>(
                        receiveDTOList.stream()
                                .collect(Collectors.toMap(
                                        shipment -> shipment.getFbaShipmentId() + shipment.getFnSku() + shipment.getSellerSku(),
                                        shipment -> shipment,
                                        PlatformFbaShipmentReceiveDTO::merge))
                                .values()
                );
                platformFbaShipmentDTO.setDetailList(detailListDTO);

                businessService.pullDetailProcess(amazonShipmentDTO, platformFbaShipmentDTO, category, platform, business);
            }
        } catch (Exception e) {
            throw new ServiceException("[Amazon SP-APi] 下载FBA货件失败" + e);
        }
        return true;
    }

    // TODO 转换切换mapstruct接口

}
