package com.erp.server.mrp.mapper;

import cn.hutool.json.JSONArray;
import com.erp.model.mrp.dto.LocalInventoryDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 库存接口
 */
@Mapper
public interface InventoryMapper {

    /**
     * 计算FBA可用库存
     */
    int getFbaUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("code") String code, @Param("tableName") String tableName);

    /**
     * 查询FBA货件数据
     */
    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaShipment(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    /**
     * 查询FBA发货单数据
     */
    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaDelivery(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("shipmentName") String shipmentName, @Param("shipmentDetailName") String shipmentDetailName);

    /**
     * 查询物流单
     */
    List<LogisticsBillEntity> getLogisticsBillBySourceIds(@Param("sourceIds") List<String> firstMileDeliveryIds, @Param("tableName") String tableName);


    /**
     * 查询头程物流单
     */
    List<FirstMileDeliveryDTO.FbaShipmentDTO> listFirstMileDelivery(@Param("codes") List<String> codes, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    /**
     * 查询表是否创建
     */
    boolean isTableExist(@Param("tableName") String tableName);

    /**
     * 查询发货计划
     */
    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getPlanDelivery(@Param("type") String type, @Param("codes") Set<String> strategyCodes,
                                                                            @Param("result") ReplenishmentResultDTO replenishmentResultDTO,
                                                                            @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("sourceType") String sourceType);

    /**
     * 查询海外仓可用库存
     */
    List<LocalInventoryDTO.OverseasInventoryDTO> getOverseasUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("code") String code, @Param("tableName") String tableName, @Param("warehouseCode") Set<String> warehouseCode);

    /**
     * 查询本地仓可用库存
     */
    List<LocalInventoryDTO> getLocalUsable(@Param("skuId") String skuId, @Param("codes") Set<String> codes, @Param("tableName") String tableName);

    /**
     * 查询虚拟仓可用库存
     */
    List<LocalInventoryDTO> getVirtualUsable(@Param("skuId") String skuId, @Param("codes") Set<String> codes, @Param("tableName") String tableName);

    /**
     * 查询采购计划
     */
    List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> listPurchasePlan(@Param("codes") Set<String> codes, @Param("skuId") String skuId, @Param("localWarehouseIds") List<String> localWarehouseIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    /**
     * 查询采购单
     */
    List<ReplenishmentResultDTO.EstimatedPurchaseDetailDTO> listPurchase(@Param("codes") Set<String> codes, @Param("skuId") String skuId, @Param("localWarehouseIds") List<String> localWarehouseIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    /**
     * 查询采购申请关联
     */
    List<PurchaseApplicationRefPoDTO.ListDTO> listPurchaseApplicationRefPo(@Param("detailIds") List<String> detailIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("otherTableName") String otherTableName);

    /**
     * 查询委外订单
     */
    List<SubcontractOrderDetailEntity> listSubcontractOrderDetail(@Param("detailIds") List<String> detailIds, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("otherTableName") String otherTableName);

    /**
     * 查询全部FBA历史库存
     */
    List<FbaInventoryEntity> getAllFbaHistoryInventory(@Param("tableName") String tableName);

    /**
     * 查询本地在途明细
     */
    List<ReplenishmentResultDTO.LocalInTransitDetailDTO> getLocalInTransitDetail(@Param("isPurchase") Boolean isPurchase, @Param("isTransfer") Boolean isTransfer,
                                                                                 @Param("skuId") String skuId, @Param("localWarehouseIds") List<String> localWarehouseIds, @Param("transactionFlow") String transactionFlow, @Param("instockForcast") String instockForcast, @Param("poReceive") String poReceive,
                                                                                 @Param("poInstock") String poInstock, @Param("poReturn") String poReturn, @Param("transferOut") String transferOut, @Param("transferIn") String transferIn);

    /**
     * 查询全部海外仓历史库存
     */
    List<OverseasInventoryEntity> getAllOverseasHistoryInventory(@Param("tableName") String tableName);

    /**
     * 查询全部本地仓历史库存
     */
    List<InventoryEntity> getAllLocalHistoryInventory(@Param("tableName") String tableName);

    /**
     * 获取虚拟仓库存
     */
    List<VirtualInventoryEntity> getAllVirtualHistoryInventory(@Param("tableName") String tableName);

    /**
     * 查询补货计划
     */
    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getReplenishmentPlan(@Param("codes") Set<String> replenishmentPlan,
                                                                                 @Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName,
                                                                                 @Param("otherTableName") String otherTableName, @Param("otherTableDetailName") String otherTableDetailName);

    /**
     * 查询发货计划
     */
    List<ReplenishmentResultDTO.OverseasInTransitDetailDTO> getOverseasDelivery(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName,
                                                                                @Param("tableDetailName") String tableDetailName, @Param("otherTableName") String otherTableName,
                                                                                @Param("otherTableDetailName") String otherTableDetailName);
}
