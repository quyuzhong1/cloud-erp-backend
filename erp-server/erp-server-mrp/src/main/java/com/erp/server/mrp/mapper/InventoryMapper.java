package com.erp.server.mrp.mapper;

import com.erp.model.mrp.dto.LocalInventoryDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {


    int getFbaUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("code") String code, @Param("tableName") String tableName);

    int getFbaOldUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName);

    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaShipment(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaDelivery(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("shipmentName") String shipmentName, @Param("shipmentDetailName") String shipmentDetailName);

    List<LogisticsBillEntity> getLogisticsBillBySourceIds(@Param("sourceIds") List<String> firstMileDeliveryIds, @Param("tableName") String tableName);

    List<LogisticsChannelEntity> listLogisticsChannelByIds(@Param("channelIds") List<String> channelIds, @Param("tableName") String tableName);

    List<FirstMileDeliveryDTO.FbaShipmentDTO> listFirstMileDelivery(@Param("codes") List<String> codes, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    boolean isTableExist(@Param("tableName") String tableName);

    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getPlanDelivery(@Param("type") String type, @Param("codes") List<String> strategyCodes, @Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    int getOverseasUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("code") String code, @Param("tableName") String tableName);

    List<LocalInventoryDTO> getLocalUsable(@Param("skuId") String skuId, @Param("codes") List<String> codes, @Param("tableName") String tableName);

    List<LocalInventoryDTO> getVirtualUsable(@Param("skuId") String skuId, @Param("codes") List<String> codes, @Param("tableName") String tableName);

    List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> listPurchasePlan(@Param("codes") List<String> codes, @Param("skuId") String skuId, @Param("localWarehouseIds") List<String> localWarehouseIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> listPurchase(@Param("codes") List<String> codes, @Param("skuId") String skuId, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    List<PurchaseApplicationRefPoDTO.ListDTO> listPurchaseApplicationRefPo(@Param("detailIds") List<String> detailIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("otherTableName") String otherTableName);

    List<SubcontractOrderDetailEntity> listSubcontractOrderDetail(@Param("detailIds") List<String> detailIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("otherTableName") String otherTableName);

    List<FbaInventoryEntity> getAllFbaHistoryInventory(String tableName);

    List<ReplenishmentResultDTO.LocalInTransitDetailDTO> getLocalInTransitDetail(@Param("isPurchase") Boolean isPurchase, @Param("isTransfer") Boolean isTransfer,
                                                                                 @Param("skuId") String skuId, @Param("localWarehouseIds") List<String> localWarehouseIds, @Param("transactionFlow") String transactionFlow, @Param("instockForcast") String instockForcast, @Param("poReceive") String poReceive,
                                                                                 @Param("poInstock") String poInstock, @Param("poReturn") String poReturn, @Param("transferOut") String transferOut, @Param("transferIn") String transferIn);

    List<OverseasInventoryEntity> getAllOverseasHistoryInventory(String tableName);

    List<InventoryEntity> getAllLocalHistoryInventory(String tableName);
}
