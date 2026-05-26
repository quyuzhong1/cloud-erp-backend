package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbsInventoryEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.entity.FbsInventoryEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Shopee FBS 库存 MQ 输出
 */
@Service
@Scope("prototype")
public class DmpOutputShopeeFbsInventoryRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpFbsInventoryEntity> dmpEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            if (!"dmp_fbs_inventory".equals(entry.getKey().getStorageName()) || CollUtil.isEmpty(entry.getValue())) {
                continue;
            }
            for (BaseEntity entity : entry.getValue()) {
                DmpFbsInventoryEntity dmpEntity = (DmpFbsInventoryEntity) entity;
                dmpEntityMap.put(dmpEntity.getId(), dmpEntity);
            }
        }

        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : dmpRequest.getChangeConvertInputDmpBaseEntityListMaps().entrySet()) {
            if (!"dmp_fbs_inventory".equals(entry.getKey().getStorageName()) || CollUtil.isEmpty(entry.getValue())) {
                continue;
            }
            for (BaseEntity entity : entry.getValue()) {
                changeIds.add(entity.getId());
            }
        }

        String shopId = dmpEntityMap.values().stream()
                .map(DmpFbsInventoryEntity::getNextLevelId)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
        List<String> platformSkuNoList = new ArrayList<>();
        for (DmpFbsInventoryEntity value : dmpEntityMap.values()) {
            if (StringUtils.isNotBlank(value.getPlatformSku())) {
                platformSkuNoList.add(value.getPlatformSku());
            } else if (StringUtils.isNotBlank(value.getFbsSku())) {
                platformSkuNoList.add(value.getFbsSku());
            }
        }

        List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewList = Collections.emptyList();
        if (StringUtils.isNotBlank(shopId) && CollUtil.isNotEmpty(platformSkuNoList)) {
            ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
            listingInfoParamDTO.setPlatformSkuNoList(platformSkuNoList);
            listingInfoParamDTO.setPlatform(PlatformDictEnum.SHOPEE.getCode());
            listingInfoParamDTO.setShopIdList(Collections.singletonList(shopId));
            mappingSkuViewList = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        }
        ShopInfoEntity shopInfo = StringUtils.isBlank(shopId) ? null : shopInfoFeign.getShopInfoById(shopId);

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            DmpFbsInventoryEntity dmpEntity = dmpEntityMap.get(changeId);
            FbsInventoryEntity entity = convert(dmpEntity, cfgOutputId, shopInfo, mappingSkuViewList);
            if (entity != null) {
                map.put(changeId, JSON.toJSONString(entity));
            }
        }
        return map;
    }

    public FbsInventoryEntity convert(DmpFbsInventoryEntity dmpEntity, String cfgOutputId,
                                      ShopInfoEntity shopInfo, List<SkuMappingDTO.MappingSkuViewDTO> mappingSkuViewList) {
        if (this.validateDataBlack(dmpEntity, cfgOutputId)) {
            return null;
        }
        FbsInventoryEntity entity = new FbsInventoryEntity();
        entity.setShopId(StringUtils.defaultString(dmpEntity.getNextLevelId()));
        entity.setShopName(shopInfo == null ? "" : StringUtils.defaultString(shopInfo.getName()));
        entity.setWarehouseId(StringUtils.defaultString(dmpEntity.getWarehouseId()));
        entity.setWarehouseName(StringUtils.defaultString(dmpEntity.getWarehouseName()));
        entity.setPlatformSku(StringUtils.defaultString(dmpEntity.getPlatformSku()));
        entity.setPlatformProductName(StringUtils.defaultString(dmpEntity.getPlatformProductName()));
        entity.setFbsSku(StringUtils.defaultString(dmpEntity.getFbsSku()));
        entity.setSpecName(StringUtils.defaultString(dmpEntity.getSpecName()));
        entity.setPurchaseMode(StringUtils.defaultString(dmpEntity.getPurchaseMode()));
        entity.setRecommendedReplenishmentQty(defaultInt(dmpEntity.getRecommendedReplenishmentQty()));
        entity.setTotalStockQty(defaultInt(dmpEntity.getTotalStockQty()));
        entity.setStockedInboundQty(defaultInt(dmpEntity.getStockedInboundQty()));
        entity.setTransferAsnInboundQty(defaultInt(dmpEntity.getTransferAsnInboundQty()));
        entity.setReservedQty(defaultInt(dmpEntity.getReservedQty()));
        entity.setUnsellableQty(defaultInt(dmpEntity.getUnsellableQty()));
        entity.setInTransitQty(defaultInt(dmpEntity.getInTransitQty()));
        entity.setTurnoverDays(defaultInt(dmpEntity.getTurnoverDays()));
        entity.setWarehouseInventoryCoverageDays(defaultInt(dmpEntity.getWarehouseInventoryCoverageDays()));
        entity.setDailyAvgSalesQty(dmpEntity.getDailyAvgSalesQty() == null ? java.math.BigDecimal.ZERO : dmpEntity.getDailyAvgSalesQty());
        entity.setLast7DaysSalesQty(defaultInt(dmpEntity.getLast7DaysSalesQty()));
        entity.setLast15DaysSalesQty(defaultInt(dmpEntity.getLast15DaysSalesQty()));
        entity.setLast30DaysSalesQty(defaultInt(dmpEntity.getLast30DaysSalesQty()));
        entity.setLast60DaysSalesQty(defaultInt(dmpEntity.getLast60DaysSalesQty()));
        entity.setLast90DaysSalesQty(defaultInt(dmpEntity.getLast90DaysSalesQty()));
        entity.setStockAge030Qty(defaultInt(dmpEntity.getStockAge030Qty()));
        entity.setStockAge3160Qty(defaultInt(dmpEntity.getStockAge3160Qty()));
        entity.setStockAge6190Qty(defaultInt(dmpEntity.getStockAge6190Qty()));
        entity.setStockAge91120Qty(defaultInt(dmpEntity.getStockAge91120Qty()));
        entity.setStockAge121180Qty(defaultInt(dmpEntity.getStockAge121180Qty()));
        entity.setStockAgeOver180Qty(defaultInt(dmpEntity.getStockAgeOver180Qty()));
        entity.setPlatformUpdateTime(dmpEntity.getPlatformUpdateTime());

        SkuMappingDTO.MappingSkuViewDTO mappingSkuViewDTO = mappingSkuViewList.stream()
                .filter(item -> StringUtils.equals(item.getPlatformSkuNo(), dmpEntity.getPlatformSku())
                        || StringUtils.equals(item.getPlatformSkuNo(), dmpEntity.getFbsSku()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(mappingSkuViewDTO)) {
            entity.setSkuId(StringUtils.defaultString(mappingSkuViewDTO.getProductSkuId()));
            entity.setSkuNo(StringUtils.defaultString(mappingSkuViewDTO.getProductSkuNo()));
            entity.setProductName(StringUtils.defaultString(mappingSkuViewDTO.getProductName()));
        } else {
            entity.setSkuId("");
            entity.setSkuNo("");
            entity.setProductName("");
        }
        return entity;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("fbsSku");
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
