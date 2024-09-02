package com.erp.server.wms.service;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.model.wms.dto.inventory.InventorySaveDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Classname: InventoryService
 * @Description: 实时库存服务接口类
 * @CreateTime: 2023-04-25  12:15
 * @Author: zhangchunlin
 * Update by Edison.qu 2023-10-07
 */
public interface InventoryService extends SuperService<InventoryEntity> {

    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录（库位为空也作为条件）;不对外使用
     *
     * @param orgId             库存组织
     * @param warehouseId       仓库ID
     * @param skuId             SKU ID
     * @param warehouseLocation 仓位
     * @param status            库存状体
     * @return 返回即时库存
     */
    InventoryEntity findInventory(String orgId, String warehouseId, String skuId, String warehouseLocation, String status);


    /**
     * 根据组织、仓库、库位、状态判断库存是否存在记录，带分布式锁，会根据是否控制库位查询（不控制库位则将库位查询条件置为空字符串）
     *
     * @param orgId             库存组织
     * @param warehouseId       仓库ID
     * @param skuId             SKU ID
     * @param warehouseLocation 仓位
     * @param status            库存状态
     * @return 返回库存锁
     */
    InventoryEntity findInventoryLock(String orgId, String warehouseId, String skuId, String warehouseLocation, String status);


    /**
     * 特别注意：库位为空，则赋值空库位查询
     *
     * @param orgId             库存组织
     * @param warehouseId       仓库ID
     * @param skuId             SKU ID
     * @param warehouseLocation 仓位ID
     * @param status            库存状态
     * @return 返回即时库存信息
     */
    InventoryEntity findInventoryIncLocation(String orgId, String warehouseId, String skuId, String warehouseLocation, String status);

    /**
     * 特别注意：库位没传，不带库位条件查询
     *
     * @param orgId             库存组织
     * @param warehouseId       仓库ID
     * @param skuId             SKU ID
     * @param warehouseLocation 仓位 ID
     * @param status            库存状态
     * @return 返回即时库存列表
     */
    List<InventoryEntity> findInventoryCheckLocation(String orgId, String warehouseId, String skuId, String warehouseLocation, String status);

    /**
     * 根据仓库、库位、状态获取可用库存数量；如果库位为空，则赋值空库位
     *
     * @param warehouseId       仓库ID
     * @param skuId             SKU Id
     * @param warehouseLocation 仓位
     * @return 返回可用库存汇总
     */
    Integer getUsableInventoryTotal(String warehouseId, String skuId, String warehouseLocation);

    /**
     * 根据仓库、SKU获取可用库存数量；特别注意：不带库位查询条件
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @return 返回当前SKU 库存汇总
     */
    Integer getUsableInventoryTotal(String warehouseId, String skuId);

    /**
     * 查实际库存
     * @author will
     * @date 2024/8/6 20:02
     * @param warehouseId
     * @param skuId
     * @return Integer
     */
    Integer getRealInventoryTotal(String warehouseId, String skuId);

    /**
     * 新增或修改库存
     *
     * @param warehouseId       仓库ID
     * @param orgId             库存组织
     * @param warehouseLocation 仓位
     * @param skuId             SKU Id
     * @param skuNo             SKU 编码
     * @param inventoryStatus   库存状态
     * @param qty               数量
     * @return 返回更新后的库存信息
     */
    InventorySaveDTO addOrUpdate(String warehouseId, String orgId, String warehouseLocation, String skuId, String skuNo, String inventoryStatus, Integer qty);


    /**
     * 根据skuIds 仓库 ，组织 仓位 获取到 sku即时库存（特别注意：没有传库位，则库位为空）
     *
     * @param skuIds            SKU ID列表
     * @param warehouseId       仓库ID
     * @param warehouseLocation 仓位ID
     * @param status            库存状态
     * @return 返回即时库存列表
     */
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(List<String> skuIds, String warehouseId, String warehouseLocation, String status);


    /**
     * 根据仓库列表 库位 获取到对应数据
     *
     * @param dto 查询参数
     * @return 返回SKU库存汇总信息
     */
    List<InventoryQtyDTO.SkuInventoryTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryParamDTO dto);


    /**
     * @description: 根据skuIds、仓库 、组织、仓位、库存状态 批量获取到 sku即时库存（库位没传，则查询空库位）
     * @author Will
     * @date: 2023/8/21 16:13
     * @param dto
     * @return List<SkuInventoryStatusTotalDTO>
     * 根据仓库列表 库位 获取到对应数据
     * @param dto   查询参数
     * @return      返回SKU库存汇总信息
     */
    List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> listSkuInventory(InventoryQtyDTO.SkuInventoryStatusParamDTO dto);


    /**
     * 根据组织、仓库、库位、状态、SKU获取库存数量；如果库位为空，则不判断库位
     *
     * @param orgId             库存组织
     * @param warehouseId       仓库ID
     * @param skuId             SKU Id
     * @param warehouseLocation 仓位
     * @param status            库存状态
     * @return 返回即时库存汇总
     */
    Integer getInventoryTotal(String orgId, String warehouseId, String skuId, String warehouseLocation, String status);

    /**
     * 修改库存表数量
     *
     * @param id  即时库存ID
     * @param qty 交易数量
     * @return true：成功，false:失败
     */
    boolean updateQtyById(String id, Integer qty);

    /**
     * 查询拣货数据
     *
     * @param dto 查询参数
     * @return 拣货库存数据
     */
    List<InventoryEntity> listPickingDetailInventory(PickingDetailDTO.InventoryParamDTO dto);


    /**
     * 即时库存分页列表
     *
     * @param pagingParamDTO 查询参数
     * @return 即时库存列表
     */
    PagingVO<InventoryDTO.PagingViewDTO> paging(PagingDTO<InventoryDTO.SearchParamDTO> pagingParamDTO);

    /**
     * 导出即时库存Excel
     *
     * @param param    业务参数
     */
    void exportExcel(InventoryDTO.ExportSearchParamDTO param);

    /**
     * 库龄计算表分页列表
     *
     * @param pagingParamDTO 分页查询参数
     * @return 库龄列表
     */
    PagingVO<LinkedHashMap> inventoryAgePaging(PagingDTO<InventoryReportDTO.InventoryAgeSearchParamDTO> pagingParamDTO);

    /**
     * 库龄计算表导出
     *
     * @param paramDTO 查询参数
     */
    void exportInventoryAge(InventoryReportDTO.ExportInventoryAgeSearchParamDTO paramDTO);

    /**
     * 根据仓库id查询库存信息
     *
     * @param warehouseId 仓库ID
     * @return PDA剩余库存信息
     */
    InventoryDTO.PdaHomeInventoryBalanceDTO getInventoryByWarehouseId(String warehouseId);

    /**
     * 根据盘点类型查询库存
     *
     * @param entity           盘点单据信息
     * @param detailEntityList 盘点明细
     * @return 即时库存列表
     */
    List<InventoryEntity> listByStocktakingType(StocktakingPlanEntity entity, List<StocktakingPlanDetailEntity> detailEntityList);

    /**
     * 根据条件查询库存信息
     *
     * @param list 查询参数
     * @return PDA查询即时库存
     **/
    List<InventoryDTO.PdaInventoryDTO> getInventoryByParam(InventoryDTO.PdaSearchParamDTO list);

    /**
     * 根据条件查询库存信息
     * @author hyj
     * @date 2024/4/17 10:57
     * @param dtos
     */
    List<InventoryDTO.InventoryViewQtyDTO> getInventoryQty(List<InventoryDTO.InventoryBySkuIdAndWarehouseDTO> dtos);
    /**
     * PDA:库存查询
     *
     * @return PDA即时库存
     **/
    InventoryDTO.PdaInventorySearch getInventoryBySkuNo(PagingDTO<InventoryDTO.PdaSearchParamDTO> dto);


    /**
     * 根据条件查询对应库存
     * @author yl
     * @date 2023-10-19 11:11
     * @param dto
     * @return java.util.List<com.erp.model.wms.entity.InventoryEntity>
     */
    List<InventoryEntity> listInventoryByParam(InventoryDTO.ParamDTO dto);

    /**
     * 根据参数获取对应数据
     * @author yl
     * @date 2023-10-24 16:59
     * @param dto
     * @return com.erp.model.wms.dto.inventory.InventoryDTO.InventoryQtyDTO
     */
    InventoryDTO.InventoryQtyDTO getInventoryQty(InventoryDTO.InventoryBySkuNoDTO dto);

    /**
     * 根据条件查询库存信息
     * @Author Luo_WG
     * @Date 2023/11/1 19:27
     * @param list
     * @return java.util.List<com.erp.model.wms.entity.InventoryEntity>
     **/
    List<InventoryDTO.UsableInventoryViewDTO> listByParam(List<InventoryDTO.UsableInventoryParamDTO> list);

    /**
     * 根据sku获取sku对应库位库存
     *
     * @param skus sku集合
     */
    List<InventoryDTO.LocationInventoryResult> listLocationInventoryBySkus(List<InventoryDTO.LocationInventoryParam> skus);

    /**
     * 根据仓库id sku 数量获取最优仓位
     * @param param param
     */
    InventoryDTO.LocationInventory recommendedLocation(InventoryDTO.RecommendedLocationParam param);

    /**
     * 按仓库统计数量
     */
    long countByWarehouse();

    /**
     * 按库区统计数量
     */
    long countByArea();

    /**
     * 按仓位统计数量
     */
    long countByLocation();

    /**
     * PDA:库存查询（仓库）
     * @param searchDTO
     * @return
     */
    InventoryDTO.PdaInventoryWarehousePageDTO<InventoryDTO.PdaInventoryPageDTO> getInventoryByWarehouse(PagingDTO<InventoryDTO.PdaSearchParamDTO> searchDTO);

    /**
     * 查询某个仓位的库存数量
     * @param warehouseId 仓库ID
     * @param warehouseLocation 仓位编码
     * @return 库存数量
     * @date: 2024-06-13
     * @author: tanmujin
     */
    Integer getQtyByLocation(String warehouseId, String warehouseLocation);

    /**
     * 统计冻结库存数量
     * @param warehouseId
     * @param skuId
     * @return
     */
    Integer getFrozenInventoryTotal(String warehouseId, String skuId);
    /**
     * 根据skuId和仓库id获取可用数量
     * @param paramDTO
     * @return
     */
    List<InventoryDTO.InventoryViewQtyDTO> getUsableQtyBySkuIdsAndWarehouseIds(InventoryDTO.ParamDTO paramDTO);

    /**
     * 导出数据分页
     * @param dto 导出条件
     */
    PagingVO<InventoryDTO.PagingViewDTO> getInventoryPageData(PagingDTO<InventoryDTO.ExportSearchParamDTO> dto);

    /**
     * 根据sku获取库存列表
     * @param dto
     * @return
     */
    List<InventoryEntity> listInventoryBySkuIds(InventoryQtyDTO.InventoryBySkuDTO dto);
    /**
     * 根据skuId、仓库id、仓位、库存状态 获取库存信息
     * @param skuId                 SKU ID
     * @param warehouseId           仓库ID
     * @param warehouseLocation     仓位
     * @param inventoryStatus       库存状态
     * @return                      库存信息
     */
    InventoryEntity getInventory(String skuId, String warehouseId, String warehouseLocation, String inventoryStatus);

    PagingVO<DynamicExcelDTO> exportWmsInventoryAge(PagingDTO<InventoryReportDTO.ExportInventoryAgeSearchParamDTO> dto);
}
